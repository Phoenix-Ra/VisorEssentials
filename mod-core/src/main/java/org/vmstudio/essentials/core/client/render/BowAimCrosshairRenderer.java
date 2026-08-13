package org.vmstudio.essentials.core.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.essentials.core.client.tasks.BowItemTask;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.render.RenderPipelineStageVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.player.VRPose;

import static org.vmstudio.essentials.core.client.AddonEntryClient.MC;


public class BowAimCrosshairRenderer implements VREventListener {

    private static final ResourceLocation ICONS_LOC =
            new ResourceLocation("textures/gui/icons.png");
    private static final float UV_SIZE = 15f / 256f;

    private static final double MAX_AIM_DISTANCE = 64.0;
    private static final float SCALE_REF_DISTANCE = 4.5f;
    private static final float BASE_SCALE = 0.06f;
    private static final float SURFACE_OFFSET = 0.01f;

    private static final float MISS_BRIGHTNESS = 0.5f;
    private static final float READY_BRIGHTNESS = 0.4f;
    private static final float MIN_LIGHT = 0.15f;

    public BowAimCrosshairRenderer(@NotNull VisorAddon owner) {
        VisorAPI.eventBus().registerListener(owner, this);
    }

    @VREventHandler
    public void onRenderPipelineStage(@NotNull RenderPipelineStageVREvent event) {
        if (event.getStage() != RenderPipelineStage.AFTER_WORLD
                || VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }
        BowItemTask task = BowItemTask.getInstance();
        if (task == null || MC.player == null || MC.level == null) {
            return;
        }
        if (!task.isActive(MC.player) || !task.isNotched()) {
            return;
        }

        var renderPose = VisorAPI.client()
                .getVRLocalPlayer()
                .getPoseData(PlayerPoseType.RENDER);
        VRPose bowHand = renderPose.getHand(task.getBowHolder());

        Vec3 handPos = bowHand.getPositionVec3();
        Vec3 aimDir = bowHand.getDirectionVec3();
        if (aimDir.lengthSqr() < 1.0E-6) {
            return;
        }
        aimDir = aimDir.normalize();

        Vec3 rayStart = handPos.add(aimDir);
        Vec3 rayEnd = handPos.add(aimDir.scale(MAX_AIM_DISTANCE));

        HitResult blockHit = MC.level.clip(new ClipContext(
                rayStart, rayEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                MC.player
        ));
        boolean hitSomething = blockHit.getType() != HitResult.Type.MISS;
        Vec3 hitPos = hitSomething ? blockHit.getLocation() : rayEnd;

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                MC.player, rayStart, hitPos,
                new AABB(rayStart, hitPos).inflate(1.0),
                entity -> !entity.isSpectator() && entity.isPickable(),
                hitPos.distanceToSqr(rayStart)
        );
        if (entityHit != null) {
            hitPos = entityHit.getLocation();
            hitSomething = true;
        }

        VRPose cameraPose = renderPose.getCameraPose(event.getRenderPass());
        Vec3 cameraPos = new Vec3(
                cameraPose.getPosition().x(),
                cameraPose.getPosition().y(),
                cameraPose.getPosition().z()
        );

        Vec3 renderPos = hitPos.subtract(aimDir.scale(SURFACE_OFFSET));

        float distance = (float) hitPos.distanceTo(handPos);
        float scale = BASE_SCALE
                * Mth.sqrt(renderPose.getWorldScale())
                * Math.max(1.0f, distance / SCALE_REF_DISTANCE);

        render(
                event.getPoseStack(),
                renderPos, cameraPos, aimDir, scale,
                getBrightness(task, renderPos, hitSomething)
        );
    }

    private float getBrightness(@NotNull BowItemTask task,
                                @NotNull Vec3 renderPos,
                                boolean hitSomething) {
        float light = MC.level.getMaxLocalRawBrightness(
                BlockPos.containing(renderPos)
        ) / (float) MC.level.getMaxLightLevel();
        float brightness = Math.max(light, MIN_LIGHT);
        if (!hitSomething) {
            brightness *= MISS_BRIGHTNESS;
        }
        if (!task.isDrawingBow()) {
            brightness *= READY_BRIGHTNESS;
        }
        return brightness;
    }

    private void render(@NotNull PoseStack poseStack,
                        @NotNull Vec3 renderPos,
                        @NotNull Vec3 cameraPos,
                        @NotNull Vec3 aimDir,
                        float scale,
                        float brightness) {

        // --- Prepare variables ---
        BufferBuilder buf = Tesselator.getInstance().getBuilder();

        float horizontalLength = Mth.sqrt(
                (float) (aimDir.x * aimDir.x + aimDir.z * aimDir.z)
        );
        float yaw = (float) Math.toDegrees(Mth.atan2(-aimDir.x, aimDir.z));
        float pitch = (float) Math.toDegrees(Mth.atan2(-aimDir.y, horizontalLength));

        // --- Setup ---
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.disableCull();

        RenderSystem.setShaderTexture(0, ICONS_LOC);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        poseStack.pushPose();
        poseStack.translate(
                renderPos.x - cameraPos.x,
                renderPos.y - cameraPos.y,
                renderPos.z - cameraPos.z
        );


        // --- Render ---
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(scale, scale, scale);
        Matrix4f mat = poseStack.last().pose();

        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buf.vertex(mat, -1f, 1f, 0f)
                .uv(UV_SIZE, 0f)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat, 1f, 1f, 0f)
                .uv(0f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat, 1f, -1f, 0f)
                .uv(0f, UV_SIZE)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat, -1f, -1f, 0f)
                .uv(UV_SIZE, UV_SIZE)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        BufferUploader.drawWithShader(buf.end());

        // --- Restore ---
        poseStack.popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }
}
