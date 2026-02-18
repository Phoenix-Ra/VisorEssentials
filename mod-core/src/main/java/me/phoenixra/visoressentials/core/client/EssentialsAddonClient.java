package me.phoenixra.visoressentials.core.client;


import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visoressentials.core.client.gui.overlays.VROverlayContainer;
import me.phoenixra.visoressentials.core.client.gui.overlays.VROverlayDraggedItem;
import me.phoenixra.visoressentials.core.client.gui.overlays.VROverlayInventory;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EssentialsAddonClient implements VisorAddon {
    public static final Logger LOGGER = LogManager.getLogger(VisorEssentials.MOD_NAME);

    public static boolean ACTIVE;

    @Override
    public void onAddonLoad() {
        VisorAPI.addonManager().getRegistries().overlays()
                .registerComponents(
                        List.of(
                                new VROverlayDraggedItem(
                                        this,
                                        VROverlayDraggedItem.ID
                                ),
                                new VROverlayContainer(
                                        this, VROverlayContainer.ID
                                ),
                                new VROverlayInventory(
                                        this, VROverlayInventory.ID
                                )
                        )
                );
        ACTIVE = true;

    }

    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visoressentials.core.client";
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
