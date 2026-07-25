# Release & publish pipeline

Three workflows, driven entirely by two JSON files. Nothing mod-specific lives in the
workflows or in `scripts/modci.py`, so porting to another mod is: copy the files, edit
the JSON.

| Workflow | Trigger | Does |
|---|---|---|
| `release.yml` | release published, or manual with a tag | builds every branch in `branches.json`, attaches the jars declared in `publish.json` to the release |
| `publish.yml` | manual with a tag | reads the release assets, resolves a publish plan, uploads to Modrinth / CurseForge |
| `discover.yml` | called by `release.yml` | resolves `branches.json` against the remote branches |

`scripts/modci.py` (stdlib-only Python) holds the logic and can be run locally.

## Porting to another mod

1. Copy `.github/workflows/{discover,release,publish}.yml`, `.github/scripts/modci.py`,
   `.github/branches.json`, `.github/publish.json`.
2. Edit `publish.json`: `name`, `artifacts` (module paths + loaders), `minecraft`,
   `dependencies`.
3. Edit `branches.json`: which branches ship, and the JDK each one needs.
4. Set the repository variables `MODRINTH_ID` / `CURSEFORGE_ID` and the secrets
   `MODRINTH_TOKEN` / `CURSEFORGE_TOKEN`.
5. Check it: `python .github/scripts/modci.py validate --sample <a real jar name>`.

The only hard requirement on the mod itself is the jar naming convention below.

## Jar naming

Assets are matched as `<archives_name>-<version>+mc<mc>-<artifact>.jar`, e.g.
`Visor-0.5.0-snapshot-1+mc1.20.1-api-fabric.jar`. `<artifact>` must be a key of
`artifacts`, `<archives_name>` defaults to `name`. In each platform module:

```groovy
version = "${rootProject.version}+mc${libs.versions.minecraftVersion.get()}-fabric"
```

`-dev`, `-sources`, `-javadoc` and `-all` jars are ignored. A mod that names jars
differently sets `filename_regex` in `publish.json` instead — any regex with
`(?P<version>…)`, `(?P<mc>…)` and `(?P<artifact>…)` groups works.

## `publish.json`

| Field | Default | Meaning |
|---|---|---|
| `name` | required | project name, used by `{name}` and to match jar names |
| `archives_name` | `name` | gradle `archivesBaseName`, if it differs from `name`. `"*"` matches anything |
| `display_name` | `{name} {version} - MC {mc_range} ({loader_names})` | version title on Modrinth/CurseForge |
| `version_pattern` | `{version}+mc{mc}-{artifact}` | version number on Modrinth/CurseForge |
| `artifacts` | required | what is built and shipped, see below |
| `minecraft` | `{}` | per-Minecraft-version settings, see below |
| `dependencies` | `{}` | dependency lists keyed by artifact, loader, or `*` |
| `modrinth` / `curseforge` | `{}` | `{"id": "…"}` or `{"id_var": "SOME_REPO_VARIABLE"}`, plus `featured` for Modrinth |
| `loader_names` | Fabric/Quilt/Forge/NeoForge | pretty names used by `{loader_names}` |
| `version_type_rules` | `alpha: *snapshot*, *alpha*` / `beta: *pre*, *rc*, *beta*` | how `version_type: auto` picks a channel |
| `jar_excludes` | `*-dev.jar`, `*-sources.jar`, `*-javadoc.jar`, `*-all.jar` | never collected |
| `java` | — | Java version tag sent to CurseForge |

### `artifacts`

Every artifact listed here is built and attached to the GitHub release; `publish: false`
is what keeps one off Modrinth/CurseForge. The key is the jar-name suffix, everything
else is optional:

```jsonc
"artifacts": {
  "fabric": {
    "module": "visor-fabric",          // <module>/build/libs/*.jar; or use "files": ["glob", …]
    "loaders": ["fabric", "quilt"],    // defaults to [<key>]
    "label": "",                       // free text for {label}
    "display_name": "…",               // overrides the global template
    "version_pattern": "…",
    "dependencies": ["…"],             // overrides the global/per-mc lists
    "publish": true,                   // false = attach to the GitHub release only
    "attach_to": "fabric",             // ship as an extra file of another artifact's version
    "modrinth": { "id_var": "MODRINTH_API_ID" },   // publish to a different project
    "exclude": ["*-shaded.jar"],
    "skip": false                      // don't even collect it
  }
}
```

Three ways to handle a secondary artifact such as an API jar:

- `"publish": false` — GitHub release only (what this repo does).
- `"attach_to": "fabric"` — uploaded as an additional file on the main Fabric version.
- own `modrinth`/`curseforge` ids — published as a separate project.

### `minecraft`

Fixes the "built on 1.20.1, but also runs on 1.20" problem — `game_versions` is what
gets published, the map key is what the jar was built against:

```jsonc
"minecraft": {
  "1.20.1": ["1.20", "1.20.1"],                      // shorthand
  "1.21.1": {                                        // full form
    "game_versions": ["1.21", "1.21.1"],
    "java": "21",
    "display_name": "…",
    "dependencies": { "fabric": ["fabric-api(required)"] }
  }
}
```

An unlisted Minecraft version warns and publishes for itself only.
`{mc_range}` renders `1.20-1.20.1`, `{game_versions}` renders `1.20, 1.20.1`.

### `dependencies`

[mc-publish syntax](https://github.com/Kir-Antipov/mc-publish#dependencies):
`<slug>(required|recommended|optional|embedded|conflicting|incompatible)`, optionally
`{modrinth:ID}{curseforge:ID}` and `#(ignore:github)`.

```jsonc
"dependencies": {
  "*":          ["architectury-api(required)"],                   // added to every artifact
  "fabric":     ["fabric-api(required)", "sodium(optional)"],     // matched by loader
  "quilt":      ["qsl(required)"],                                // only for a quilt-only artifact
  "forge":      ["embeddium(optional)", "optifine(incompatible)"],
  "api-fabric": ["visor(required)"]                               // matched by artifact name
}
```

Keys are matched against the artifact key first, then its loaders, then `*`. The final
list is `*` entries + the most specific match, resolved
`artifacts.<key>.dependencies` > `minecraft.<mc>.dependencies` > `dependencies`.

The slug is the one in the project URL. Add explicit ids when the platforms disagree,
and `#(ignore:<platform>)` to skip one:

```jsonc
"sodium(optional){modrinth:AANobbMI}{curseforge:394468}"
"replaymod(optional)#(ignore:curseforge)"
```

An empty list means "let mc-publish infer dependencies from `fabric.mod.json` /
`mods.toml`" — declare them explicitly if you don't want that.

### Name placeholders

`{name}` `{version}` `{mc}` `{mc_range}` `{game_versions}` `{artifact}` `{label}`
`{loader}` `{loaders}` `{loader_names}` `{tag}` `{version_type}`

## `branches.json`

```jsonc
{
  "defaults": { "java": "17", "gradle_task": "build" },
  "branches": [
    "dev",
    { "pattern": "mc/1.21.*", "java": "21", "gradle_args": ["-x", "test"] },
    "mc/*"
  ]
}
```

Entries are branch names or globs; the first entry that matches a branch wins, so list
specific patterns before wide ones. Per-branch keys: `java`, `java_distribution`,
`gradle_task`, `gradle_args`. A plain `["dev", "mc/*"]` array still works.

## Publishing

`Publish to Modrinth / CurseForge` → Run workflow:

| Input | Notes |
|---|---|
| `tag` | release to publish |
| `modrinth` / `curseforge` | per-platform toggles |
| `version_type` | `auto` derives the channel from the mod version |
| `artifacts` | comma-separated filter, e.g. `fabric` |
| `minecraft` | comma-separated filter, e.g. `1.20.1` |
| `dry_run` | resolve the plan, download the jars, upload nothing |

The plan (name, version, game versions, loaders, dependencies, files per entry) is
printed as a table in the job summary before anything is uploaded. Entries publish one
at a time to stay under the CurseForge rate limit. The changelog is the release body.

## Local checks

```bash
python .github/scripts/modci.py validate --sample "Visor-0.5.0+mc1.20.1-fabric.jar"
python .github/scripts/modci.py collect --out /tmp/assets
ls /tmp/assets | python .github/scripts/modci.py matrix --assets -
```
