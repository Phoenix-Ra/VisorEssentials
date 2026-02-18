package me.phoenixra.visoressentials.core.client.gui.overlays;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreenInScreen;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.player.pose.PoseAnchor;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.component.ComponentPriority;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


public class VROverlayDraggedItem extends VROverlayScreen
        implements VREventListener {
    public static final String ID = "dragged_item";

    private Vector3f orientPosOffset = new Vector3f(0,0,-0.6f);
    private Vector3f orientRotationOffset = new Vector3f(0,0,0);

    public VROverlayDraggedItem(@NotNull VisorAddon owner,
                                @NotNull String id) {
        super(owner, id, ComponentPriority.HIGHER, 0.1f);
        setEnabled(true);
        cursorBoundsX = width/2 - 8;
        cursorBoundsY = height/2 - 8;
        cursorBoundsWidth = 16;
        cursorBoundsHeight = 16;
        VisorAPI.eventBus().registerListener(owner,this);
    }


    @VREventHandler
    public void disableWorldHands(AllowClientFeatureVREvent event){
        var featureToDisable = VisorAPI.client().getGuiManager().getCursorHandler()
                .getCursorHand() == HandType.MAIN
                ? ClientFeature.VR_WORLD_HAND_MAIN
                : ClientFeature.VR_WORLD_HAND_OFFHAND;
        if(event.getFeature() == featureToDisable) {
            if(isDraggingItem()){
                //To fix flickering on changing focus
                // from container/inventory to this overlay
                event.setCanceled(true);
            }
        }
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var keyboard = VisorAPI.client().getGuiManager().getOverlayManager()
                .getKeyboardAccessor();
        if(keyboard.isVisible()){
            keyboard.setVisible(false);
        }

        renderFloatingItem(
                guiGraphics,
                minecraft.player.containerMenu.getCarried(),
                width/2 - 8,height/2 - 8,
                null
        );
    }

    private void renderFloatingItem(GuiGraphics guiGraphics,
                                    ItemStack itemStack,
                                    int posX, int posY,
                                    String string) {
        guiGraphics.pose().pushPose();
        guiGraphics.renderItem(itemStack, posX, posY);
        guiGraphics.renderItemDecorations(
                this.font,
                itemStack,
                posX, posY, string
        );
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean updateVisibility() {
        if(!isDraggingItem()) {
            return false;
        }
        var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        if(supportsDragging(cursorHandler.getFocusedOverlay())){
            return false;
        }

        if(!VisorAPI.client().getVRLocalPlayer().getRawController(
                     cursorHandler.getCursorHand()
                ).isTracking()){
            return false;
        }

        if(isVisible()){
            var cursorResult  = cursorHandler.getCursorResult(
                    cursorHandler.getCursorHand(),
                    VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER),
                    it->it != this,
                    false
            );
            if(supportsDragging(cursorResult.focusedOverlay())){
                return false;
            }

        }
        return true;
    }


    @Override
    public void onUpdatePose(float partialTicks) {
        PoseAnchor anchor =  VisorAPI.client().getGuiManager().getCursorHandler()
                .getCursorHand() == HandType.MAIN
                ? PoseAnchor.MAIN_HAND
                : PoseAnchor.OFFHAND;
        VROverlayHelper.applyPose(
                this,
                anchor,
                anchor,
                1.0f,
                true,
                orientPosOffset,
                orientRotationOffset
        );
    }


    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(minecraft.player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu itemPickerMenu){
            if (i == 0) {
                this.minecraft.player.drop(itemPickerMenu.getCarried(), true);
                this.minecraft.gameMode.handleCreativeModeItemDrop(itemPickerMenu.getCarried());
                itemPickerMenu.setCarried(ItemStack.EMPTY);
            }

            if (i == 1) {
                ItemStack itemstack5 = itemPickerMenu.getCarried().split(1);
                this.minecraft.player.drop(itemstack5, true);
                this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack5);
            }
        }else {
            this.minecraft.gameMode.handleInventoryMouseClick(
                    minecraft.player.containerMenu.containerId,
                    -999, i, ClickType.PICKUP, this.minecraft.player
            );
        }
        return true;
    }
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    private boolean supportsDragging(VROverlay overlay){
        if(overlay != null
                && overlay.getId().equals("game_screen")){
            if(minecraft.screen instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        if(overlay instanceof VROverlayScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        if(overlay instanceof VROverlayTemplateScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        return false;
    }
    public static boolean isDraggingItem(){
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null
                && mc.player != null
                && mc.player.containerMenu != null
                && mc.player.containerMenu.getCarried() != null
                && !mc.player.containerMenu.getCarried().isEmpty();
    }
}
