package org.vmstudio.essentials.core.mixin.common.bow;

import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.essentials.core.server.BowReleaseTension;


@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"), ordinal = 3, argsOnly = true)
    private float visorEssentials$applyBowTension(float velocity) {
        Float tension = BowReleaseTension.get();
        return tension != null ? velocity * tension : velocity;
    }
}
