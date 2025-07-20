package me.phoenixra.visoressentials.core.mixin.client.gui.containers;

import me.phoenixra.visoressentials.core.client.mcmodified.AbstractContainerScreenModified;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin
        extends AbstractContainerScreen<MerchantMenu>
        implements AbstractContainerScreenModified {
    @Unique
    private ResourceLocation visorEssentials$VrTexture = new ResourceLocation(
            VisorEssentials.MOD_ID,
            "textures/gui/container/villager.png"
    );

    public MerchantScreenMixin(MerchantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 276;
        imageHeight = 166;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void visorEssentials$updateEdges(CallbackInfo ci){
        visorEssentials$setEdgeX(leftPos);
        visorEssentials$setEdgeY(topPos);
        visorEssentials$setEdgeWidth(imageWidth);
        visorEssentials$setEdgeHeight(imageHeight);
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIFFIIII)V", ordinal = 0))
    private void visorEssentials$background(GuiGraphics instance, ResourceLocation atlasLocation, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight){
        if(visorEssentials$isVRContainer()){
            instance.blit(visorEssentials$VrTexture, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
            return;
        }
        instance.blit(atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
    }


    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }
}
