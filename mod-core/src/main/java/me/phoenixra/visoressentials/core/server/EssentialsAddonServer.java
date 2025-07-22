package me.phoenixra.visoressentials.core.server;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class EssentialsAddonServer implements VisorAddon {
    public static final Logger LOGGER = LogManager.getLogger(VisorEssentials.MOD_NAME);

    public static boolean ACTIVE;

    @Override
    public void onAddonLoad() {
        ACTIVE = true;
    }

    @Override
    public @NotNull String getAddonId() {
        return VisorEssentials.MOD_ID;
    }

    @Override
    public String getModId() {
        return VisorEssentials.MOD_ID;
    }

}
