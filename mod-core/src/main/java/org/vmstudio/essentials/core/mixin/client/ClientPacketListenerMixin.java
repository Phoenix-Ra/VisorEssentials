package org.vmstudio.essentials.core.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayInventory;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    /**
     * Vanilla dispatches recipe updates only to Minecraft#screen.
     * VR overlay screens are never the current screen,
     * so forward the updates to them manually.
     */
    @Inject(method = "handleAddOrRemoveRecipes", at = @At("TAIL"))
    private void visorEssentials$forwardRecipesUpdated(ClientboundRecipePacket packet, CallbackInfo ci) {
        visorEssentials$notifyOverlayScreens();
    }

    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void visorEssentials$forwardRecipesReplaced(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        visorEssentials$notifyOverlayScreens();
    }

    @Unique
    private static void visorEssentials$notifyOverlayScreens() {
        if (!EssentialsClientSettings.getBetterInventory().isEnabled()) {
            return;
        }
        if (VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }
        var overlayManager = VisorAPI.client().getGuiManager().getOverlayManager();
        visorEssentials$notifyScreen(overlayManager.getOverlay(VROverlayInventory.ID, VROverlayInventory.class));
        visorEssentials$notifyScreen(overlayManager.getOverlay(VROverlayContainer.ID, VROverlayContainer.class));
    }

    @Unique
    private static void visorEssentials$notifyScreen(VROverlayScreenInScreen<?> overlay) {
        if (overlay != null
                && overlay.getScreen() instanceof RecipeUpdateListener listener) {
            listener.recipesUpdated();
        }
    }

    /**
     * Same forwarding for the ghost recipe "hint" packet,
     * sent by the server when a clicked recipe can't be fully placed.
     */
    @Inject(method = "handlePlaceRecipe", at = @At("TAIL"))
    private void visorEssentials$forwardGhostRecipe(ClientboundPlaceGhostRecipePacket packet, CallbackInfo ci) {
        if (!EssentialsClientSettings.getBetterInventory().isEnabled()) {
            return;
        }
        if (VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        AbstractContainerMenu containerMenu = minecraft.player.containerMenu;
        if (containerMenu.containerId != packet.getContainerId()) {
            return;
        }
        ((ClientPacketListener) (Object) this).getRecipeManager()
                .byKey(packet.getRecipe())
                .ifPresent(recipe -> {
                    var overlayManager = VisorAPI.client().getGuiManager().getOverlayManager();
                    visorEssentials$setupGhostRecipe(
                            overlayManager.getOverlay(VROverlayInventory.ID, VROverlayInventory.class),
                            containerMenu, recipe
                    );
                    visorEssentials$setupGhostRecipe(
                            overlayManager.getOverlay(VROverlayContainer.ID, VROverlayContainer.class),
                            containerMenu, recipe
                    );
                });
    }

    @Unique
    private static void visorEssentials$setupGhostRecipe(VROverlayScreenInScreen<?> overlay,
                                                         AbstractContainerMenu menu,
                                                         Recipe<?> recipe) {
        if (overlay == null) {
            return;
        }
        var screen = overlay.getScreen();
        if (!(screen instanceof RecipeUpdateListener listener)) {
            return;
        }
        // only the screen actually hosting this menu shows the hint
        if (!(screen instanceof MenuAccess<?> menuAccess)
                || menuAccess.getMenu() != menu) {
            return;
        }
        listener.getRecipeBookComponent().setupGhostRecipe(recipe, menu.slots);
    }
}
