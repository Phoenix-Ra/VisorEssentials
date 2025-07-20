package me.phoenixra.visoressentials.core.mixin.client.gui.containers;

import me.phoenixra.visoressentials.core.client.mcmodified.AbstractContainerScreenModified;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin extends ItemCombinerScreen<AnvilMenu> implements AbstractContainerScreenModified {
    @Unique
    private ResourceLocation visorEssentials$VrTexture = new ResourceLocation(
            VisorEssentials.MOD_ID,
            "textures/gui/container/smithing.png"
    );

    public SmithingScreenMixin(AnvilMenu menu, Inventory playerInventory, Component title, ResourceLocation menuResource) {
        super(menu, playerInventory, title, menuResource);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void visorEssentials$onInit(SmithingMenu menu, Inventory playerInventory, Component title, CallbackInfo ci){
        imageWidth = 176;
        imageHeight = 82;
    }

    @Inject(method = "subInit", at = @At("TAIL"))
    private void visorEssentials$onInit(CallbackInfo ci){
        visorEssentials$setEdgeX(leftPos);
        visorEssentials$setEdgeY(topPos);
        visorEssentials$setEdgeWidth(imageWidth);
        visorEssentials$setEdgeHeight(imageHeight);
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/ItemCombinerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void visorEssentials$background(ItemCombinerScreen instance, GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY){
        if(visorEssentials$isVRContainer()) {
            guiGraphics.blit(visorEssentials$VrTexture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
            this.renderErrorIcon(guiGraphics, this.leftPos, this.topPos);
            return;
        }
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }
}
