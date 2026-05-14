package org.vmstudio.essentials.core.client.extensions;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface LocalPlayerExtension {


    void visor$setUsingItem(ItemStack itemstack1, InteractionHand interactionhand);

    void visor$setUseItemRemaining(int i);




}
