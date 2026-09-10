package org.vmstudio.essentials.core.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
import org.lwjgl.opengl.GL30;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.render.RenderPipelineStageVREvent;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.player.VRPose;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// TODO: Add an icon to this display, such as eyes or a Visor logo
public class RemoteOverlayRenderHelper implements VREventListener {
    private static final String DISPLAY_TEXT = "OVERLAY";
    private static final AtumColorImmutable DISPLAY_COLOR =
            new AtumColorImmutable(40, 45, 60, 140);
    private static final AtumColorImmutable DISPLAY_TEXT_COLOR =
            new AtumColorImmutable(92, 100, 118, DISPLAY_COLOR.getAlphaInt());
    private static final float DISPLAY_WIDTH = 1.6f;
    private static final float DISPLAY_HEIGHT = 0.9f;
    private static final float DISPLAY_SIZE = 0.8f;
    private static final float DISPLAY_TEXT_SCALE = 0.01f;
    private static final float DISPLAY_TEXT_Z_OFFSET = 0.01f;

    public RemoteOverlayRenderHelper(@NotNull VisorAddon owner) {
        VisorAPI.eventBus().registerListener(owner, this);
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_TRANSLUCENT,
                this::renderWorld
        );
    }

    private static void render(@NotNull PoseStack poseStack,
                               @NotNull Vec3 cameraPos,
                               float partialTicks) {
        if (!EssentialsClientSettings.getOverlayFocusVisualizer().isEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        List<OverlayRenderData> overlays = new ArrayList<>();

        for (Player player : minecraft.level.players()) {
            if (player == minecraft.player) {
                continue;
            }
            if (!player.isAlive() || player.isSleeping() || player.isInvisible()) {
                continue;
            }

            VRClientPlayer vrPlayer = VisorAPI.client().getVRPlayer(player.getUUID());
            if (vrPlayer == null || !vrPlayer.isOverlayFocused()) {
                continue;
            }

            VRPose hmdPose = vrPlayer.getPoseData(PlayerPoseType.RENDER).getHmd();
            RotationAngles rotationAngles = getOverlayRotation(player, hmdPose, partialTicks);
            Vector3f position = getIndicatorPosition(hmdPose, rotationAngles);
            double dx = position.x - cameraPos.x;
            double dy = position.y - cameraPos.y;
            double dz = position.z - cameraPos.z;
            overlays.add(new OverlayRenderData(
                    position,
                    rotationAngles,
                    isCameraBehindOverlay(position, rotationAngles, cameraPos),
                    dx * dx + dy * dy + dz * dz
            ));
        }

        if (overlays.isEmpty()) {
            return;
        }

        overlays.sort(Comparator.comparingDouble(OverlayRenderData::distanceToCameraSq).reversed());

        for (OverlayRenderData overlay : overlays) {
            setupOverlayRenderState();
            poseStack.pushPose();
            poseStack.translate(
                    overlay.position().x - cameraPos.x,
                    overlay.position().y - cameraPos.y,
                    overlay.position().z - cameraPos.z
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(-overlay.rotationAngles().yaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(overlay.rotationAngles().pitch()));
            renderPlaceholderQuad(
                    poseStack.last().pose(),
                    DISPLAY_COLOR,
                    DISPLAY_WIDTH,
                    DISPLAY_HEIGHT,
                    DISPLAY_SIZE
            );
            renderPlaceholderText(poseStack, minecraft.font, overlay.cameraBehind());
            poseStack.popPose();
        }

        RenderSystem.depthFunc(GL30.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private static void setupOverlayRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                GlStateManager.DestFactor.ONE
        );
        RenderSystem.disableCull();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.depthFunc(GL30.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
    }

    private static @NotNull Vector3f getIndicatorPosition(@NotNull VRPose hmdPose,
                                                          @NotNull RotationAngles rotationAngles) {
        Vec3 forward = Vec3.directionFromRotation(rotationAngles.pitch(), rotationAngles.yaw()).scale(0.45F);
        return new Vector3f(
                hmdPose.getPosition().x() + (float) forward.x,
                hmdPose.getPosition().y() + (float) forward.y,
                hmdPose.getPosition().z() + (float) forward.z
        );
    }

    private static void renderPlaceholderText(@NotNull PoseStack poseStack,
                                              @NotNull Font font,
                                              boolean backSide) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
        float textWidth = font.width(DISPLAY_TEXT);
        float textX = -textWidth / 2.0F;
        float textY = -font.lineHeight / 2.0F;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, backSide ? -DISPLAY_TEXT_Z_OFFSET : DISPLAY_TEXT_Z_OFFSET);
        if (backSide) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        poseStack.scale(DISPLAY_TEXT_SCALE, -DISPLAY_TEXT_SCALE, DISPLAY_TEXT_SCALE);
        font.drawInBatch(
                DISPLAY_TEXT,
                textX,
                textY,
                DISPLAY_TEXT_COLOR.asInt(),
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

    @VREventHandler
    public void onRenderPipelineStage(@NotNull RenderPipelineStageVREvent event) {
        if (event.getStage() != RenderPipelineStage.AFTER_TRANSLUCENT
                || VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }

        VRPose cameraPose = VisorAPI.client()
                .getVRLocalPlayer()
                .getPoseData(PlayerPoseType.RENDER)
                .getCameraPose(event.getRenderPass());
        render(
                event.getPoseStack(),
                new Vec3(
                        cameraPose.getPosition().x(),
                        cameraPose.getPosition().y(),
                        cameraPose.getPosition().z()
                ),
                event.getPartialTicks()
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

    private static boolean isCameraBehindOverlay(@NotNull Vector3f position,
                                                 @NotNull RotationAngles rotationAngles,
                                                 @NotNull Vec3 cameraPos) {
        Vec3 normal = Vec3.directionFromRotation(rotationAngles.pitch(), rotationAngles.yaw());
        double toCameraX = cameraPos.x - position.x;
        double toCameraY = cameraPos.y - position.y;
        double toCameraZ = cameraPos.z - position.z;
        return toCameraX * normal.x + toCameraY * normal.y + toCameraZ * normal.z < 0.0D;
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

    private void renderWorld(@NotNull PoseStack poseStack, float partialTicks) {
        if (VisorAPI.clientState().stateMode().isActive()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        render(poseStack, minecraft.gameRenderer.getMainCamera().getPosition(), partialTicks);
    }

    private record RotationAngles(float yaw, float pitch) {}

    private record OverlayRenderData(@NotNull Vector3f position,
                                     @NotNull RotationAngles rotationAngles,
                                     boolean cameraBehind,
                                     double distanceToCameraSq) {
    }
}

