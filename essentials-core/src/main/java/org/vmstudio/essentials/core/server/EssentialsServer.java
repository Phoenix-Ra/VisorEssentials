package org.vmstudio.essentials.core.server;

import org.jetbrains.annotations.Nullable;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.essentials.core.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.server.events.ServerStartedVREvent;
import org.vmstudio.visor.api.server.events.ServerStoppedVREvent;
import org.vmstudio.visor.api.server.events.VisorPlayerJoinedVREvent;
import org.vmstudio.visor.api.server.events.VisorPlayerLeftVREvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EssentialsServer implements VREventListener {

    private Map<UUID, EssentialsServerPlayer> playerMap = new ConcurrentHashMap<>();

    @VREventHandler
    public void onServerStarted(ServerStartedVREvent event){
        if(!VisorEssentials.hasConfigManager()){
            VisorEssentials.initConfigManager(
                    event.getServer().getConfigManager().getLogger()
            );
        }
        EssentialsServerConfig.onServerInit();
    }

    @VREventHandler
    public void onPlayerJoined(VisorPlayerJoinedVREvent event){
        EssentialsChannel.get().sendToClient(
                event.getPlayer().getMcPlayer(),
                new SettingsPayloadToClient(
                        EssentialsServerConfig
                                .getSettingsForClient()
                                .toPlaintext()
                )
        );

        var vrPlayer = event.getPlayer().asVR();
        if(vrPlayer == null) return;
        playerMap.put(
                vrPlayer.getMcPlayer().getUUID(),
                new EssentialsServerPlayer(vrPlayer)
        );
    }

    @VREventHandler
    public void onPlayerLeft(VisorPlayerLeftVREvent event){
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
