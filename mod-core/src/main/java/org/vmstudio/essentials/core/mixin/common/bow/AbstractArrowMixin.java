package org.vmstudio.essentials.core.mixin.common.bow;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.essentials.core.server.BowReleaseTension;


@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Unique
    private static final float CRIT_TENSION = 0.99f;

    @ModifyVariable(method = "setCritArrow", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private boolean visorEssentials$critOnlyAtFullDraw(boolean crit) {
        Float tension = BowReleaseTension.get();
        if (tension == null) {
            return crit;
        }
        return crit && tension >= CRIT_TENSION;
    }
}
