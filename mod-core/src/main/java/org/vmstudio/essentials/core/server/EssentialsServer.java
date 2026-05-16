package org.vmstudio.essentials.core.server;

import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.server.events.ServerStoppedVREvent;
import org.vmstudio.visor.api.server.events.VRPlayerJoinedVREvent;
import org.vmstudio.visor.api.server.events.VRPlayerLeftVREvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EssentialsServer implements VREventListener {

    private Map<UUID, EssentialsServerPlayer> playerMap = new ConcurrentHashMap<>();

    @VREventHandler
    public void onPlayerJoined(VRPlayerJoinedVREvent event){
        var vrPlayer = event.getPlayer();
        playerMap.put(
                vrPlayer.getMcPlayer().getUUID(),
                new EssentialsServerPlayer(vrPlayer)
        );
    }

    @VREventHandler
    public void onPlayerLeft(VRPlayerLeftVREvent event){
        playerMap.remove(event.getPlayer().getMcPlayer().getUUID());
    }

    @VREventHandler
    public void onServerStopped(ServerStoppedVREvent event){
        playerMap.clear();
    }



    @Nullable
    public EssentialsServerPlayer getPlayer(UUID uuid){
        return playerMap.get(uuid);
    }
}
