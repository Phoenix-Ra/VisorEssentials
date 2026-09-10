package org.vmstudio.essentials.core.server;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.essentials.core.common.VisorEssentials;

import java.nio.file.Path;
import java.util.List;


public final class EssentialsServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(VisorEssentials.MOD_NAME);

    private static final String KEY_BETTER_BOW = "betterBow";

    private EssentialsServerConfig() {}

    public static void onServerInit(){
        EssentialsServerSettings.resetToDefaults();
        try {
            ConfigFile config = VisorEssentials.configManager().createConfigFile(
                    ConfigType.YAML,
                    "server_settings",
                    Path.of("server_settings.yml"),
                    false
            );
            updateSettings(config);
            applySettingsTo(config);
            config.save();
        } catch (Exception e) {
            LOGGER.error("Failed to load VisorEssentials server settings", e);
        }
    }

    public static void updateSettings(@NotNull ConfigManager configManager,
                                      @NotNull String configString){
        updateSettings(configManager.createConfigFromString(
                ConfigType.YAML,
                configString
        ));
    }

    public static void updateSettings(@NotNull Config config){
        EssentialsServerSettings.setBetterBow(
                config.getBoolOrDefault(KEY_BETTER_BOW,
                        EssentialsServerSettings.isBetterBow())
        );
    }

    public static void applySettingsTo(@NotNull Config config){
        config.set(KEY_BETTER_BOW, EssentialsServerSettings.isBetterBow());
        config.setComments(KEY_BETTER_BOW, List.of(
                "Allow VR players to use the roomscale bow",
                "(arrow power is taken from the real draw distance)"
        ));
    }

    public static @NotNull Config getSettingsForClient(){
        Config config = VisorEssentials.configManager()
                .createConfig(ConfigType.YAML, null);
        config.set(KEY_BETTER_BOW, EssentialsServerSettings.isBetterBow());
        return config;
    }
}
