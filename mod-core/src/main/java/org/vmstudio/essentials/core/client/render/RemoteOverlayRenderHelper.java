package org.vmstudio.essentials.core.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;

// TODO: Add an icon to this display, such as eyes or a Visor logo
public final class RemoteOverlayRenderHelper {
    private static final String DISPLAY_TEXT = "OVERLAY";
    private static final AtumColorImmutable DISPLAY_COLOR =
            new AtumColorImmutable(40, 45, 60, 140);
    private static final AtumColorImmutable DISPLAY_TEXT_COLOR =
            new AtumColorImmutable(92, 100, 118, DISPLAY_COLOR.getAlpha());
    private static final float DISPLAY_WIDTH = 1.6f;
    private static final float DISPLAY_HEIGHT = 0.9f;
    private static final float DISPLAY_SIZE = 0.8f;
    private static final float DISPLAY_TEXT_SCALE = 0.01f;
    private static final float DISPLAY_TEXT_Z_OFFSET = 0.01f;

    private RemoteOverlayRenderHelper() {
    }

    public static void render(@NotNull PoseStack poseStack,
                              @NotNull Vec3 cameraPos,
                              float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        boolean rendered = false;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        for (Player player : minecraft.level.players()) {
            if (player == minecraft.player) {
                continue;
            }

            VRClientPlayer vrPlayer = VisorAPI.client().getVRPlayer(player.getUUID());
            if (vrPlayer == null || !vrPlayer.isOverlayFocused()) {
                continue;
            }

            VRPose hmdPose = vrPlayer.getPoseData(PlayerPoseType.RENDER).getHmd();
            Vector3f indicatorPos = getIndicatorPosition(player, hmdPose, partialTicks);
            RotationAngles rotationAngles = getOverlayRotation(player, hmdPose, partialTicks);

            poseStack.pushPose();
            poseStack.translate(
                    indicatorPos.x - cameraPos.x,
                    indicatorPos.y - cameraPos.y,
                    indicatorPos.z - cameraPos.z
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotationAngles.yaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotationAngles.pitch()));
            renderPlaceholderQuad(
                    poseStack.last().pose(),
                    DISPLAY_COLOR,
                    DISPLAY_WIDTH,
                    DISPLAY_HEIGHT,
                    DISPLAY_SIZE
            );
            renderPlaceholderText(poseStack, minecraft.font);
            poseStack.popPose();
            rendered = true;
        }

        if (rendered) {
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            return;
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static @NotNull Vector3f getIndicatorPosition(@NotNull Player player,
                                                          @NotNull VRPose hmdPose,
                                                          float partialTicks) {
        RotationAngles rotationAngles = getOverlayRotation(player, hmdPose, partialTicks);
        Vec3 forward = Vec3.directionFromRotation(rotationAngles.pitch(), rotationAngles.yaw()).scale(0.45F);
        return new Vector3f(
                hmdPose.getPosition().x() + (float) forward.x,
                hmdPose.getPosition().y() + (float) forward.y,
                hmdPose.getPosition().z() + (float) forward.z
        );
    }

    private static @NotNull RotationAngles getOverlayRotation(@NotNull Player player,
                                                              @NotNull VRPose hmdPose,
                                                              float partialTicks) {
        Vector3fc direction = hmdPose.getDirection();
        float horizontalLength = Mth.sqrt(direction.x() * direction.x() + direction.z() * direction.z());

        if (horizontalLength < 1.0E-4F) {
            float bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
            return new RotationAngles(bodyYaw, 0.0F);
        }

        float yaw = (float) Math.toDegrees(Mth.atan2(-direction.x(), direction.z()));
        float pitch = (float) Math.toDegrees(Mth.atan2(-direction.y(), horizontalLength));
        return new RotationAngles(yaw, pitch);
    }

    private static void renderPlaceholderQuad(@NotNull Matrix4f poseMatrix,
                                              @NotNull AtumColorImmutable color,
                                              float displayWidth,
                                              float displayHeight,
                                              float size) {
        float aspect = displayHeight / displayWidth;
        float halfSize = size * 0.5f;
        float halfHeight = halfSize * aspect;
        float r = color.getRed();
        float g = color.getGreen();
        float b = color.getBlue();
        float a = color.getAlpha();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(poseMatrix, -halfSize, -halfHeight, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(poseMatrix, halfSize, -halfHeight, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(poseMatrix, halfSize, halfHeight, 0f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(poseMatrix, -halfSize, halfHeight, 0f).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private static void renderPlaceholderText(@NotNull PoseStack poseStack,
                                              @NotNull Font font) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        float textWidth = font.width(DISPLAY_TEXT);
        float textX = -textWidth / 2.0F;
        float textY = -font.lineHeight / 2.0F;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, DISPLAY_TEXT_Z_OFFSET);
        poseStack.scale(DISPLAY_TEXT_SCALE, -DISPLAY_TEXT_SCALE, DISPLAY_TEXT_SCALE);
        font.drawInBatch(
                DISPLAY_TEXT,
                textX,
                textY,
                DISPLAY_TEXT_COLOR.asInt(),
//                toArgb(DISPLAY_TEXT_COLOR),
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                15728880
        );
        bufferSource.endBatch();
        poseStack.popPose();
    }

    private record RotationAngles(float yaw, float pitch) {}
}
