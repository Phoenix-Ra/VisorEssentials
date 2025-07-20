package me.phoenixra.visoressentials.core.client.mcmodified;

import me.phoenixra.visoressentials.core.client.gui.ContainerSlot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AbstractContainerScreenModified {

    // Defaults used to not override
    // already implemented methods via mixin
    // for VrInvScreen


    default void visorEssentials$fillVRSlots(@NotNull List<ContainerSlot> slots){

    }

    @NotNull
    default List<ContainerSlot> visorEssentials$getVRSlots(){
        return List.of();
    }

    default void visorEssentials$setVRContainer(boolean flag){

    }

    default boolean visorEssentials$isVRContainer(){
        return false;
    }


    default int visorEssentials$getEdgeX(){
        return -1;
    }
    default int visorEssentials$getEdgeY(){
        return -1;
    }

    default int visorEssentials$getEdgeWidth(){
        return -1;
    }
    default int visorEssentials$getEdgeHeight(){
        return -1;
    }
}
