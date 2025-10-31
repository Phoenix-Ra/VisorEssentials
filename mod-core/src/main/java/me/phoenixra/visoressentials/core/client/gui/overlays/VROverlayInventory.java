package me.phoenixra.visoressentials.core.client.gui.overlays;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visoressentials.core.client.gui.screens.VRInvScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class VROverlayInventory extends VROverlayScreenInScreen<VRInvScreen> {
    public static final String ID = "inventory";

    protected final OverlayOptionsPose optionsPose;

    public VROverlayInventory(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id, null);
        setEnabled(true);
        optionsPose = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
    }



    @Override
    protected void onTick() {
        VROverlayHelper.applyPose(
                this,
                optionsPose.getPositionAnchor(),
                optionsPose.getRotationAnchor(),
                optionsPose.getScale(),
                optionsPose.isAimedRotation(),
                optionsPose.getPositionOffset(),
                optionsPose.getRotationOffset()
        );
        if(!isVisible()) return;

        var overlayContainer =
                VisorAPI.client().getGuiManager()
                        .getOverlayManager().getOverlay(
                                VROverlayContainer.ID,
                                VROverlayContainer.class
                        );
        AbstractContainerMenu menu =
                overlayContainer.isEnabled() ?
                        overlayContainer.getScreen().getMenu() : minecraft.player.inventoryMenu;

        if (screen == null) {
            screen = new VRInvScreen(menu, minecraft.player.getInventory());
            screen.init(minecraft, width, height);
        }else{
            boolean craftingAllowed = !overlayContainer.isEnabled();
            if (craftingAllowed != screen.isFullInventory()
                    || menu != screen.getMenu()) {
                screen = new VRInvScreen(menu, minecraft.player.getInventory());
                screen.init(minecraft, width, height);
            }
        }

        screen.tick();

        cursorBoundsX = screen.visorEssentials$getEdgeX();
        cursorBoundsY = screen.visorEssentials$getEdgeY();
        cursorBoundsWidth = screen.visorEssentials$getEdgeWidth();
        cursorBoundsHeight = screen.visorEssentials$getEdgeHeight();

    }

    @Override
    protected void onUpdatePose(float partialTicks) {
        VROverlayHelper.applyPose(
                this,
                optionsPose.getPositionAnchor(),
                optionsPose.getRotationAnchor(),
                optionsPose.getScale(),
                optionsPose.isAimedRotation(),
                optionsPose.getPositionOffset(),
                optionsPose.getRotationOffset()
        );
    }

    @Override
    public boolean updateVisibility() {
        if (!VisorAPI.client().getPlayer()
                .getControllerRaw(ControllerHand.OFFHAND)
                .isTracking()) {
            return false;
        }
        if(minecraft.screen != null){
            return false;
        }
        if (minecraft.isPaused()
                || minecraft.level == null
                || minecraft.player == null
                || minecraft.getEntityRenderDispatcher().camera == null) {
            return false;
        }
        if (VisorAPI.client().getGuiManager().getOverlayManager()
                .getKeyboardAccessor().isVisible()) {
            return false;
        }


        VRCursorHandler cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        boolean focused = cursorHandler.getFocusedOverlay() == this
                || isAimedAtOverlay(
                        VisorAPI.client().getPlayer()
                                .getPoseData(PoseDataType.RENDER)
                                .getHmd(),
                this,
                true,
                0.6f,
                1f
        );
        if(!focused){
            return false;
        }



        return true;
    }

    @Override
    public void onDisable() {
        if (screen != null) {
            screen.removed();
            screen = null;
        }
    }

    private boolean isAimedAtOverlay(@NotNull PoseElement element,
                                    @NotNull VROverlay overlay,
                                    boolean checkUpsideDown,
                                    float overlayBoundsExtraX,
                                    float overlayBoundsExtraY
    ) {

        var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        if (!cursorHandler.isFacingOverlay(
                element,
                overlay,
                checkUpsideDown
        )) {
            return false;
        }

        Vector3f newCursor = cursorHandler.findCursorPosition3D(
                element,
                overlay.getPose().getPosition(),
                overlay.getPose().getRotation(),
                overlay.getPose().getScale(),
                overlay.getAspectRatio()
        );
        if (overlayBoundsExtraX != 0 || overlayBoundsExtraY != 0) {
            float multX = overlayBoundsExtraX / 2;
            float multY = overlayBoundsExtraY / 2;
            float x = 0, y =0;

            if ((newCursor.x < 0.5 && newCursor.x >= -multX)
                    || (newCursor.x > 0.5 && newCursor.x <= 1 + multX)) {
                x = 0.5f;
            } else {
                x = newCursor.x;
            }
            if ((newCursor.y < 0.5 && newCursor.y >= -multY)
                    || (newCursor.y > 0.5 && newCursor.y <= 1 + multY)) {
                y = 0.5f;
            } else {
                y = newCursor.y;
            }
            newCursor = new Vector3f(x, y, 0);
        }


        return overlay.isWithinCursorBounds(
                newCursor.x,
                newCursor.y
        );
    }

    @Override
    public boolean supportsCursorIgnoreVisible() {
        return true;
    }


    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createOptions() {
        return List.of(
                new OverlayOptionsPose(
                        this,
                        it-> {
                            it.setTickPose(true);
                            it.setAimedRotation(false);
                            it.setPositionAnchor(PoseAnchor.OFFHAND);
                            it.setPositionOffset(
                                    -0.07f,
                                    -0.081f,
                                    0.2f
                            );
                            it.setRotationAnchor(PoseAnchor.OFFHAND);
                            it.setRotationOffset(
                                    0f,
                                    (float) (Math.PI/2),
                                    (float) Math.PI
                            );
                            it.setScale(0.5f);
                        }

                )
        );
    }
}
