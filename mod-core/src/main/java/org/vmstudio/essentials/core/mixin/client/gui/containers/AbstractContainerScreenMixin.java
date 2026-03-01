package org.vmstudio.essentials.core.mixin.client.gui.containers;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreenInScreen;
import org.vmstudio.essentials.core.client.gui.ContainerSlot;
import org.vmstudio.essentials.core.client.mcmodified.AbstractContainerScreenModified;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;


@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin <T extends AbstractContainerMenu>
        extends Screen
        implements MenuAccess<T>, AbstractContainerScreenModified {
    @Shadow @Final protected T menu;

    @Unique
    private List<ContainerSlot> visorEssentials$vrSlots;
    @Unique
    private LinkedHashMap<Slot, ContainerSlot> visorEssentials$vrSlotsMap;
    @Unique
    private boolean visorEssentials$isVrContainer;

    @Unique
    private int visorEssentials$edgeX = -1;
    @Unique
    private int visorEssentials$edgeY = -1;
    @Unique
    private int visorEssentials$edgeWidth = -1;
    @Unique
    private int visorEssentials$edgeHeight = -1;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
    )
    private boolean visorEssentials$noDraggingItem(ItemStack instance) {
        var focused = VisorAPI.client().getGuiManager().getCursorHandler()
                .getFocusedOverlay();
        if(focused != null
                && focused.getId().equals("game_screen")){
            if(Minecraft.getInstance().screen == this){
                return instance.isEmpty();
            }
        }
        if(focused instanceof VROverlayScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() == this){
                return instance.isEmpty();
            }
        }
        if(focused instanceof VROverlayTemplateScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() == this){
                return instance.isEmpty();
            }
        }
        return true;
    }

    @Inject(method = "<init>", at  = @At("TAIL"))
    public void visorEssentials$onInit(AbstractContainerMenu menu, Inventory playerInventory, Component title, CallbackInfo ci){
        visorEssentials$vrSlots = new ArrayList<>();
        visorEssentials$vrSlotsMap = new LinkedHashMap<>();

        visorEssentials$fillVRSlots(
                visorEssentials$vrSlots
        );
        for(var entry : visorEssentials$vrSlots){
            visorEssentials$vrSlotsMap.put(
                    entry.parent(), entry
            );
        }
    }
    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        if(visorEssentials$isVrContainer) {
            visorEssentials$preInit();
        }
    }

    @Redirect(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",ordinal = 1))
    private int visorEssentials$noInventoryTitle(GuiGraphics instance, Font font, Component text, int x, int y, int color, boolean dropShadow){
        if(visorEssentials$isVrContainer){
            return 0;
        }
        return instance.drawString(this.font, text, x, y, color, dropShadow);
    }
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I")
    )
    private int visorEssentials$redirectSlots1(NonNullList<?> instance) {
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlots.size();
        }
        return instance.size();
    }
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;")
    )
    private Object visorEssentials$redirectSlots2(NonNullList<?> instance, int i) {
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlots.get(i).parent();
        }
        return instance.get(i);
    }



    @Redirect(
            method = "findSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I")
    )
    private int visorEssentials$redirectSlots3(NonNullList<Slot> instance) {
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlots.size();
        }
        return instance.size();
    }
    @Redirect(
            method = "findSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;")
    )
    private Object visorEssentials$redirectSlots4(NonNullList<Slot> instance, int i) {
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlots.get(i).parent();
        }
        return instance.get(i);
    }


    @Redirect(
            method = "mouseReleased", // or the method where the for-each is
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;iterator()Ljava/util/Iterator;")
    )
    private Iterator<Slot> visorEssentials$redirectSlots5(NonNullList<Slot> instance) {
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap.keySet().iterator();
        }
        return instance.iterator();
    }




    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;x:I"))
    private int visorEssentials$redirectSlotPos1(Slot instance){
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap
                    .get(instance).vrPosX();
        }
        return instance.x;
    }
    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I"))
    private int visorEssentials$redirectSlotPos2(Slot instance){
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap
                    .get(instance).vrPosY();
        }
        return instance.y;
    }


    @Redirect(method = "renderSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;x:I"))
    private int visorEssentials$redirectSlotPos3(Slot instance){
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap
                    .get(instance).vrPosX();
        }
        return instance.x;
    }
    @Redirect(method = "renderSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I"))
    private int visorEssentials$redirectSlotPos4(Slot instance){
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap
                    .get(instance).vrPosY();
        }
        return instance.y;
    }


    @Redirect(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;x:I"))
    private int visorEssentials$redirectSlotPos5(Slot instance){
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap
                    .get(instance).vrPosX();
        }
        return instance.x;
    }
    @Redirect(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I"))
    private int visorEssentials$redirectSlotPos6(Slot instance){
        if(visorEssentials$isVrContainer){
            return visorEssentials$vrSlotsMap
                    .get(instance).vrPosY();
        }
        return instance.y;
    }


    @Override
    public void visorEssentials$fillVRSlots(
            @NotNull List<ContainerSlot> slots
    ) {
        slots.clear();
        for(Slot slot : menu.slots){
            if(!(slot.container instanceof Inventory)){
                slots.add(
                        new ContainerSlot(
                                slot,
                                slot.x, slot.y
                        )
                );
            }
        }
    }

    @Override
    public void visorEssentials$setVRContainer(boolean flag) {
        visorEssentials$isVrContainer = flag;
    }
    @Override
    public boolean visorEssentials$isVRContainer() {
        return visorEssentials$isVrContainer;
    }

    @Override
    public @NotNull List<ContainerSlot> visorEssentials$getVRSlots() {
        return visorEssentials$vrSlots;
    }


    @Override
    public void visorEssentials$setEdgeX(int value) {
        visorEssentials$edgeX = value;
    }
    @Override
    public void visorEssentials$setEdgeY(int value) {
        visorEssentials$edgeY = value;
    }

    @Override
    public void visorEssentials$setEdgeWidth(int value) {
        visorEssentials$edgeWidth = value;
    }
    @Override
    public void visorEssentials$setEdgeHeight(int value) {
        visorEssentials$edgeHeight = value;
    }

    @Override
    public int visorEssentials$getEdgeX() {
        return visorEssentials$edgeX;
    }

    @Override
    public int visorEssentials$getEdgeY() {
        return visorEssentials$edgeY;
    }

    @Override
    public int visorEssentials$getEdgeWidth() {
        return visorEssentials$edgeWidth;
    }

    @Override
    public int visorEssentials$getEdgeHeight() {
        return visorEssentials$edgeHeight;
    }
}
