package org.vmstudio.essentials.core.client.tasks;

import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.client.extensions.LocalPlayerExtension;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.essentials.core.common.network.toserver.BowDrawCancelPayloadToServer;
import org.vmstudio.essentials.core.common.network.toserver.BowTensionPayloadToServer;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.events.AllowClientFeatureVREvent;
import org.vmstudio.visor.api.client.events.gui.CursorFocusChangedVREvent;
import org.vmstudio.visor.api.client.events.render.HandRenderStateVREvent;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;

import static org.vmstudio.essentials.core.client.AddonEntryClient.MC;


@RegisterVisorTask
public class BowItemTask extends VisorTask implements VREventListener {
    public static final String ID = "item_bow";

    @Getter
    private static BowItemTask instance;

    private static final long SHOOT_DELAY = 500;
    private static final float START_DRAW_DISTANCE = 0.2f;

    private Vec3 aim;
    private double currentBowDraw;
    private double maxBowDraw;

    @Getter
    private boolean drawingBow;
    private boolean canDrawBow;
    private boolean pressed;

    private long holdBowTime;
    private int lastHapticStep;
    private long lastShoot;

    @Getter
    private HandType bowHolder = HandType.MAIN;

    private HandType savedActiveHand = HandType.MAIN;
    private boolean activeHandOverridden;

    public BowItemTask(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
        VisorAPI.eventBus().registerListener(owner, this);
    }

    @VREventHandler
    public void onAllowClientFeaturesEvent(AllowClientFeatureVREvent event) {
        if (event.getFeature() == ClientFeature.AIM_EFFECTS && isNotched()) {
            event.setCanceled(true);
        }
    }
    @VREventHandler
    public void onHandRenderState(HandRenderStateVREvent event) {
        if(!isActive(MC.player)){
            return;
        }
        if(event.getState().isGuiHand() || event.getState().isOff()){
            return;
        }
        var hand = event.getHandType();
        if (isHoldingBow(MC.player, hand.asInteractionHand())) {
            event.setState(HandRenderState.WORLD_HAND_ITEM_ONLY);
        }
        if(isNotched() && hand == bowHolder.opposite()){
            //ARROW
            event.setState(HandRenderState.WORLD_HAND_NO_ITEM);
        }
    }
    @VREventHandler
    public void onCursorFocus(CursorFocusChangedVREvent event){
        if(!isActive(MC.player)){
            return;
        }
        if(isNotched() && event.getNewOverlay() != null){
            event.setCanceled(true);
        }
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
        this.maxBowDraw = player.getBbHeight() * 0.22;

        // Determine which hand holds the bow
        final boolean bowInMainHand = isHoldingBow(player, InteractionHand.MAIN_HAND);
        final HandType bowHolder = bowInMainHand ? HandType.MAIN : HandType.OFFHAND;
        final HandType arrowHolder = bowHolder.opposite();
        this.bowHolder = bowHolder;

        // Cache controller positions (grip and aim share the same position in new Visor)
        final Vec3 handArrowPos = renderPose.getHand(arrowHolder).getPositionVec3();
        final Vec3 handBowPos = renderPose.getHand(bowHolder).getPositionVec3();

        final float worldScale = renderPose.getWorldScale();
        final float maxDistanceToBowCenter = START_DRAW_DISTANCE * worldScale;

        final Vec3 bowHandOffset = renderPose.getGripHand(bowHolder)
                .getCustomVector3(new Vector3f(0.0f, worldScale, 0.0f))
                .scale(maxBowDraw * 0.5);
        final Vec3 bowCenter = handBowPos.add(bowHandOffset);
        final double distanceToBowCenter = handArrowPos.distanceTo(bowCenter);

        this.aim = handArrowPos.subtract(handBowPos).normalize();

        final VRActionButton attackMain = inputManager.getActionLeftMouse(HandType.MAIN);
        final VRActionButton attackOff = inputManager.getActionLeftMouse(HandType.OFFHAND);
        this.pressed = (attackMain != null && attackMain.isPressed())
                || (attackOff != null && attackOff.isPressed());

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
                && distanceToBowCenter <= maxDistanceToBowCenter) {
            this.canDrawBow = true;
            this.holdBowTime = Util.getMillis();
            if (!this.drawingBow) {
                ((LocalPlayerExtension) player).visor$setUsingItem(
                        bowItem, bowInteractionHand
                );
                ((LocalPlayerExtension) player).visor$setUseItemRemaining(
                        useDuration
                );
            }
            onNotched();
        } else if (currentTime - this.holdBowTime > 250) {
            // Delay disable to avoid premature cancellation
            this.canDrawBow = false;
            if (isBow(player.getUseItem())) {
                ((LocalPlayerExtension) player).visor$setUsingItem(
                        ItemStack.EMPTY, bowInteractionHand
                );
            }
        }

        if (!this.drawingBow && this.canDrawBow && this.pressed && !lastPressed) {
            this.drawingBow = true;
            this.savedActiveHand = vrLocalPlayer.getActiveHand();
            this.activeHandOverridden = true;
            vrLocalPlayer.setActiveHand(bowHolder);
            MC.gameMode.useItem(player, bowInteractionHand);
            onNotched();
        }

        if (this.drawingBow && !this.pressed) {
            if (lastPressed && getDrawPercent() >= 0.1f) {
                shoot(player);
            } else {
                cancelDraw();
            }
        }

        if (!this.pressed) {
            this.drawingBow = false;
            restoreActiveHand();
        }

        if (!this.drawingBow && this.canDrawBow && !lastCanDraw) {
            inputManager.triggerHapticPulseBoth(0.0008f);
        }

        if (!this.drawingBow) {
            this.lastHapticStep = 0;
            return;
        }

        final boolean canShoot = currentTime > lastShoot + SHOOT_DELAY;
        final double handsDistance = canShoot ? handBowPos.distanceTo(handArrowPos) : 0;
        this.currentBowDraw = (handsDistance - maxDistanceToBowCenter) / worldScale;

        ((LocalPlayerExtension) player).visor$setUsingItem(bowItem, bowInteractionHand);
        final double drawPercent = getDrawPercent();
        if (drawPercent >= 1.0) {
            useDuration = 0;
        } else if (drawPercent > 0.4) {
            useDuration -= 15;
        }
        ((LocalPlayerExtension) player).visor$setUseItemRemaining(useDuration);

        final int currentStep = (int) (drawPercent * 10);
        if (currentStep % 2 == 0 && this.lastHapticStep != currentStep) {
            int hapticMicroSec = drawPercent > 0 ? (int) (drawPercent * 500) + 700 : 0;
            inputManager.triggerHapticPulseMicroSec(
                    arrowHolder, hapticMicroSec
            );
            if (drawPercent == 1.0) {
                inputManager.triggerHapticPulseMicroSec(
                        bowHolder, hapticMicroSec
                );
            }
        }
        this.lastHapticStep = currentStep;
    }

    @Override
    public void onClear(LocalPlayer player) {
        if (this.drawingBow) {
            cancelDraw();
        }
        restoreActiveHand();
        if (player != null && isBow(player.getUseItem())) {
            ((LocalPlayerExtension) player).visor$setUsingItem(
                    ItemStack.EMPTY, player.getUsedItemHand()
            );
        }
        this.drawingBow = false;
        this.canDrawBow = false;
        this.currentBowDraw = 0;
        this.lastHapticStep = 0;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if(!EssentialsClientSettings.isBetterBowActive()){
            return false;
        }
        if(MC.screen != null){
            return false;
        }
        if (!isEnabled() || player == null || MC.gameMode == null) return false;
        if (!player.isAlive() || player.isSleeping()) return false;
        return isHoldingBow(player, InteractionHand.MAIN_HAND)
                || isHoldingBow(player, InteractionHand.OFF_HAND);
    }

    private void shoot(Player player) {
        final boolean bowInMain = this.bowHolder == HandType.MAIN;
        VisorAPI.client().getInputManager().triggerHapticPulseBothMicroSec(
                bowInMain ? 3000 : 500,
                bowInMain ? 500 : 3000
        );
        float drawPercent = getDrawPercent();
        EssentialsChannel.get().sendToServer(new BowTensionPayloadToServer(drawPercent));
        MC.gameMode.releaseUsingItem(player);
        restoreActiveHand();
        this.drawingBow = false;
        this.currentBowDraw = 0;
        this.lastHapticStep = 0;
        if (drawPercent > 0.05f) {
            lastShoot = System.currentTimeMillis();
        }
    }


    private void cancelDraw() {
        EssentialsChannel.get().sendToServer(new BowDrawCancelPayloadToServer());
        restoreActiveHand();
        this.drawingBow = false;
        this.currentBowDraw = 0;
        this.lastHapticStep = 0;
    }

    private void restoreActiveHand() {
        if (this.activeHandOverridden) {
            this.activeHandOverridden = false;
            VisorAPI.client().getVRLocalPlayer().setActiveHand(this.savedActiveHand);
        }
    }


    private void onNotched(){
        var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        cursorHandler.clearFocus(HandType.MAIN);
        cursorHandler.clearFocus(HandType.OFFHAND);
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

    public Vec3 getAimVector() {
        return this.aim;
    }

    public float getDrawPercent() {
        if (this.maxBowDraw <= 0) return 0f;
        float p = (float) (this.currentBowDraw / this.maxBowDraw);
        return Math.max(0f, Math.min(1f, p));
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