package org.vmstudio.essentials.core.server;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

public class EssentialsServerPlayer {
    private final VRServerPlayer vrPlayer;

    @Getter @Setter
    private float bowTension;

    public EssentialsServerPlayer(@NotNull VRServerPlayer vrPlayer){
        this.vrPlayer = vrPlayer;
    }


    public ServerPlayer getMcPlayer(){
        return vrPlayer.getMcPlayer();
    }
}
