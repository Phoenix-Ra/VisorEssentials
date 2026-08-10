package org.vmstudio.essentials.core.common.network.toclient;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.nio.charset.StandardCharsets;


public record SettingsPayloadToClient(String config) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBytes(config.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte payloadId() {
        return 0;
    }

    public static SettingsPayloadToClient read(FriendlyByteBuf buffer) {
        return new SettingsPayloadToClient(VisorPayload.readString(buffer));
    }
}
