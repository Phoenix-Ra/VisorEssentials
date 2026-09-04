package org.vmstudio.essentials.core.common.network.toserver;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;


public record BowDrawCancelPayloadToServer() implements VisorPayloadToServer {
    public static final byte PAYLOAD_ID = 1;

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        //empty
    }

    @Override
    public byte payloadId() {
        return PAYLOAD_ID;
    }

    public static BowDrawCancelPayloadToServer read(FriendlyByteBuf buffer) {
        return new BowDrawCancelPayloadToServer();
    }
}
