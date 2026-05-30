package org.vmstudio.essentials.core.client.gui.overlays;

import org.vmstudio.essentials.core.client.tasks.BowItemTask;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.gui.CursorFocusChangedVREvent;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.essentials.core.client.gui.screens.VRInvScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class VROverlayInventory extends VROverlayScreenInScreen<VRInvScreen> implements VREventListener {
    public static final String ID = "inventory";

    protected final OverlayOptionsPose optionsPose;

    public VROverlayInventory(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id, null);
        optionsPose = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
        setEnabled(true);
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void disableWorldHands(CursorFocusChangedVREvent event){
        var usedHand = getUsedHand();
        if(usedHand == null
                || event.getHand() != usedHand
                || event.getNewOverlay() == null){
            return;
        }
        //don't focus hand that is used by inventory
        if(isVisible()){
            event.setCanceled(true);
        }
    }

    @Override
    protected void onPreTick() {
        if(isCanBeVisible()) {
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
    }

    @Override
    protected void onTick() {
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
        if(!isCanBeVisible()){
            return false;
        }

        var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        var cursorHand = cursorHandler.getCursorHand();
        var focusedOverlay = cursorHandler.getFocusedOverlay();
        boolean focused = focusedOverlay == this
                || isAimedAtOverlay(
                VisorAPI.client().getVRLocalPlayer()
                        .getPoseData(PlayerPoseType.RENDER)
                        .getHand(cursorHand),
                this,
                false,
                0f,
                0f
        ) || isAimedAtOverlay(
                        VisorAPI.client().getVRLocalPlayer()
                                .getPoseData(PlayerPoseType.RENDER)
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

    private boolean isCanBeVisible(){
        if (!VisorEssentials.customInventory) {
            return false;
        }
        if (!VisorAPI.client().getVRLocalPlayer()
                .getRawController(HandType.OFFHAND)
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
        if(BowItemTask.getInstance().isNotched()){
            return false;
        }
        return true;
    }

    @Override
    protected void onVisibilityChanged() {
        var usedHand = getUsedHand();
        VisorAPI.client().getGuiManager().getCursorHandler()
                .clearFocus(usedHand);
    }

    @Override
    public void onDisable() {
        if (screen != null) {
            screen.removed();
            screen = null;
        }
    }

    private boolean isAimedAtOverlay(@NotNull VRPose vrPose,
                                    @NotNull VROverlay overlay,
                                    boolean checkUpsideDown,
                                    float overlayBoundsExtraX,
                                    float overlayBoundsExtraY
    ) {

        var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        if (!cursorHandler.isFacingOverlay(
                vrPose,
                overlay,
                checkUpsideDown
        )) {
            return false;
        }

        Vector3f newCursor = cursorHandler.findCursorPosition3D(
                vrPose,
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

    public HandType getUsedHand(){
        return switch(optionsPose.getPositionAnchor()){
            case MAIN_HAND -> HandType.MAIN;
            case OFFHAND -> HandType.OFFHAND;
            default -> null;
        };
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
