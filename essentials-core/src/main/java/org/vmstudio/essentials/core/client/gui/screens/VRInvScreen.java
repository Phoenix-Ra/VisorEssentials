package org.vmstudio.essentials.core.client.gui.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.essentials.core.client.gui.ContainerSlot;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.server.VRServerSettings;

import java.util.Iterator;
import java.util.List;


public class VRInvScreen extends VRInvEffectInvScreen implements AbstractContainerScreenExtension, RecipeUpdateListener {
    private GuiTexture IMAGE_FULL = new GuiTexture(
            new ResourceLocation(VisorEssentials.MOD_ID,"textures/gui/inventory.png"),
            0,0,258,156
    );
    private GuiTexture IMAGE_FULL_WITH_OFFHAND = new GuiTexture(
            new ResourceLocation(VisorEssentials.MOD_ID,"textures/gui/inventory_with_offhand.png"),
            0,0,278,156
    );
    private GuiTexture IMAGE_SIMPLIFIED = new GuiTexture(
            new ResourceLocation(VisorEssentials.MOD_ID,"textures/gui/inventory_simplified.png"),
            0,0,258,156
    );


    private static final ResourceLocation RECIPE_BUTTON_LOCATION =
            new ResourceLocation("textures/gui/recipe_button.png");

    public static final int IMAGE_HEIGHT = 156;
    public static final int RECIPE_BOOK_GAP = 2;
    public static final int CREATIVE_BUTTON_WIDTH = 120;
    public static final int CREATIVE_BUTTON_HEIGHT = 20;
    public static final int CREATIVE_BUTTON_GAP = 4;
    // canvas tall enough to fit the recipe book below the centered inventory
    public static final int MIN_CANVAS_HEIGHT =
            IMAGE_HEIGHT + 2 * (RecipeBookComponent.IMAGE_HEIGHT + RECIPE_BOOK_GAP);

    private final RecipeBookComponent recipeBookComponent = new VRRecipeBookComponent();
    private boolean recipeBookAvailable;

    private Button creativeButton;

    private float xMouse;
    private float yMouse;

    private boolean buttonClicked;

    public VRInvScreen(AbstractContainerMenu menu,
                       Inventory inventory) {
        super(menu,  inventory, Component.literal(""));
        this.titleLabelX = 97;
        this.imageWidth = hasOffhandSlot() ? 278 : 258;
        this.imageHeight = IMAGE_HEIGHT;
        visorEssentials$setVRContainer(true);
    }

    private static boolean hasOffhandSlot() {
        return !VRServerSettings.isTwoHandedVR();
    }
    @Override
    public void visorEssentials$fillVRSlots(
            @NotNull List<ContainerSlot> slots
    ) {
        fullInventory = !VisorAPI.client().getGuiManager()
                .getOverlayManager().getOverlay(VROverlayContainer.ID).isEnabled();

        boolean hasOffhand = hasOffhandSlot();

        int xOffset = hasOffhand ? 20 : 0;

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
                posX = 181 + xOffset + col * 18;
                posY = 26 + row * 18;
                slots.add(new ContainerSlot(slot, posX,posY));
            }else if(slot.container instanceof Inventory){
                if(slot.getContainerSlot()<=8){
                    //hotbar
                    switch (slot.getContainerSlot()){
                        case 0 -> {
                            posX = 121 + xOffset;
                            posY = 38;
                        }
                        case 1 -> {
                            posX = 121 + xOffset;
                            posY = 11;
                        }
                        case 2 -> {
                            posX = 148 + xOffset;
                            posY = 11;
                        }
                        case 3 -> {
                            posX = 148 + xOffset;
                            posY = 38;
                        }
                        case 4 -> {
                            posX = 148 + xOffset;
                            posY = 65;
                        }
                        case 5 -> {
                            posX = 121 + xOffset;
                            posY = 65;
                        }
                        case 6 -> {
                            posX = 94 + xOffset;
                            posY = 65;
                        }
                        case 7 -> {
                            posX = 94 + xOffset;
                            posY = 38;
                        }
                        case 8 -> {
                            posX = 94 + xOffset;
                            posY = 11;
                        }
                    }

                }else if(slot.getContainerSlot()<=35) {
                    // 9x3 grid layout
                    int index = slot.getContainerSlot() - 9;
                    int row = index / 9;
                    int col = index % 9;
                    posX = 49 + xOffset + col * 18;
                    posY = 96 + row * 18;
                }else{
                    if(!fullInventory) continue;
                    if(slot.getContainerSlot() == 40){
                        if(!hasOffhand) continue;
                        // offhand slot
                        posX = 79;
                        posY = 62;
                    }else {
                        // equipment slots
                        int index = slot.getContainerSlot() - 36;
                        posX = 8;
                        posY = 62 + index * -18;
                    }
                }
                slots.add(new ContainerSlot(slot,posX,posY));
            }
            else if(fullInventory && slot instanceof ResultSlot){
                posX = 237 + xOffset;
                posY = 36;
                slots.add(new ContainerSlot(slot, posX,posY));
            }
        }
    }
    @Override
    protected void init() {
        super.init();
        // [-- Modified: vanilla swaps the whole screen for the creative one,
        // here it is an opt-in button placed above the inventory
        this.creativeButton = null;
        if (fullInventory) {
            this.creativeButton = this.addRenderableWidget(Button.builder(
                            Component.translatable(VisorEssentials.MOD_ID + ".gui.open_creative"),
                            button -> openCreativeInventory())
                    .bounds(
                            this.leftPos + (this.imageWidth - CREATIVE_BUTTON_WIDTH) / 2,
                            this.topPos - CREATIVE_BUTTON_GAP - CREATIVE_BUTTON_HEIGHT,
                            CREATIVE_BUTTON_WIDTH,
                            CREATIVE_BUTTON_HEIGHT
                    )
                    .build());
            this.creativeButton.visible = isCreativeMode();
        }
        // --]
        recipeBookAvailable = fullInventory
                && this.menu instanceof RecipeBookMenu<?>;
        if (!recipeBookAvailable) {
            return;
        }

        int bookLeft = (this.width - RecipeBookComponent.IMAGE_WIDTH) / 2;
        int bookTop = this.topPos + this.imageHeight + RECIPE_BOOK_GAP;
        this.recipeBookComponent.init(
                2 * (bookLeft + 86) + RecipeBookComponent.IMAGE_WIDTH,
                2 * bookTop + RecipeBookComponent.IMAGE_HEIGHT,
                this.minecraft,
                false,
                (RecipeBookMenu<?>) this.menu
        );
        int xOffset = hasOffhandSlot() ? 20 : 0;
        this.addRenderableWidget(new ImageButton(this.leftPos + 188 + xOffset, this.topPos + 62, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
            this.recipeBookComponent.toggleVisibility();
            this.buttonClicked = true;
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    public void containerTick() {
        // [-- Modified: creative-screen switch replaced with a button,
        // its visibility follows the game mode
        if (creativeButton != null) {
            creativeButton.visible = isCreativeMode();
        }
        // --]
        if (recipeBookAvailable) {
            this.recipeBookComponent.tick();
        }
    }

    private boolean isCreativeMode() {
        return this.minecraft != null
                && this.minecraft.gameMode != null
                && this.minecraft.gameMode.hasInfiniteItems();
    }

    private boolean isCreativeButtonVisible() {
        return creativeButton != null && creativeButton.visible;
    }

    private void openCreativeInventory() {
        this.minecraft.setScreen(new CreativeModeInventoryScreen(
                this.minecraft.player,
                this.minecraft.player.connection.enabledFeatures(),
                this.minecraft.options.operatorItemsTab().get()
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // [-- Modified: no renderBackground inside the overlay,
        // no narrow-screen mode - the book always fits below,
        // recipe book calls guarded by availability
        if (recipeBookAvailable) {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, partialTick);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        }
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

        if(fullInventory){
            (hasOffhandSlot() ? IMAGE_FULL_WITH_OFFHAND : IMAGE_FULL).blit(guiGraphics, i, j);
        }else {
            IMAGE_SIMPLIFIED.blit(guiGraphics, i + imageWidth - IMAGE_SIMPLIFIED.getWidth(), j);
        }

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

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (recipeBookAvailable && this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        if (!recipeBookAvailable) {
            return bl;
        }
        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && bl;
    }

    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        if (recipeBookAvailable) {
            this.recipeBookComponent.slotClicked(slot);
        }
    }

    @Override
    public void recipesUpdated() {
        if (recipeBookAvailable) {
            this.recipeBookComponent.recipesUpdated();
        }
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }



    private boolean isRecipeBookOpen() {
        return recipeBookAvailable && this.recipeBookComponent.isVisible();
    }

    @Override
    public int visorEssentials$getEdgeX() {
        return fullInventory ? leftPos
                : leftPos + imageWidth - IMAGE_SIMPLIFIED.getWidth() + 40;
    }

    @Override
    public int visorEssentials$getEdgeY() {
        // extend the interactable area over the button above
        if (isCreativeButtonVisible()) {
            return topPos - CREATIVE_BUTTON_GAP - CREATIVE_BUTTON_HEIGHT;
        }
        return topPos;
    }

    @Override
    public int visorEssentials$getEdgeWidth() {
        if(hasEffects){
            return fullInventory
                    ? imageWidth + 40 : IMAGE_SIMPLIFIED.getWidth() - 40;
        }else {
            return fullInventory
                    ? imageWidth : IMAGE_SIMPLIFIED.getWidth() - 80;
        }    }

    @Override
    public int visorEssentials$getEdgeHeight() {
        int height = imageHeight;
        // extend the interactable area over the book below
        if (isRecipeBookOpen()) {
            height += RECIPE_BOOK_GAP + RecipeBookComponent.IMAGE_HEIGHT;
        }
        if (isCreativeButtonVisible()) {
            height += CREATIVE_BUTTON_GAP + CREATIVE_BUTTON_HEIGHT;
        }
        return height;
    }


    private class VRRecipeBookComponent extends RecipeBookComponent {

        @Override
        public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
            if (!recipeBookAvailable) {
                return;
            }
            ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
            this.ghostRecipe.setRecipe(recipe);
            addGhostIngredient(Ingredient.of(resultStack), slots.get(0));
            this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
        }

        @Override
        public void addItemToSlot(Iterator<Ingredient> ingredients, int slotIndex, int maxAmount, int gridX, int gridY) {
            Ingredient ingredient = ingredients.next();
            if (!ingredient.isEmpty()) {
                addGhostIngredient(ingredient, this.menu.slots.get(slotIndex));
            }
        }

        private void addGhostIngredient(Ingredient ingredient, Slot slot) {
            for (ContainerSlot vrSlot : visorEssentials$getVRSlots()) {
                if (vrSlot.parent() == slot) {
                    this.ghostRecipe.addIngredient(ingredient, vrSlot.vrPosX(), vrSlot.vrPosY());
                    return;
                }
            }
            this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
        }
    }






}
