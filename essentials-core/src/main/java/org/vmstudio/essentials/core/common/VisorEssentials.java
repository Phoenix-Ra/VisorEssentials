package org.vmstudio.essentials.core.common;

import me.phoenixra.atumconfig.api.ConfigLogger;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.essentials.core.client.AddonEntryClient;
import org.vmstudio.essentials.core.server.AddonEntryDedicatedServer;
import org.vmstudio.essentials.core.server.EssentialsServer;
import org.vmstudio.visor.api.ModLoader;

public abstract class VisorEssentials {
    public static final String MOD_ID = "visor_essentials";
    public static final String MOD_NAME = "VisorEssentials";



    public static EssentialsServer SERVER;

    private static ConfigManager CONFIG_MANAGER;


    public static void initConfigManager(@NotNull ConfigLogger logger){
        if (CONFIG_MANAGER != null) return;
        CONFIG_MANAGER = new AtumConfigManager(
                MOD_ID,
                ModLoader.get().getConfigFolder().toPath().resolve(MOD_NAME),
                logger,
                true
        );
    }

    public static boolean hasConfigManager(){
        return CONFIG_MANAGER != null;
    }

    public static @NotNull ConfigManager configManager(){
        if (CONFIG_MANAGER == null) {
            throw new IllegalStateException(
                    "VisorEssentials config manager is not initialized yet");
        }
        return CONFIG_MANAGER;
    }


    public static boolean isActive(){
        return AddonEntryClient.ACTIVE
                || AddonEntryDedicatedServer.ACTIVE;
    }
}
