package org.vmstudio.essentials.core.server;

import net.minecraft.client.Minecraft;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.essentials.core.common.VisorEssentials;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class AddonEntryDedicatedServer implements VisorAddon {
    public static final Logger LOGGER = LogManager.getLogger(VisorEssentials.MOD_NAME);

    public static boolean ACTIVE;

    @Override
    public void onAddonLoad() {
        VisorEssentials.MC = Minecraft.getInstance();

        EssentialsChannel.createChannel(this);

        VisorEssentials.SERVER = new EssentialsServer();
        VisorAPI.eventBus().registerListener(this, VisorEssentials.SERVER);

        ACTIVE = true;
    }

    @Override
    public @NotNull String getAddonId() {
        return VisorEssentials.MOD_ID;
    }

    @Override
    public @NotNull Component getAddonName() {
        return Component.literal(VisorEssentials.MOD_NAME);
    }

    @Override
    public String getModId() {
        return VisorEssentials.MOD_ID;
    }

}
