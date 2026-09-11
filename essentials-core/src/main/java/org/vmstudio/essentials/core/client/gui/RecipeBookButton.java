package org.vmstudio.essentials.core.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.ResourceLocation;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;


public final class RecipeBookButton {

    public static final int WIDTH = 20;
    public static final int HEIGHT = 18;

    //? if <1.20.2 {
    /*private static final ResourceLocation TEXTURE =
            McVersionUtils.newResourceLoc("textures/gui/recipe_button.png");
    *///?}

    private RecipeBookButton() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static ImageButton create(int x, int y, Button.OnPress onPress) {
        //? if <1.20.2 {
        /*return new ImageButton(x, y, WIDTH, HEIGHT, 0, 0, 19, TEXTURE, onPress);
        *///?} else {
        return new ImageButton(x, y, WIDTH, HEIGHT, RecipeBookComponent.RECIPE_BUTTON_SPRITES, onPress);
        //?}
    }
}
