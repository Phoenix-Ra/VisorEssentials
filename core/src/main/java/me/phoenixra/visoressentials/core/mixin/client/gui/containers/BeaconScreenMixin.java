package me.phoenixra.visoressentials.core.mixin.client.gui.containers;

import me.phoenixra.visoressentials.core.client.gui.ContainerSlot;
import me.phoenixra.visoressentials.core.client.mcmodified.AbstractContainerScreenModified;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu>
        implements AbstractContainerScreenModified {
    public BeaconScreenMixin(BeaconMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }


    @Override
    public void visorEssentials$fillVRSlots(
            @NotNull List<ContainerSlot> slots
    ) {
        slots.clear();
        for(Slot slot : menu.slots){
            if(slot.container instanceof SimpleContainer){
                slots.add(
                        new ContainerSlot(
                                slot,
                                slot.x, slot.y
                        )
                );
                break;
            }
        }
    }


}
