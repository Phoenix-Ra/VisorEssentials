package org.vmstudio.essentials.core.mixin.client.gui.containers;

import org.vmstudio.essentials.core.client.mcmodified.AbstractContainerScreenModified;
import org.vmstudio.essentials.core.common.VisorEssentials;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin
        extends AbstractContainerScreen<CraftingMenu>
        implements AbstractContainerScreenModified {

    @Shadow @Final private static ResourceLocation RECIPE_BUTTON_LOCATION;
    @Shadow @Final
    private RecipeBookComponent recipeBookComponent;

    @Shadow private boolean widthTooNarrow;

    @Unique
    private ResourceLocation visorEssentials$VrTexture = new ResourceLocation(
            VisorEssentials.MOD_ID,
            "textures/gui/container/crafting_table.png"
            );



    public CraftingScreenMixin(CraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 86;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void visorEssentials$updateEdges(CallbackInfo ci){
        if(recipeBookComponent.isVisible() && !this.widthTooNarrow){
            visorEssentials$setEdgeX(-1);
            visorEssentials$setEdgeY(-1);
            visorEssentials$setEdgeWidth(-1);
            visorEssentials$setEdgeHeight(-1);
        }else {
            visorEssentials$setEdgeX(leftPos);
            visorEssentials$setEdgeY(topPos);
            visorEssentials$setEdgeWidth(imageWidth);
            visorEssentials$setEdgeHeight(imageHeight);
        }


    }


    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void visorEssentials$background(GuiGraphics instance, ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight){
        if(visorEssentials$isVRContainer()){
            instance.blit(visorEssentials$VrTexture, x, y, uOffset, vOffset, uWidth, vHeight);
            return;
        }
        instance.blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CraftingScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener visorEssentials$recipeBook(CraftingScreen instance, GuiEventListener guiEventListener){

        if(visorEssentials$isVRContainer()){
            return addRenderableWidget(new ImageButton(this.leftPos + 5, /*modified*/topPos + 35 , 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (arg) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                arg.setPosition(this.leftPos + 5, /*modified*/topPos + 35);
            }));
        }
        return addRenderableWidget((ImageButton)guiEventListener);
    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }

}
