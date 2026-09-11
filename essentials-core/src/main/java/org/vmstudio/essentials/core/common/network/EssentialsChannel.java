package org.vmstudio.essentials.core.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.essentials.core.common.network.toserver.BowDrawCancelPayloadToServer;
import org.vmstudio.essentials.core.common.network.toserver.BowTensionPayloadToServer;
import org.vmstudio.essentials.core.server.EssentialsServerConfig;
import org.vmstudio.essentials.core.server.EssentialsServerSettings;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorNetwork;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

public final class EssentialsChannel {

    public static final ResourceLocation ID =
            McVersionUtils.newResourceLoc(VisorEssentials.MOD_ID, "channel");
    public static final int NETWORK_VERSION = 2; // 2: bow draw cancel payload

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
                        EssentialsChannel::readToServer,
                        (payload, sender, response) -> {
                            if(!EssentialsServerSettings.isBetterBow()) return;

                            if (payload instanceof BowTensionPayloadToServer tension) {
                                var essentialsPlayer = VisorEssentials.SERVER.getPlayer(sender.getUUID());
                                if(essentialsPlayer == null) return;

                                essentialsPlayer.setBowTension(tension.tension());
                            } else if (payload instanceof BowDrawCancelPayloadToServer) {
                                if (sender.isUsingItem()
                                        && sender.getUseItem().getItem() instanceof BowItem) {
                                    sender.stopUsingItem();
                                }
                            }
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

    private static VisorPayloadToServer readToServer(byte id, FriendlyByteBuf buffer) {
        return switch (id) {
            case BowTensionPayloadToServer.PAYLOAD_ID -> BowTensionPayloadToServer.read(buffer);
            case BowDrawCancelPayloadToServer.PAYLOAD_ID -> BowDrawCancelPayloadToServer.read(buffer);
            default -> null;
        };
    }
}