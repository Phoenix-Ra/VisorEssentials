package org.vmstudio.essentials.core.server;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

public class EssentialsServerPlayer {
    public static final float NO_BOW_TENSION = -1f;

    private final VRServerPlayer vrPlayer;

    @Getter @Setter
    private float bowTension = NO_BOW_TENSION;

    public EssentialsServerPlayer(@NotNull VRServerPlayer vrPlayer){
        this.vrPlayer = vrPlayer;
    }


    public float consumeBowTension(){
        float tension = bowTension;
        bowTension = NO_BOW_TENSION;
        return tension;
    }


    public ServerPlayer getMcPlayer(){
        return vrPlayer.getMcPlayer();
    }
}
