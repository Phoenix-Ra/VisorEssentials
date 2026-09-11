package org.vmstudio.essentials.core.mixin.client.gui.containers;

import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.client.gui.RecipeBookButton;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin <T extends AbstractFurnaceMenu>
        extends AbstractContainerScreen<T>
        implements RecipeUpdateListener, AbstractContainerScreenExtension {

    @Shadow @Final
    public AbstractFurnaceRecipeBookComponent recipeBookComponent;
    @Shadow private boolean widthTooNarrow;

    @Unique
    private ResourceLocation visorEssentials$VrTexture;

    public AbstractFurnaceScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void visorEssentials$onInit(CallbackInfo ci) {
        if(((Object)this) instanceof BlastFurnaceScreen) {
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/blast_furnace.png"
            );
        } else if (((Object)this) instanceof SmokerScreen) {
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/smoker.png"
            );
        } else if(((Object)this) instanceof FurnaceScreen){
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/furnace.png"
            );
        }
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 86;
    }

    @Inject(method = "init", at = @At("TAIL"))
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


    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", ordinal = 0))
    private void visorEssentials$background(GuiGraphics instance, ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight){
        if(visorEssentials$isVRContainer()){
            instance.blit(visorEssentials$VrTexture, x, y, uOffset, vOffset, uWidth, vHeight);
            return;
        }
        instance.blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractFurnaceScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener visorEssentials$recipeBook(AbstractFurnaceScreen instance, GuiEventListener guiEventListener){

        if(visorEssentials$isVRContainer()){
            return addRenderableWidget(RecipeBookButton.create(this.leftPos + 20, topPos + 35, (arg) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                arg.setPosition(this.leftPos + 20, topPos + 35);
            }));
        }
        return addRenderableWidget((ImageButton)guiEventListener);
    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return visorEssentials$VrTexture != null;
    }
}
