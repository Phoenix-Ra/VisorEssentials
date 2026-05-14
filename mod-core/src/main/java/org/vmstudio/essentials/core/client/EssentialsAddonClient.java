package org.vmstudio.essentials.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayDraggedItem;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayInventory;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

import java.util.List;

public class EssentialsAddonClient implements VisorAddon {
    public static final Logger LOGGER = LogManager.getLogger(VisorEssentials.MOD_NAME);

    public static boolean ACTIVE;


    @Override
    public void onAddonLoad() {
        VisorEssentials.MC = Minecraft.getInstance();

        EssentialsChannel.createChannel(this);

        VisorAPI.addonManager().getRegistries().overlays()
                .registerComponents(List.of(
                        new VROverlayDraggedItem(this, VROverlayDraggedItem.ID),
                        new VROverlayContainer(this, VROverlayContainer.ID),
                        new VROverlayInventory(this, VROverlayInventory.ID)
                ));


        ACTIVE = true;
    }

    @Override
    public @Nullable String getAddonPackagePath() {
        return "org.vmstudio.essentials.core.client";
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