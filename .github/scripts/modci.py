#!/usr/bin/env python3
"""Config-driven release/publish helper for multi-loader Minecraft mods.

Nothing here is mod-specific: everything comes from .github/publish.json and
.github/branches.json, so this file can be copied verbatim into another repo.
See .github/CI.md for the config reference.

Subcommands:
  branches  resolve branches.json against the remote refs   (discover.yml)
  collect   gather the built jars declared by the config    (release.yml)
  matrix    turn release assets into a publish matrix       (publish.yml)
  validate  sanity-check the config
"""

import argparse
import fnmatch
import json
import os
import re
import shutil
import sys
from pathlib import Path

ON_ACTIONS = os.environ.get("GITHUB_ACTIONS") == "true"

DEFAULT_DISPLAY_NAME = "{name} {version} - MC {mc_range} ({loader_names})"
DEFAULT_VERSION_PATTERN = "{version}+mc{mc}-{artifact}"
DEFAULT_LOADER_NAMES = {
    "fabric": "Fabric",
    "quilt": "Quilt",
    "forge": "Forge",
    "neoforge": "NeoForge",
    "rift": "Rift",
    "liteloader": "LiteLoader",
}
DEFAULT_VERSION_TYPE_RULES = {
    "alpha": ["*snapshot*", "*alpha*"],
    "beta": ["*beta*", "*pre*", "*rc*"],
}
DEFAULT_JAR_EXCLUDES = ["*-dev.jar", "*-sources.jar", "*-javadoc.jar", "*-all.jar"]
DEFAULT_BRANCH_OPTIONS = {
    "java": "17",
    "java_distribution": "temurin",
    "gradle_task": "build",
    "gradle_args": "",
}


# --------------------------------------------------------------------------- io

def warn(msg):
    print(f"::warning::{msg}" if ON_ACTIONS else f"warning: {msg}", file=sys.stderr)


def fail(msg):
    print(f"::error::{msg}" if ON_ACTIONS else f"error: {msg}", file=sys.stderr)
    sys.exit(1)


def gh_output(key, value):
    path = os.environ.get("GITHUB_OUTPUT")
    if path:
        with open(path, "a", encoding="utf-8") as fh:
            fh.write(f"{key}={value}\n")


def gh_summary(markdown):
    path = os.environ.get("GITHUB_STEP_SUMMARY")
    if path:
        with open(path, "a", encoding="utf-8") as fh:
            fh.write(markdown.rstrip() + "\n\n")


def load_json(path, what):
    file = Path(path)
    if not file.is_file():
        fail(f"{what} not found at '{path}'")
    try:
        return json.loads(file.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        fail(f"{what} at '{path}' is not valid JSON: {exc}")


# ----------------------------------------------------------------------- config

def load_config(path):
    cfg = load_json(path, "publish config")
    if not isinstance(cfg, dict):
        fail("publish config must be a JSON object")
    if not cfg.get("name"):
        fail("publish config is missing the required 'name' field")
    artifacts = cfg.get("artifacts")
    if not isinstance(artifacts, dict) or not artifacts:
        fail("publish config is missing a non-empty 'artifacts' object")
    known_loaders = {**DEFAULT_LOADER_NAMES, **(cfg.get("loader_names") or {})}
    for key, art in artifacts.items():
        if not isinstance(art, dict):
            fail(f"artifact '{key}' must be an object")
        parent = art.get("attach_to")
        if parent and parent not in artifacts:
            fail(f"artifact '{key}' attaches to unknown artifact '{parent}'")
        if not art.get("loaders") and key not in known_loaders:
            warn(f"artifact '{key}' has no 'loaders' and is not a known loader - assuming ['{key}']")
    return cfg


def artifact_loaders(key, art):
    return list(art.get("loaders") or [key])


def mc_entry(cfg, mc):
    """Per-Minecraft-version block; a bare list is shorthand for game_versions."""
    raw = (cfg.get("minecraft") or {}).get(mc)
    if raw is None:
        return {}
    if isinstance(raw, list):
        return {"game_versions": raw}
    if isinstance(raw, dict):
        return raw
    fail(f"minecraft.{mc} must be a list or an object")


def game_versions_for(cfg, mc):
    versions = mc_entry(cfg, mc).get("game_versions")
    if not versions:
        warn(f"no 'minecraft.{mc}' entry in the config - publishing for '{mc}' only")
        return [mc]
    return list(versions)


def mc_range(versions):
    return versions[0] if len(versions) == 1 else f"{versions[0]}-{versions[-1]}"


def dedupe(items):
    seen, out = set(), []
    for item in items:
        if item not in seen:
            seen.add(item)
            out.append(item)
    return out


def dependencies_for(cfg, mc, key, art, loaders):
    """Resolution: artifact > minecraft[mc] > global, with '*' entries prepended."""

    def split(table):
        if not isinstance(table, dict):
            return None, None
        specific = None
        for candidate in [key, *loaders]:
            if candidate in table:
                specific = table[candidate]
                break
        return table.get("*"), specific

    g_common, g_specific = split(cfg.get("dependencies"))
    m_common, m_specific = split(mc_entry(cfg, mc).get("dependencies"))

    common = m_common if m_common is not None else (g_common or [])
    specific = art.get("dependencies")
    if specific is None:
        specific = m_specific if m_specific is not None else (g_specific or [])
    return dedupe([*common, *specific])


def pick(*values):
    for value in values:
        if value not in (None, ""):
            return value
    return None


def resolve_platform_id(platform, cfg, art, repo_vars):
    """Literal id wins, then a repo-variable name, then the default <PLATFORM>_ID var."""
    for source in (art, cfg):
        block = source.get(platform)
        if isinstance(block, str):
            return block
        if isinstance(block, dict):
            if block.get("id"):
                return str(block["id"])
            if block.get("id_var"):
                name = block["id_var"]
                if not repo_vars.get(name):
                    warn(
                        f"'{platform}.id_var' points at the repository variable '{name}', which is "
                        f"not set - define it in the repository settings, or put the project id "
                        f"straight in the config with \"{platform}\": {{ \"id\": \"...\" }}"
                    )
                return str(repo_vars.get(name, ""))
    return str(repo_vars.get(f"{platform.upper()}_ID", ""))


def platform_flag(platform, cfg, art, field, default):
    for source in (art, cfg):
        block = source.get(platform)
        if isinstance(block, dict) and field in block:
            return block[field]
    return default


def auto_version_type(version, cfg):
    rules = cfg.get("version_type_rules") or DEFAULT_VERSION_TYPE_RULES
    lowered = version.lower()
    for channel, patterns in rules.items():
        if any(fnmatch.fnmatchcase(lowered, p.lower()) for p in patterns):
            return channel
    return "release"


class _Placeholders(dict):
    def __missing__(self, key):
        warn(f"unknown placeholder '{{{key}}}' in a name template")
        return "{" + key + "}"


def render(template, values):
    return template.format_map(_Placeholders(values))


# -------------------------------------------------------------------- filenames

def build_pattern(cfg):
    """<archives-name>-<version>+mc<mc>-<artifact>.jar, or a custom regex."""
    custom = cfg.get("filename_regex")
    if custom:
        pattern = re.compile(custom)
        for group in ("version", "mc", "artifact"):
            if group not in pattern.groupindex:
                fail(f"'filename_regex' must define a (?P<{group}>...) group")
        return pattern
    base = cfg.get("archives_name") or cfg["name"]
    base_re = ".+?" if base == "*" else re.escape(base)
    keys = sorted(cfg["artifacts"], key=len, reverse=True)
    alternation = "|".join(re.escape(k) for k in keys)
    return re.compile(
        rf"^(?P<base>{base_re})-(?P<version>.+?)\+mc(?P<mc>.+?)-(?P<artifact>{alternation})\.jar$"
    )


def parse_asset(pattern, filename):
    match = pattern.match(filename)
    if not match:
        return None
    parsed = match.groupdict()
    return {
        "file": filename,
        "artifact": parsed["artifact"],
        "mc": parsed["mc"],
        "mod_version": parsed["version"],
    }


def read_list(path):
    """Accepts a JSON array (of strings or {name: ...}) or a plain newline list."""
    text = sys.stdin.read() if path == "-" else Path(path).read_text(encoding="utf-8")
    text = text.strip()
    if not text:
        return []
    if text[0] in "[{":
        data = json.loads(text)
        if isinstance(data, dict):
            data = data.get("assets", [])
        return [item["name"] if isinstance(item, dict) else str(item) for item in data]
    return [line.strip() for line in text.splitlines() if line.strip()]


def csv(value):
    return [part.strip() for part in (value or "").split(",") if part.strip()]


# ------------------------------------------------------------------- subcommands

def cmd_branches(args):
    raw = load_json(args.config, "branches config")
    if isinstance(raw, list):
        entries, defaults = raw, {}
    elif isinstance(raw, dict):
        entries, defaults = raw.get("branches") or [], raw.get("defaults") or {}
    else:
        fail("branches config must be a JSON array or object")
    if not entries:
        fail("branches config lists no branches")

    refs = read_list(args.refs)
    resolved, seen = [], set()
    for entry in entries:
        if isinstance(entry, str):
            entry = {"pattern": entry}
        pattern = pick(entry.get("pattern"), entry.get("name"), entry.get("branch"))
        if not pattern:
            fail(f"branch entry {json.dumps(entry)} has no 'pattern'")
        if any(ch in pattern for ch in "*?["):
            matched = [b for b in refs if fnmatch.fnmatchcase(b, pattern)]
            if not matched:
                warn(f"pattern '{pattern}' matched no branches")
        else:
            matched = [pattern] if pattern in refs else []
            if not matched:
                warn(f"branch '{pattern}' does not exist, skipping")
        options = {k: v for k, v in entry.items() if k not in ("pattern", "name", "branch")}
        for branch in matched:
            if branch in seen:
                continue
            seen.add(branch)
            item = {**DEFAULT_BRANCH_OPTIONS, **defaults, **options}
            if isinstance(item.get("gradle_args"), list):
                item["gradle_args"] = " ".join(item["gradle_args"])
            item["java"] = str(item["java"])
            item["branch"] = branch
            item["safe"] = re.sub(r"[^A-Za-z0-9._-]", "-", branch)
            resolved.append(item)

    if not resolved:
        fail("no branches resolved - check .github/branches.json against the remote branches")

    print(json.dumps(resolved, indent=2))
    gh_output("branches", json.dumps(resolved, separators=(",", ":")))
    gh_summary(
        "### Branches to build\n\n| branch | JDK | gradle |\n|---|---|---|\n"
        + "\n".join(
            "| `{branch}` | {java} | `{gradle}` |".format(
                branch=b["branch"], java=b["java"],
                gradle=" ".join(filter(None, [b["gradle_task"], b["gradle_args"]])),
            )
            for b in resolved
        )
    )


def cmd_collect(args):
    cfg = load_config(args.config)
    root = Path(args.root).resolve()
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    pattern = build_pattern(cfg)
    excludes = cfg.get("jar_excludes") or DEFAULT_JAR_EXCLUDES

    collected = []
    for key, art in cfg["artifacts"].items():
        if art.get("skip"):
            continue
        globs = art.get("files")
        if not globs:
            if not art.get("module"):
                fail(f"artifact '{key}' needs a 'module' or a 'files' glob list")
            globs = [f"{art['module'].strip('/')}/build/libs/*.jar"]

        candidates = []
        for glob in globs:
            candidates += sorted(p for p in root.glob(glob) if p.is_file())
        candidates = [
            p for p in dedupe(candidates)
            if not any(fnmatch.fnmatchcase(p.name, ex) for ex in [*excludes, *(art.get("exclude") or [])])
        ]
        if not candidates:
            warn(f"no jar found for artifact '{key}' (looked in: {', '.join(globs)})")
            continue

        for jar in candidates:
            safe = re.sub(r"[^A-Za-z0-9._+-]", ".", jar.name)
            parsed = parse_asset(pattern, safe)
            if not parsed:
                warn(f"'{jar.name}' does not match the '{key}' naming convention - publish will skip it")
            elif parsed["artifact"] != key:
                warn(f"'{jar.name}' was collected for '{key}' but parses as '{parsed['artifact']}'")
            shutil.copy2(jar, out_dir / safe)
            collected.append({"artifact": key, "file": safe, "source": str(jar.relative_to(root))})

    if not collected:
        warn(f"no release jars collected under '{root}'")
        gh_output("found", "false")
        gh_output("count", "0")
        return

    for item in collected:
        print(f"{item['artifact']:>12}  {item['source']} -> {item['file']}")
    gh_output("found", "true")
    gh_output("count", str(len(collected)))
    gh_summary(
        f"### Collected on `{args.label or root.name}`\n\n| artifact | file |\n|---|---|\n"
        + "\n".join(f"| `{i['artifact']}` | `{i['file']}` |" for i in collected)
    )


def cmd_matrix(args):
    cfg = load_config(args.config)
    repo_vars = json.loads(args.vars) if args.vars else {}
    artifacts = cfg["artifacts"]
    pattern = build_pattern(cfg)

    only_artifacts, only_mc = csv(args.artifacts), csv(args.minecraft)
    parsed = []
    for name in read_list(args.assets):
        if not name.endswith(".jar"):
            continue
        item = parse_asset(pattern, name)
        if not item:
            print(f"ignoring '{name}' - not an artifact jar", file=sys.stderr)
            continue
        if only_mc and item["mc"] not in only_mc:
            continue
        parsed.append(item)

    # Extras ride along with their parent artifact's version.
    extras = {}
    for item in list(parsed):
        parent = artifacts[item["artifact"]].get("attach_to")
        if not parent:
            continue
        parsed.remove(item)
        extras.setdefault((item["mc"], parent), []).append(item)

    entries = []
    for item in parsed:
        key, mc = item["artifact"], item["mc"]
        art = artifacts[key]
        if art.get("publish") is False:
            continue
        if only_artifacts and key not in only_artifacts:
            continue

        loaders = artifact_loaders(key, art)
        versions = game_versions_for(cfg, mc)
        loader_names = [
            (cfg.get("loader_names") or {}).get(loader, DEFAULT_LOADER_NAMES.get(loader, loader.title()))
            for loader in loaders
        ]
        version_type = args.version_type
        if version_type in (None, "", "auto"):
            version_type = auto_version_type(item["mod_version"], cfg)

        placeholders = {
            "name": cfg["name"],
            "version": item["mod_version"],
            "mc": mc,
            "mc_range": mc_range(versions),
            "game_versions": ", ".join(versions),
            "artifact": key,
            "label": art.get("label", ""),
            "loader": loaders[0],
            "loaders": ", ".join(loaders),
            "loader_names": "/".join(loader_names),
            "tag": args.tag or "",
            "version_type": version_type,
        }
        display_template = pick(
            art.get("display_name"), mc_entry(cfg, mc).get("display_name"),
            cfg.get("display_name"), DEFAULT_DISPLAY_NAME,
        )
        version_template = pick(
            art.get("version_pattern"), cfg.get("version_pattern"), DEFAULT_VERSION_PATTERN
        )

        files = [item["file"]] + [e["file"] for e in sorted(
            extras.pop((mc, key), []), key=lambda e: e["file"]
        )]
        entries.append({
            "id": f"{mc}-{key}",
            "artifact": key,
            "mc": mc,
            "mod_version": item["mod_version"],
            "files": files,
            "files_input": "\n".join(f"dist/{f}" for f in files),
            "display_name": render(display_template, placeholders),
            "version": render(version_template, placeholders),
            "version_type": version_type,
            "game_versions": "\n".join(versions),
            "loaders": "\n".join(loaders),
            "dependencies": "\n".join(dependencies_for(cfg, mc, key, art, loaders)),
            "java": str(pick(art.get("java"), mc_entry(cfg, mc).get("java"), cfg.get("java")) or ""),
            "modrinth_id": resolve_platform_id("modrinth", cfg, art, repo_vars),
            "curseforge_id": resolve_platform_id("curseforge", cfg, art, repo_vars),
            "featured": str(platform_flag("modrinth", cfg, art, "featured", False)).lower(),
        })

    if not only_artifacts:
        for (mc, parent), orphans in extras.items():
            warn(
                f"{', '.join(o['file'] for o in orphans)}: parent artifact '{parent}' has no "
                f"jar for MC {mc} - not published"
            )

    entries.sort(key=lambda e: (e["mc"], e["artifact"]))
    if not entries:
        fail(
            "no publishable jars found on the release. Run the Release build workflow first, "
            "or relax the artifact/minecraft filters."
        )

    print(json.dumps(entries, indent=2))
    gh_output("matrix", json.dumps(entries, separators=(",", ":")))
    gh_output("count", str(len(entries)))
    gh_summary(
        "### Publish plan\n\n| version | name | game versions | loaders | files | modrinth | curseforge |\n"
        "|---|---|---|---|---|---|---|\n"
        + "\n".join(
            "| `{version}` | {display_name} | {gv} | {loaders} | {files} | {mr} | {cf} |".format(
                version=e["version"], display_name=e["display_name"],
                gv=e["game_versions"].replace("\n", ", "),
                loaders=e["loaders"].replace("\n", ", "),
                files="<br>".join(f"`{f}`" for f in e["files"]),
                mr="yes" if e["modrinth_id"] else "-",
                cf="yes" if e["curseforge_id"] else "-",
            )
            for e in entries
        )
    )


def cmd_validate(args):
    cfg = load_config(args.config)
    pattern = build_pattern(cfg)
    print(f"config:   {args.config}")
    print(f"name:     {cfg['name']}")
    print(f"pattern:  {pattern.pattern}")
    for key, art in cfg["artifacts"].items():
        target = "github release only" if art.get("publish") is False else (
            f"attached to '{art['attach_to']}'" if art.get("attach_to") else "published"
        )
        source = art.get("module") or ", ".join(art.get("files") or [])
        print(f"artifact: {key:<12} loaders={','.join(artifact_loaders(key, art)):<14} "
              f"src={source:<24} {target}")
    for mc in (cfg.get("minecraft") or {}):
        versions = game_versions_for(cfg, mc)
        print(f"mc:       {mc:<12} -> {', '.join(versions)} (range '{mc_range(versions)}')")
    for name in args.sample or []:
        parsed = parse_asset(pattern, name)
        print(f"sample:   {name} -> {json.dumps(parsed) if parsed else 'NO MATCH'}")


# -------------------------------------------------------------------------- cli

def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    subs = parser.add_subparsers(dest="command", required=True)

    p = subs.add_parser("branches", help="resolve branches.json against remote refs")
    p.add_argument("--config", default=".github/branches.json")
    p.add_argument("--refs", required=True, help="file with one branch name per line, or '-' for stdin")
    p.set_defaults(func=cmd_branches)

    p = subs.add_parser("collect", help="copy the built jars declared by the config")
    p.add_argument("--config", default=".github/publish.json")
    p.add_argument("--root", default=".", help="checkout to scan for build output")
    p.add_argument("--out", default="release-assets")
    p.add_argument("--label", default="", help="name used in the job summary")
    p.set_defaults(func=cmd_collect)

    p = subs.add_parser("matrix", help="build the publish matrix from release assets")
    p.add_argument("--config", default=".github/publish.json")
    p.add_argument("--assets", required=True, help="JSON array or newline list of asset names, or '-'")
    p.add_argument("--vars", default="", help="JSON object of repository variables")
    p.add_argument("--tag", default="")
    p.add_argument("--version-type", default="auto", choices=["auto", "release", "beta", "alpha"])
    p.add_argument("--artifacts", default="", help="comma-separated artifact keys to keep")
    p.add_argument("--minecraft", default="", help="comma-separated Minecraft versions to keep")
    p.set_defaults(func=cmd_matrix)

    p = subs.add_parser("validate", help="sanity-check the config")
    p.add_argument("--config", default=".github/publish.json")
    p.add_argument("--sample", nargs="*", help="jar names to test against the filename pattern")
    p.set_defaults(func=cmd_validate)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
