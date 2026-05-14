package org.vmstudio.essentials.core.client.tasks;

import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.vmstudio.essentials.core.client.extensions.LocalPlayerExtension;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.essentials.core.common.network.toserver.BowTensionPayloadToServer;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;

import static org.vmstudio.essentials.core.common.VisorEssentials.MC;


@RegisterVisorTask
public class ItemBowTask extends VisorTask implements VREventListener {
    private static final String ID = "item_bow";

    @Getter
    private static ItemBowTask instance;

    private static final long SHOOT_DELAY = 1000;
    private static final float START_DRAW_DISTANCE = 0.15f;
    private static final float START_DRAW_ANGLE = 20.0f;

    private Vec3 aim;
    private double currentBowDraw;
    private double maxBowDraw;

    @Getter
    private boolean drawingBow;
    private boolean canDrawBow;
    private boolean pressed;

    private float holdBowTime;
    private int lastHapticStep;
    private long lastShoot;

    public ItemBowTask(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @Override
    public void onRun(LocalPlayer player) {
        var vrLocalPlayer = VisorAPI.client().getVRLocalPlayer();
        var renderPose = vrLocalPlayer.getPoseData(PlayerPoseType.RENDER);
        var inputManager = VisorAPI.client().getInputManager();

        final long currentTime = System.currentTimeMillis();

        final boolean lastPressed = this.pressed;
        final boolean lastCanDraw = this.canDrawBow;

        // Update maximum bow draw based on player's height
        this.maxBowDraw = MC.player.getBbHeight() * 0.22;

        // Determine which hand holds the bow
        final boolean bowInMainHand = isHoldingBow(player, InteractionHand.MAIN_HAND);
        final HandType bowHolder = bowInMainHand ? HandType.MAIN : HandType.OFFHAND;
        final HandType arrowHolder = bowHolder.opposite();

        // Cache controller positions
        final Vec3 handArrowPos = renderPose.getHand(arrowHolder).getPositionVec3();
        final Vec3 handBowPos = renderPose.getHand(bowHolder).getPositionVec3();

        final float worldScale = renderPose.getWorldScale();
        final float maxDistanceToBowCenter = START_DRAW_DISTANCE * worldScale;

        // Calculate bow center using a custom offset based on maxBowDraw
        final Vec3 bowHandOffset = renderPose.getHand(
                bowInMainHand ? HandType.MAIN : HandType.OFFHAND
                )
                .getCustomVector3(new Vector3f(0.0f, worldScale, 0.0f))
                .scale(maxBowDraw * 0.5);
        final Vec3 bowCenter = handBowPos.add(bowHandOffset);
        final double distanceToBowCenter = handArrowPos.distanceTo(bowCenter);

        // Calculate aim vector
        this.aim = handArrowPos.subtract(handBowPos).normalize();

        // Determine the direction vectors for the arrow and bow hands
        final Vec3 arrowHandDir = new Vec3(
                renderPose.getHand(arrowHolder)
                        .getCustomVector(new Vector3f(0.0f, 0.0f, -1.0f))
        );
        final Vec3 bowHandDir = new Vec3(
                renderPose.getHand(bowHolder)
                        .getCustomVector(new Vector3f(0.0f, -1.0f, 0.0f))
        );
        final double handsAngle = Math.toDegrees(
                Math.acos(bowHandDir.dot(arrowHandDir))
        );

        // Update button state
        this.pressed = MC.options.keyAttack.isDown();

        final InteractionHand bowInteractionHand = bowInMainHand
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        // Determine which items are the bow and the arrow
        final ItemStack bowItem = bowInMainHand ? player.getMainHandItem() : player.getOffhandItem();
        ItemStack arrowItem = bowInMainHand ? player.getOffhandItem() : player.getMainHandItem();
        if (!arrowItem.is(ItemTags.ARROWS)) {
            arrowItem = ItemStack.EMPTY;
        }
        int useDuration = bowItem.getUseDuration();

        // Conditions for being able to draw the bow
        if (!arrowItem.isEmpty()
                && distanceToBowCenter <= maxDistanceToBowCenter
                && handsAngle <= START_DRAW_ANGLE) {
            this.canDrawBow = true;
            this.holdBowTime = (float) Util.getMillis();
            if (!this.drawingBow) {
                ((LocalPlayerExtension) player).visor$setUsingItem(
                        bowItem, bowInteractionHand
                );
                ((LocalPlayerExtension) player).visor$setUseItemRemaining(
                        useDuration
                );
            }
        } else if (currentTime - this.holdBowTime > 250) {
            // Delay disable to avoid premature cancellation
            this.canDrawBow = false;
            if (isHoldingBowOnActiveHand(player)) {
                ((LocalPlayerExtension) player).visor$setUsingItem(
                        ItemStack.EMPTY, bowInteractionHand
                );
            }
        }

        // Start drawing the bow when conditions are met and the attack button was just pressed
        if (!this.drawingBow && this.canDrawBow && this.pressed && !lastPressed) {
            this.drawingBow = true;
            MC.gameMode.useItem(player, bowInteractionHand);
        }

        // Shoot if the player releases the button after drawing the bow
        if (this.drawingBow && !this.pressed && lastPressed && getDrawPercent() > 0.0) {
            shoot(player, bowInMainHand, bowInMainHand);
        }

        // Stop drawing if the button is not pressed
        if (!this.pressed) {
            this.drawingBow = false;
        }

        // Provide haptic feedback when drawing is cancelled without shooting
        if (!this.drawingBow && this.canDrawBow && !lastCanDraw) {
            inputManager.triggerHapticPulseBoth(0.0008f);
        }

        if (!this.drawingBow) {
            this.lastHapticStep = 0;
            return;
        }

        // Update drawing state based on the distance between hands
        final boolean canShoot = currentTime > lastShoot + SHOOT_DELAY;
        final double handsDistance = canShoot ? handBowPos.distanceTo(handArrowPos) : 0;
        this.currentBowDraw = (handsDistance - maxDistanceToBowCenter) / worldScale;
        if (this.currentBowDraw > this.maxBowDraw) {
            this.currentBowDraw = this.maxBowDraw;
        }

        ((LocalPlayerExtension) player).visor$setUsingItem(bowItem, bowInteractionHand);
        final double drawPercent = getDrawPercent();
        if (drawPercent >= 1.0) {
            useDuration = 0;
        } else if (drawPercent > 0.4) {
            useDuration -= 15;
        }
        ((LocalPlayerExtension) player).visor$setUseItemRemaining(useDuration);

        // Provide haptic feedback while drawing
        final int currentStep = (int) (drawPercent * 10);
        if (currentStep % 2 == 0 && this.lastHapticStep != currentStep) {
            int hapticMicroSec = drawPercent > 0 ? (int) (drawPercent * 500) + 700 : 0;
            inputManager.triggerHapticPulse(
                    arrowHolder, hapticMicroSec
            );
            if (drawPercent == 1.0) {
                inputManager.triggerHapticPulse(
                        bowHolder, hapticMicroSec
                );
            }
        }
        this.lastHapticStep = currentStep;
    }

    @Override
    public void onClear(LocalPlayer player) {
        this.drawingBow = false;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (!isEnabled() || player == null || MC.gameMode == null) return false;
        if (!player.isAlive() || player.isSleeping()) return false;
        return isHoldingBow(player, InteractionHand.MAIN_HAND) || isHoldingBow(player, InteractionHand.OFF_HAND);
    }

    private void shoot(Player player, boolean bowInMain, boolean arrowInOffhand) {
        VisorAPI.client().getInputManager().triggerHapticPulseBothMicroSec(
                bowInMain ? 3000 : 500, // main hand
                arrowInOffhand ? 500 : 3000  // offhand
        );
        // Emulate bow tension for the server to release the bow properly
        EssentialsChannel.get().sendToServer(new BowTensionPayloadToServer(getDrawPercent()));
        MC.gameMode.releaseUsingItem(player);
        EssentialsChannel.get().sendToServer(new BowTensionPayloadToServer(0f));
        this.drawingBow = false;
        lastShoot = System.currentTimeMillis();
    }

    public boolean isItemModelDisabled(InteractionHand hand) {
        if (!isNotched()) return false;
        return MC.player.getItemInHand(hand).getItem() instanceof ArrowItem;
    }

    public boolean isNotched() {
        return this.canDrawBow || this.drawingBow;
    }

    public static boolean isBow(ItemStack itemStack) {
        return itemStack.getItem() instanceof BowItem;
    }

    public static boolean isHoldingBow(LivingEntity e, InteractionHand hand) {
        return isBow(e.getItemInHand(hand));
    }

    public static boolean isHoldingBowOnActiveHand(LivingEntity e) {
        return isBow(e.getItemInHand(VisorAPI.client().getVRLocalPlayer().getActiveHand().asInteractionHand()));
    }

    public Vec3 getAimVector() {
        return this.aim;
    }

    public float getDrawPercent() {
        return (float) (this.currentBowDraw / this.maxBowDraw);
    }


    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PRE_RENDER;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
