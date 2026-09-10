package org.vmstudio.essentials.core.mixin.common.bow;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.server.BowReleaseTension;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    protected ItemStack useItem;

    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void visorEssentials$openTensionWindow(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return;
        }
        if (VisorEssentials.SERVER == null) {
            return;
        }
        var essentialsPlayer = VisorEssentials.SERVER.getPlayer(player.getUUID());
        if (essentialsPlayer == null) {
            return;
        }
        float tension = essentialsPlayer.consumeBowTension();
        if (tension < 0) {
            return;
        }
        if (this.useItem.getItem() instanceof ProjectileWeaponItem) {
            BowReleaseTension.open(Math.min(tension, 1f));
        }
    }

    @ModifyArg(method = "releaseUsingItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"),
            index = 2)
    private int visorEssentials$forceFullCharge(int timeLeft) {
        return BowReleaseTension.get() != null ? 0 : timeLeft;
    }

    @Inject(method = "releaseUsingItem", at = @At("RETURN"))
    private void visorEssentials$closeTensionWindow(CallbackInfo ci) {
        BowReleaseTension.close();
    }
}
