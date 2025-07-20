package me.phoenixra.visoressentials.core.mixin.common.inventory;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visoressentials.core.common.MixinUtils;
import me.phoenixra.visoressentials.core.common.VisorEssentials;

import me.phoenixra.visoressentials.core.server.EssentialsAddonServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin extends AbstractContainerMenu {

    protected CraftingMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void visor$constructSlots(int i, Inventory inventory,
                                     ContainerLevelAccess containerLevelAccess,
                                     CallbackInfo ci) {
        EssentialsAddonServer.LOGGER.info("Crafting Menu");
        if(ModLoader.get().isDedicatedServer()) return;
        if(!VisorEssentials.isActive()) return;
        if(VisorAPI.clientState().stateMode().isNotActive()) return;
        
        for(Slot slot : MixinUtils
                .getExtraSlots(inventory, inventory.player)){
            addSlot(slot);
        }
    }


}
