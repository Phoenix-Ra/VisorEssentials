package me.phoenixra.visoressentials.core.common;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MixinUtils {
    public static final EquipmentSlot[] SLOT_IDS;
    public static final ResourceLocation[] TEXTURE_EMPTY_SLOTS;

    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");



     static {
         TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
         SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    }
    public static List<Slot> getExtraSlots(Inventory container, Player player){
        List<Slot> slots = new ArrayList<>();
        for(int i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentSlot = MixinUtils.SLOT_IDS[i];
            slots.add(new Slot(container, 39 - i, 8, 8 + i * 18) {
                public void setByPlayer(@NotNull ItemStack itemStack) {
                    player.onEquipItem(equipmentSlot, this.getItem(), itemStack);
                    super.setByPlayer(itemStack);
                }

                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(@NotNull ItemStack itemStack) {
                    return equipmentSlot == Mob.getEquipmentSlotForItem(itemStack);
                }

                public boolean mayPickup(@NotNull Player playerx) {
                    ItemStack itemStack = this.getItem();
                    return !itemStack.isEmpty() && !playerx.isCreative() && EnchantmentHelper.hasBindingCurse(itemStack) ? false : super.mayPickup(playerx);
                }

                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, MixinUtils.TEXTURE_EMPTY_SLOTS[equipmentSlot.getIndex()]);
                }
            });
        }
        return slots;
    }
}
