package me.phoenixra.visoressentials.core.client.gui.overlays;

import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.NotNull;

public class VROverlayContainer extends VROverlayScreenInScreen<AbstractContainerScreen<?>> {

    public static final String ID = "container";

    public VROverlayContainer(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id, null);
    }

    @Override
    protected boolean updateVisibility() {
        return false;
    }

    @Override
    public void updatePose(float v) {

    }
}
