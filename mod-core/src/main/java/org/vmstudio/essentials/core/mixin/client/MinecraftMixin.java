package org.vmstudio.essentials.core.mixin.client;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.mcmodified.AbstractContainerScreenModified;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Final
    public Options options;

    @Shadow
    public HitResult hitResult;

    @Shadow
    public Screen screen;

    /**
     * Replaces vanilla container screen with overlay
     *
     * @param screen s
     * @param info   s
     */
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void visor$UseVRContainerScreen(Screen screen, CallbackInfo info) {
        if(VisorAPI.clientState().stateMode().isNotActive()) return;

        // we need containers attached to entity or block,
        // otherwise display it vanilla way
        if(hitResult == null
                || hitResult.getType() == HitResult.Type.MISS){
            return;
        }

        // if already have screen opened, don't use container overlay,
        // This approach helps with server GUIs support and just more stable
        if (this.screen != null) {
            return;
        }

        if (!(screen instanceof InventoryScreen)
                && !(screen instanceof CreativeModeInventoryScreen)
                && (screen instanceof AbstractContainerScreen<?> containerScreen)) {
           boolean supportsVR = ((AbstractContainerScreenModified)containerScreen)
                   .visorEssentials$supportsVRContainer();
            if(!supportsVR){
                return;
            }
            info.cancel();


            var overlayContainer = VisorAPI.client().getGuiManager()
                    .getOverlayManager()
                    .getOverlay(
                            "container",
                            VROverlayContainer.class
                    );

            overlayContainer.openMenu(
                    containerScreen
            );
        }
    }


    /**
     * Disable mouse bindings when focused at overlay.
     * &
     * Closes container overlay
     * if player has it active
     * and hitResult is an entity or block
     * which is the container holder
     *
     * @param ci s
     */
    //@TODO move logic to VR input
    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true)
    private void visor$mouseAndOverlays(CallbackInfo ci) {
        if(VisorAPI.clientState().stateMode().isNotActive()) return;

        if (VisorAPI.client().getGuiManager().getCursorHandler().getFocusedOverlay() != null) {
            ci.cancel();
        }
        var container = VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getOverlay("container", VROverlayContainer.class);


        if (System.currentTimeMillis() < container.getLastManualClose() + 200) {
            // consume all key usages
            // if too small-time left after overlay close
            // it fixes accidental opening of same menu
            while (this.options.keyUse.consumeClick()) {
            }
            ci.cancel();
            return;
        }
        if (container.isEnabled()) {
            boolean isBlock = hitResult != null
                    && (hitResult.getType() == HitResult.Type.BLOCK);
            boolean isEntity = hitResult instanceof EntityHitResult
                    && (((EntityHitResult) hitResult).getEntity() instanceof ContainerEntity
                    || ((EntityHitResult) hitResult).getEntity() instanceof InventoryCarrier
                    || ((EntityHitResult) hitResult).getEntity() instanceof HasCustomInventoryScreen);

            if (isEntity) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();
                if (container.getSourceEntity() == entity) {
                    boolean b = false;
                    // consume all key usages
                    // if too small-time left after overlay close
                    // it fixes accidental opening of same menu
                    while (this.options.keyUse.consumeClick()) {
                        b = true;
                    }
                    if (b) {
                        //close container attached to an entity
                        container.setLastManualClose(System.currentTimeMillis());
                        container.setEnabled(false);
                        ci.cancel();
                    }
                }
            } else if (isBlock) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (container.isAttachedTo(blockPos)) {
                    boolean b = false;
                    //consume all key usages
                    // if too small-time left after overlay close
                    //if fixes accidental opening of same menu
                    while (this.options.keyUse.consumeClick()) {
                        b = true;
                    }
                    if (b) {
                        //close container attached to block
                        container.setLastManualClose(System.currentTimeMillis());
                        container.setEnabled(false);
                        ci.cancel();
                    }
                }
            }

        }
    }

}
