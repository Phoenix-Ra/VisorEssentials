package me.phoenixra.visoressentials.core.mixin.common.inventory;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visoressentials.core.common.MixinUtils;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//SMOKER, FURNACE, BLAST FURNACE
@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceMenuMixin extends RecipeBookMenu<Container> {

    public AbstractFurnaceMenuMixin(MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
            at = @At("TAIL"))
    private void visor$constructSlots(MenuType<?> menuType, RecipeType<? extends AbstractCookingRecipe> recipeType,
                                     RecipeBookType recipeBookType, int i, Inventory inventory, Container container,
                                     ContainerData containerData, CallbackInfo ci
    ) {
        if(ModLoader.get().isDedicatedServer()) return;
        if(!VisorEssentials.isActive()) return;
        if(VisorAPI.clientState().stateMode().isNotActive()) return;
        
        for(Slot slot : MixinUtils
                .getExtraSlots(inventory, inventory.player)){
            addSlot(slot);
        }

    }
}
