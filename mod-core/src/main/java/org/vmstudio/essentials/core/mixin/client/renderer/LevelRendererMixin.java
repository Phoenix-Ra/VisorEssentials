package org.vmstudio.essentials.core.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.client.render.RemoteOverlayRenderHelper;
import org.vmstudio.visor.api.VisorAPI;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
            at = @At("TAIL")
    )
    private void visorEssentials$renderRemoteOverlayDisplay(PoseStack poseStack,
                                                         float partialTicks,
                                                         long finishTimeNano,
                                                         boolean renderBlockOutline,
                                                         Camera camera,
                                                         GameRenderer gameRenderer,
                                                         LightTexture lightTexture,
                                                         Matrix4f projectionMatrix,
                                                         CallbackInfo ci) {
        var clientState = VisorAPI.clientState();
        boolean renderInVanilla = clientState.renderPhase().isVanilla();
        boolean renderInVrWorld = clientState.stateMode().isActive()
                && clientState.renderPhase().isVRWorld();

        if (!renderInVanilla && !renderInVrWorld) {
            return;
        }

        RemoteOverlayRenderHelper.render(poseStack, camera.getPosition(), partialTicks);
    }
}
