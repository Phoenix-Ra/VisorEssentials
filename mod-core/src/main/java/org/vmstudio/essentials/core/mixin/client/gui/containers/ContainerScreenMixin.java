package org.vmstudio.essentials.core.mixin.client.gui.containers;

import org.vmstudio.essentials.core.client.mcmodified.AbstractContainerScreenModified;
import org.vmstudio.essentials.core.common.VisorEssentials;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin
        extends AbstractContainerScreen<ChestMenu>
        implements AbstractContainerScreenModified {

    @Shadow @Final private int containerRows;


    @Unique
    private ResourceLocation visorEssentials$VrTexture = new ResourceLocation(
            VisorEssentials.MOD_ID,
            "textures/gui/container/generic_54.png"
    );



    public ContainerScreenMixin(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 34 + this.containerRows * 18;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void visorEssentials$updateEdges(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci){
        visorEssentials$setEdgeX(leftPos);
        visorEssentials$setEdgeY(topPos);
        visorEssentials$setEdgeWidth(imageWidth);
        visorEssentials$setEdgeHeight(imageHeight);
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void visorEssentials$background(GuiGraphics instance, ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight){
        if(visorEssentials$isVRContainer()){
            instance.blit(visorEssentials$VrTexture, x, y, uOffset, vOffset, uWidth, vHeight);
            return;
        }
        instance.blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }
}
