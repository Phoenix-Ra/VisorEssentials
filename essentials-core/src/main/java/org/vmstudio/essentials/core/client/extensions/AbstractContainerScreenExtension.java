package org.vmstudio.essentials.core.client.extensions;

import net.minecraft.world.inventory.Slot;
import org.vmstudio.essentials.core.client.gui.ContainerSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AbstractContainerScreenExtension {

    // Defaults used to not override
    // already implemented methods via mixin
    // for VrInvScreen

    default void visorEssentials$preInit(){

    }

    default void visorEssentials$fillVRSlots(@NotNull List<ContainerSlot> slots){

    }

    default boolean visorEssentials$supportsVRContainer(){
        return false;
    }

    default void visorEssentials$setVRContainer(boolean flag){

    }

    default boolean visorEssentials$isVRContainer(){
        return false;
    }



    @NotNull
    default List<ContainerSlot> visorEssentials$getVRSlots(){
        return List.of();
    }


    @Nullable
    default ContainerSlot visorEssentials$getVRSlot(@NotNull Slot slot){
        return null;
    }



    default int visorEssentials$getEdgeX(){
        return -1;
    }
    default void visorEssentials$setEdgeX(int value){

    }

    default int visorEssentials$getEdgeY(){
        return -1;
    }
    default void visorEssentials$setEdgeY(int value){

    }

    default int visorEssentials$getEdgeWidth(){
        return -1;
    }
    default void visorEssentials$setEdgeWidth(int value){

    }

    default int visorEssentials$getEdgeHeight(){
        return -1;
    }
    default void visorEssentials$setEdgeHeight(int value){

    }
}
