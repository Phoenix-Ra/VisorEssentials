package me.phoenixra.visoressentials.core.client.gui.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visoressentials.core.client.gui.ContainerSlot;
import me.phoenixra.visoressentials.core.client.gui.overlays.VROverlayContainer;
import me.phoenixra.visoressentials.core.client.mcmodified.AbstractContainerScreenModified;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;


public class VRInvScreen extends VRInvEffectInvScreen implements AbstractContainerScreenModified {
    private ResourceLocation IMAGE_FULL = new ResourceLocation(
            VisorEssentials.MOD_ID,"textures/gui/inventory.png"
    );
    private ResourceLocation IMAGE_SIMPLIFIED = new ResourceLocation(
            VisorEssentials.MOD_ID,"textures/gui/inventory_simplified.png"
    );


    private float xMouse;
    private float yMouse;

    private boolean buttonClicked;

    public VRInvScreen(AbstractContainerMenu menu,
                       Inventory inventory) {
        super(menu,  inventory, Component.literal(""));
        this.titleLabelX = 97;
        this.imageWidth = 258;
        this.imageHeight = 156;
        visorEssentials$setVRContainer(true);
    }
    @Override
    public void visorEssentials$fillVRSlots(
            @NotNull List<ContainerSlot> slots
    ) {
        fullInventory = !VisorAPI.client().getGuiManager()
                .getOverlayManager().getOverlay(VROverlayContainer.ID).isEnabled();

        slots.clear();
        for(Slot slot : menu.slots){
            int posX = 0;
            int posY = 0;
            if((slot.container instanceof CraftingContainer)
                    && fullInventory){
                // 2x2 grid layout
                int index = slot.getContainerSlot();
                int row = index / 2;
                int col = index % 2;
                posX = 181 + col * 18;
                posY = 26 + row * 18;
                slots.add(new ContainerSlot(slot, posX,posY));
            }else if(slot.container instanceof Inventory){
                if(slot.getContainerSlot()<=8){
                    //hotbar
                    switch (slot.getContainerSlot()){
                        case 0 -> {
                            posX = 121;
                            posY = 38;
                        }
                        case 1 -> {
                            posX = 121;
                            posY = 11;
                        }
                        case 2 -> {
                            posX = 148;
                            posY = 11;
                        }
                        case 3 -> {
                            posX = 148;
                            posY = 38;
                        }
                        case 4 -> {
                            posX = 148;
                            posY = 65;
                        }
                        case 5 -> {
                            posX = 121;
                            posY = 65;
                        }
                        case 6 -> {
                            posX = 94;
                            posY = 65;
                        }
                        case 7 -> {
                            posX = 94;
                            posY = 38;
                        }
                        case 8 -> {
                            posX = 94;
                            posY = 11;
                        }
                    }

                }else if(slot.getContainerSlot()<=35) {
                    // 9x3 grid layout
                    int index = slot.getContainerSlot() - 9;
                    int row = index / 9;
                    int col = index % 9;
                    posX = 49 + col * 18;
                    posY = 96 + row * 18;
                }else{
                    if(!fullInventory) continue;
                    // equipment slots
                    if(slot.getContainerSlot() == 40) continue; //ignore offhand
                    int index = slot.getContainerSlot() - 36;
                    posX = 8;
                    posY = 62 + index * -18;
                }
                slots.add(new ContainerSlot(slot,posX,posY));
            }
            else if(fullInventory && slot instanceof ResultSlot){
                posX = 237;
                posY = 36;
                slots.add(new ContainerSlot(slot, posX,posY));
            }
        }
    }
    @Override
    public void containerTick() {
        // [-- Modified
        /*if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), (Boolean)this.minecraft.options.operatorItemsTab().get()));
        } else {
            this.recipeBookComponent.tick();
        }*/
        // --]
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // [-- Modified
       /* this.renderBackground(guiGraphics);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, partialTick);
        }*/
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        //this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        // --]

        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;

        // [-- Modified
        //guiGraphics.blit(INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        guiGraphics.blit(fullInventory
                        ? IMAGE_FULL : IMAGE_SIMPLIFIED,
                width/2-258/2, height/2-156/2,
                0, 0.0F, 0.0F,
                258, 156,
                258,156
        );

        if(fullInventory) {
            renderEntityInInventoryFollowsMouse(guiGraphics, i + 51, j + 75, 30, (float) (i + 51) - this.xMouse, (float) (j + 75 - 50) - this.yMouse, this.minecraft.player);
        }
        // --]
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float)Math.atan(mouseX / 40.0F);
        float g = (float)Math.atan(mouseY / 40.0F);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(g * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf2);
        float h = entity.yBodyRot;
        float i = entity.getYRot();
        float j = entity.getXRot();
        float k = entity.yHeadRotO;
        float l = entity.yHeadRot;
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-g * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        renderEntityInInventory(guiGraphics, x, y, scale, quaternionf, quaternionf2, entity);
        entity.yBodyRot = h;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
    }



    public static void renderEntityInInventory(GuiGraphics guiGraphics, int x, int y, int scale, Quaternionf pose, @Nullable Quaternionf cameraOrientation, LivingEntity entity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, (double)50.0F);
        guiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)scale, (float)scale, (float)(-scale)));
        guiGraphics.pose().mulPose(pose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            cameraOrientation.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(cameraOrientation);
        }

        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880));
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        // [-- Modified
        //return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
        return super.isHovering(x, y, width, height, mouseX, mouseY);
        // --]
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // [-- Modified
        /*if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(mouseX, mouseY, button);
        }*/
        return super.mouseClicked(mouseX, mouseY, button);
        // --]
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean bl = mouseX < (double)guiLeft || mouseY < (double)guiTop || mouseX >= (double)(guiLeft + this.imageWidth) || mouseY >= (double)(guiTop + this.imageHeight);
        // [-- Modified
        //return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && bl;
        return bl;
        // --]
    }

    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        // [-- Modified
        //this.recipeBookComponent.slotClicked(slot);
        // --]
    }

    // [-- Modified
/*
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), (Boolean)this.minecraft.options.operatorItemsTab().get()));
        } else {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                button.setPosition(this.leftPos + 104, this.height / 2 - 22);
                this.buttonClicked = true;
            }));
            this.addWidget(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);
        }
    }

    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }
*/

    // --]



    @Override
    public int visorEssentials$getEdgeX() {
        return fullInventory ? leftPos : leftPos + 40;
    }

    @Override
    public int visorEssentials$getEdgeY() {
        return topPos;
    }

    @Override
    public int visorEssentials$getEdgeWidth() {
        if(hasEffects){
            return fullInventory
                    ? imageWidth + 40 : imageWidth - 40;
        }else {
            return fullInventory
                    ? imageWidth : imageWidth - 80;
        }    }

    @Override
    public int visorEssentials$getEdgeHeight() {
        return imageHeight;
    }






}
