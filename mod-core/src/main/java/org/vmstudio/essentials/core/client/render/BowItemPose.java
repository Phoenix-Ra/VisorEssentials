package org.vmstudio.essentials.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;

@RegisterVRItemPose
public class BowItemPose extends VRHandItemPose {
    private static final String ID = "essentials_bow";

    public BowItemPose(@NotNull VisorAddon owner) { super(owner); }

    @Override
    public void applyPose(@NotNull PoseStack stack,
                          @NotNull AbstractClientPlayer player,
                          @NotNull HandType hand,
                          @NotNull ItemStack item,
                          float equipProgress,
                          float partialTicks) {
        VRClientPlayer vrPlayer = VisorAPI.client().getVRPlayer(player.getUUID());
        if (vrPlayer == null) return;

        int handDir = hand == HandType.MAIN ? 1 : -1;
        float gunAngle = vrPlayer.getGunAngle();


        float scale = 1.4f;
        float translateX = handDir * 0.1f;
        float translateY = 0.18f;
        float translateZ = -0.1f;
        float yaw = -7f;
        float pitch = 0f;
        float roll = handDir * -10f;

        yaw += gunAngle - 60f;

        Quaternionf rotation = new Quaternionf();
        rotation.mul(Axis.ZP.rotationDegrees(roll));
        rotation.mul(Axis.YP.rotationDegrees(pitch));
        rotation.mul(Axis.XP.rotationDegrees(yaw));

        stack.translate(translateX, translateY, translateZ);
        stack.mulPose(rotation);
        stack.scale(scale, scale, scale);
    }

    @Override
    public boolean canApplyPose(@NotNull AbstractClientPlayer player,
                                @NotNull HandType hand,
                                @NotNull ItemStack itemStack) {
        return itemStack.getItem() instanceof BowItem;
    }

    @Override
    public @NotNull ComponentPriority getPriority() { return ComponentPriority.NORMAL; }

    @Override
    public @NotNull String getId() { return ID; }
}