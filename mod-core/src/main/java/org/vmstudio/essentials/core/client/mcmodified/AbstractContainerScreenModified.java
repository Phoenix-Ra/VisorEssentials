package org.vmstudio.essentials.core.client.mcmodified;

import org.vmstudio.essentials.core.client.gui.ContainerSlot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AbstractContainerScreenModified {

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
