package org.vmstudio.essentials.core.mixin.common.bow;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.essentials.core.server.bow.ServerBowState;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void visorEssentials$captureReleaser(ItemStack stack,
                                                 Level level,
                                                 LivingEntity entity,
                                                 int timeCharged,
                                                 CallbackInfo ci) {
        ServerBowState.beginRelease(entity);
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void visorEssentials$clearReleaser(ItemStack stack,
                                               Level level,
                                               LivingEntity entity,
                                               int timeCharged,
                                               CallbackInfo ci) {
        ServerBowState.endRelease();
        ServerBowState.clear(entity.getUUID());
    }

    @Inject(method = "getPowerForTime", at = @At("HEAD"), cancellable = true)
    private static void visorEssentials$overridePower(int charge,
                                                      CallbackInfoReturnable<Float> cir) {
        LivingEntity releaser = ServerBowState.currentReleaser();
        if (releaser == null) return;

        float tension = ServerBowState.peekTension(releaser.getUUID());
        if (tension < 0f) return;

        cir.setReturnValue(tension);
    }
}