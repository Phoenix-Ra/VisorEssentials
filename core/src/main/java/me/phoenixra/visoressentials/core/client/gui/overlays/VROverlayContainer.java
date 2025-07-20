package me.phoenixra.visoressentials.core.client.gui.overlays;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.VRClientPlayerImpl;
import me.phoenixra.visoressentials.core.client.mcmodified.AbstractContainerScreenModified;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicBoolean;

public class VROverlayContainer extends VROverlayScreenInScreen<AbstractContainerScreen<?>> {

    public static final String ID = "container";


    @Getter
    private Vector3f sourcePos;
    @Getter
    private Vector3f sourceBlockPos;
    @Getter
    private Entity sourceEntity;

    //For convenience
    private final float extraXForBlock = 0.5f;
    private final float extraYForBlock = 0;
    private final float extraZForBlock = 0.5f;


    @Getter
    @Setter
    private long lastManualClose;


    private boolean orientInitialized;


    public VROverlayContainer(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id, null);
    }

    @Override
    protected void init() {

    }


    @Override
    protected boolean updateVisibility() {
        return true;
    }


    @Override
    protected void onTick() {
        if (minecraft.screen != null || minecraft.player == null) {
            setEnabled(false);
            return;
        }
        if (minecraft.player.containerMenu
                == minecraft.player.inventoryMenu) {
            setEnabled(false);
            return;
        }
        if (!isOverlayNearPlayer()) {
            setEnabled(false);
            return;
        }

        var vrContainer = (AbstractContainerScreenModified)screen;

        cursorEdgeX = vrContainer.visorEssentials$getEdgeX();
        cursorEdgeY = vrContainer.visorEssentials$getEdgeY();
        cursorEdgeWidth = vrContainer.visorEssentials$getEdgeWidth();
        cursorEdgeHeight = vrContainer.visorEssentials$getEdgeHeight();

        super.onTick();
    }


    @Override
    public void updatePose(float partialTick) {
        Vector3f newPosition;
        Matrix4f newRotation;

        boolean aimedAtEntity;
        boolean aimedAtBlock;

        if (orientInitialized) {
            aimedAtEntity = sourceEntity != null;
            aimedAtBlock = !aimedAtEntity;
        } else {
            aimedAtEntity = minecraft.hitResult instanceof EntityHitResult
                    && (((EntityHitResult) minecraft.hitResult).getEntity() instanceof ContainerEntity
                    || ((EntityHitResult) minecraft.hitResult).getEntity() instanceof InventoryCarrier
                    || ((EntityHitResult) minecraft.hitResult).getEntity() instanceof HasCustomInventoryScreen);

            aimedAtBlock = !aimedAtEntity
                    && minecraft.hitResult != null
                    && (minecraft.hitResult.getType() == HitResult.Type.BLOCK);

        }

        var facingElement = ClientContext.player
                .getPoseData(PoseDataType.RENDER)
                .getHmd();

        if ((aimedAtBlock || aimedAtEntity || sourcePos != null)) {
            if (sourcePos == null) {
                if (aimedAtEntity) {
                    EntityHitResult entityHitResult = (EntityHitResult) minecraft.hitResult;
                    sourceEntity = entityHitResult.getEntity();
                    Vector3f pos = sourceEntity
                            .getPosition(partialTick).toVector3f();
                    sourcePos = new Vector3f(
                            pos.x,
                            pos.y,
                            pos.z
                    );
                } else {
                    BlockHitResult blockHitResult = (BlockHitResult) minecraft.hitResult;
                    sourcePos = new Vector3f(
                            blockHitResult.getBlockPos().getX() + extraXForBlock,
                            blockHitResult.getBlockPos().getY() + extraYForBlock,
                            blockHitResult.getBlockPos().getZ() + extraZForBlock
                    );
                    sourceBlockPos = new Vector3f(
                            blockHitResult.getBlockPos().getX(),
                            blockHitResult.getBlockPos().getY(),
                            blockHitResult.getBlockPos().getZ()
                    );
                }
            } else if (sourceEntity != null && sourceEntity.isAlive()) {
                Vector3f pos = sourceEntity
                        .getPosition(partialTick).toVector3f();
                sourcePos = new Vector3f(
                        pos.x,
                        pos.y,
                        pos.z
                );
            }

            float yPos = findYPos(
                    sourcePos.y,
                    (float) (aimedAtEntity ?
                            sourceEntity.getEyePosition(partialTick).y
                            : sourcePos.y + 1.1),
                    ClientContext.player
                            .getPoseData(PoseDataType.RENDER)
                            .convertPositionFrom(
                                    PoseDataType.ROOM,
                                    facingElement.getPosition()
                            ).y
            );
            newPosition = new Vector3f(sourcePos.x, yPos, sourcePos.z);

        } else {
            Vector3f vec3 = new Vector3f(0.0f, 0.0f, -2.0f);


            var hmdPos = facingElement.getPosition();
            var vec32 = facingElement.getCustomVector(vec3);
            newPosition = (
                    new Vector3f(
                            vec32.x / 2.0f + hmdPos.x(),
                            vec32.y / 2.0f + hmdPos.y(),
                            vec32.z / 2.0f + hmdPos.z()
                    )
            );
        }

        // orient screen
        var hmdPos = facingElement.getPosition();
        Vector3f look = new Vector3f();

        look.x = newPosition.x - hmdPos.x();
        look.y = newPosition.y - hmdPos.y();
        look.z = newPosition.z - hmdPos.z();
        float pitch = (float) Math.asin((look.y() / look.length()));
        float yaw = (float) (Math.PI + Mth.atan2(look.x(), look.z()));
        Matrix4f rotation = new Matrix4f().rotationY(yaw);
        Matrix4f tilt = new Matrix4f().rotationX(pitch);
        newRotation = rotation.mul(tilt);
        getPose().update(
                newPosition, newRotation,
                getPose().getScale()
        );
        orientInitialized = true;
    }


    @Override
    public void onEnable() {
        minecraft.player.containerMenu = screen.getMenu();
        var keyboard = ClientContext.overlayManager.getKeyboardAccessor();
        if (keyboard.getAttachedTo() == this) {
            keyboard.setVisible(false);
        }

    }

    @Override
    public void onDisable() {
        if (screen != null) {
            screen.onClose();
            screen.removed();
            screen = null;
            sourcePos = null;
            sourceBlockPos = null;
            sourceEntity = null;
            orientInitialized = false;
        }
    }



    private boolean isOverlayNearPlayer() {
        if (sourcePos == null) return true;

        VRClientPlayerImpl clientPlayer = ClientContext.player;

        var roomPos = clientPlayer
                .getPoseData(PoseDataType.ROOM)
                .convertPositionFrom(PoseDataType.PRE_TICK, sourcePos);

        var hmdPos = clientPlayer
                .getPoseData(PoseDataType.ROOM)
                .getHmd()
                .getPosition();
        double distance = roomPos.sub(hmdPos).length();
        return distance < 4f;
    }



    private float findYPos(float lowerBound,
                           float upperBound,
                           float hmdPos
    ) {
        return Math.max(
                lowerBound,
                Math.min(hmdPos, upperBound)
        ) + (0.25F * getPose().getScale());
    }


    public void openMenu(AbstractContainerScreen<?> newScreen) {

        ((AbstractContainerScreenModified)newScreen)
                .visorEssentials$setVRContainer(true);

        screen = newScreen;
        if (isEnabled()) {
            sourcePos = null;
            sourceBlockPos = null;
            sourceEntity = null;
            orientInitialized = false;
            onEnable();
        }else {
            setEnabled(true);
        }
        screen.init(minecraft, width, height);
        updatePose(1);
    }

    public boolean isAttachedTo(BlockPos blockPos){
        if(sourceBlockPos == null) return false;

        return sourceBlockPos.x == blockPos.getX()
                && sourceBlockPos.y == blockPos.getY()
                && sourceBlockPos.z == blockPos.getZ();
    }
}
