package org.vmstudio.essentials.core.mixin.common.bow;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.essentials.core.common.VisorEssentials;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    /** Bow tension reported for the releaseUsing call currently being processed, per thread. */
    @Unique
    private static final ThreadLocal<Float> visor$releasingTension = new ThreadLocal<>();

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void visorEssentials$captureReleaser(ItemStack stack,
                                                 Level level,
                                                 LivingEntity entity,
                                                 int timeCharged,
                                                 CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            var essentialsPlayer = VisorEssentials.SERVER.getPlayer(player.getUUID());
            if(essentialsPlayer == null) {
                return;
            }
            float tension = essentialsPlayer.consumeBowTension();
            if(tension < 0) {
                return;
            }
            visor$releasingTension.set(tension);
        }

    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void visorEssentials$clearReleaser(ItemStack stack,
                                               Level level,
                                               LivingEntity entity,
                                               int timeCharged,
                                               CallbackInfo ci) {
        visor$releasingTension.remove();
    }

    @Inject(method = "getPowerForTime", at = @At("HEAD"), cancellable = true)
    private static void visorEssentials$overridePower(int charge,
                                                      CallbackInfoReturnable<Float> cir) {
        Float tension = visor$releasingTension.get();
        if(tension == null){
            return;
        }

        cir.setReturnValue(Math.min(tension, 1f));
    }
}