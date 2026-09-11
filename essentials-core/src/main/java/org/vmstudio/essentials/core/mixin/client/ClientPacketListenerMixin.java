package org.vmstudio.essentials.core.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
//? if <1.20.2 {
/*import net.minecraft.world.item.crafting.Recipe;
*///?} else {
import net.minecraft.world.item.crafting.RecipeHolder;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayInventory;
import org.vmstudio.essentials.core.client.gui.screens.VRInvScreen;
import org.vmstudio.essentials.core.server.EssentialsServerSettings;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void visorEssentials$onJoinedServer(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isLocalServer()) {
            EssentialsServerSettings.joinedDedicatedServer();
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void visorEssentials$onDisconnected(CallbackInfo ci) {
        EssentialsServerSettings.resetToDefaults();
    }


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
        if (overlay == null
                || !(overlay.getScreen() instanceof RecipeUpdateListener listener)) {
            return;
        }
        if (!(listener instanceof VRInvScreen)) {
            var player = Minecraft.getInstance().player;
            if (player == null
                    || !(listener instanceof MenuAccess<?> menuAccess)
                    || menuAccess.getMenu() != player.containerMenu) {
                return;
            }
        }
        listener.recipesUpdated();
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

    //? if <1.20.2 {
    /*@Unique
    private static void visorEssentials$setupGhostRecipe(VROverlayScreenInScreen<?> overlay,
                                                         AbstractContainerMenu menu,
                                                         Recipe<?> recipe) {
    *///?} else {
    @Unique
    private static void visorEssentials$setupGhostRecipe(VROverlayScreenInScreen<?> overlay,
                                                         AbstractContainerMenu menu,
                                                         RecipeHolder<?> recipe) {
    //?}
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
