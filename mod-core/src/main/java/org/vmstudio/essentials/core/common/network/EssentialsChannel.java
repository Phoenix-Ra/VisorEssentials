package org.vmstudio.essentials.core.common.network;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.essentials.core.common.network.toserver.BowTensionPayloadToServer;
import org.vmstudio.essentials.core.server.EssentialsServerConfig;
import org.vmstudio.essentials.core.server.EssentialsServerSettings;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorNetwork;

public final class EssentialsChannel {

    public static final ResourceLocation ID =
            new ResourceLocation(VisorEssentials.MOD_ID, "channel");
    public static final int NETWORK_VERSION = 1;

    private static VisorChannel INSTANCE;

    private EssentialsChannel() {}

    public static @NotNull VisorChannel get() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "EssentialsChannel not built — call init(owner) at mod-init time");
        }
        return INSTANCE;
    }

    public static void createChannel(@NotNull VisorAddon owner) {
        if (INSTANCE != null) return;
        INSTANCE = VisorChannel.builder(owner, ID, NETWORK_VERSION)
                .toServer(
                        (id, buffer)-> BowTensionPayloadToServer.read(buffer),
                        (payload, sender, response) -> {
                            if(!EssentialsServerSettings.isBetterBow()) return;

                            var essentialsPlayer = VisorEssentials.SERVER.getPlayer(sender.getUUID());
                            if(essentialsPlayer == null) return;

                            essentialsPlayer.setBowTension(payload.tension());
                        }
                )
                .toClient(
                        (id, buffer)-> SettingsPayloadToClient.read(buffer),
                        (payload) -> EssentialsServerConfig.updateSettings(
                                VisorEssentials.configManager(),
                                payload.config()
                        )
                )
                .build();
        VisorNetwork.registerChannel(INSTANCE);
    }
}