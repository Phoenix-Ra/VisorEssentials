package me.phoenixra.visoressentials.core.client.gui.overlays;

import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.framework.VROverlayTemplateScreenInScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsLocation;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visoressentials.core.client.gui.screens.VRInvScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

@RegisterVROverlayTemplate(id = VROverlayTemplateInventory.ID, isCreateDefault = true)
public class VROverlayTemplateInventory extends VROverlayTemplateScreenInScreen<VRInvScreen> {
    public static final String ID = "inventory";

    public VROverlayTemplateInventory(@NotNull VisorAddon owner,
                                      @NotNull String id) {
        super(owner, id);
        setEnabled(true);
    }



    @Override
    protected void onTick() {
        if(!isVisible()) return;

        var overlayContainer =
                ClientContext.overlayManager.getOverlay(
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
            if (craftingAllowed != screen.isWithCrafting()
                    || menu != screen.getMenu()) {
                screen = new VRInvScreen(menu, minecraft.player.getInventory());
                screen.init(minecraft, width, height);
            }
        }

        screen.tick();

        cursorEdgeX = screen.visorEssentials$getEdgeX();
        cursorEdgeY = screen.visorEssentials$getEdgeY();
        cursorEdgeWidth = screen.visorEssentials$getEdgeWidth();
        cursorEdgeHeight = screen.visorEssentials$getEdgeHeight();

    }

    @Override
    public boolean updateVisibility() {
        if (!ClientContext.rawPoseHandler
                .getControllerData(ControllerHand.OFFHAND)
                .isTracking()) {
            return false;
        }
        if(minecraft.screen instanceof AbstractContainerScreen<?>){
            return false;
        }
        if (minecraft.isPaused()
                || minecraft.level == null
                || minecraft.player == null
                || minecraft.getEntityRenderDispatcher().camera == null) {
            return false;
        }
        if (ClientContext.overlayManager.getKeyboardAccessor().isVisible()) {
            return false;
        }

        VRCursorHandler cursorHandler = ClientContext.cursorHandler;
        boolean focused = cursorHandler.getFocusedOverlay() == this
                || isAimedAtOverlay(
                        ClientContext.player
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

        var cursorHandler = ClientContext.cursorHandler;
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
                overlay.getPose().getScale()
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


        return overlay.isCursorWithinBounds(
                true,
                newCursor.x,
                newCursor.y
        );
    }

    @Override
    public boolean supportsCursorIgnoreVisible() {
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptions> createOptions() {
        return List.of(
                new OverlayOptionsGlobal(
                        this,
                        it->{
                            it.setUpdateOptionsType(OverlayOptionsGlobal.UpdateOptionsType.TICK);
                            it.setFormulaOverlayScale("0.5");
                        }
                ),
                new OverlayOptionsLocation(
                        this,
                        it-> {
                            it.setTickModelView(true);
                            it.setAimRotation(false);
                            it.setPositionAnchor(PoseAnchor.OFFHAND);
                            it.setFormulaPosX("-0.07");
                            it.setFormulaPosY("-0.081");
                            it.setFormulaPosZ("0.2");
                            it.setRotationAnchor(PoseAnchor.OFFHAND);
                            it.setFormulaRotationX(null);
                            it.setFormulaRotationY("pi/2");
                            it.setFormulaRotationZ("pi");
                        }

                )
        );
    }
}
