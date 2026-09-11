package org.vmstudio.essentials.loader.neoforge.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.client.gui.ContainerSlot;


@Mixin(AbstractContainerScreen.class)
public abstract class NeoForgeContainerSlotPosMixin implements AbstractContainerScreenExtension {

    //? if >=1.20.4 {
    @Redirect(method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V", remap = false, require = 1,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;x:I", remap = true))
    private int visorEssentials$neoHighlightX(Slot instance) {
        ContainerSlot vrSlot = visorEssentials$getVRSlot(instance);
        return vrSlot != null ? vrSlot.vrPosX() : instance.x;
    }

    @Redirect(method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V", remap = false, require = 1,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I", remap = true))
    private int visorEssentials$neoHighlightY(Slot instance) {
        ContainerSlot vrSlot = visorEssentials$getVRSlot(instance);
        return vrSlot != null ? vrSlot.vrPosY() : instance.y;
    }

    @Redirect(method = "renderSlotContents", remap = false, require = 1,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;x:I", remap = true))
    private int visorEssentials$neoSlotContentsX(Slot instance) {
        ContainerSlot vrSlot = visorEssentials$getVRSlot(instance);
        return vrSlot != null ? vrSlot.vrPosX() : instance.x;
    }

    @Redirect(method = "renderSlotContents", remap = false, require = 1,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I", remap = true))
    private int visorEssentials$neoSlotContentsY(Slot instance) {
        ContainerSlot vrSlot = visorEssentials$getVRSlot(instance);
        return vrSlot != null ? vrSlot.vrPosY() : instance.y;
    }
    //?}
}
