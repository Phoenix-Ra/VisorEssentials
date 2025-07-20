package me.phoenixra.visoressentials.core.mixin.common.inventory;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visoressentials.core.common.MixinUtils;
import me.phoenixra.visoressentials.core.common.VisorEssentials;

import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin extends AbstractContainerMenu {

    protected HorseInventoryMenuMixin(
            @Nullable MenuType<?> menuType, int i
    ) {
        super(menuType, i);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/animal/horse/AbstractHorse;)V",
            at = @At("TAIL"))
    private void visor$constructSlots(int i, Inventory inventory,
                                     Container container,
                                     AbstractHorse abstractHorse,
                                     CallbackInfo ci) {
        if(ModLoader.get().isDedicatedServer()) return;
        if(!VisorEssentials.isActive()) return;
        if(VisorAPI.clientState().stateMode().isNotActive()) return;
        
        for(Slot slot : MixinUtils
                .getExtraSlots(inventory, inventory.player)){
            addSlot(slot);
        }
    }
}
