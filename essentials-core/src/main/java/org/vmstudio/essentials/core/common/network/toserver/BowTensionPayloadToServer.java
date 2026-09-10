package org.vmstudio.essentials.core.common.network.toserver;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;

public record BowTensionPayloadToServer(float tension) implements VisorPayloadToServer {
    public static final byte PAYLOAD_ID = 0;

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(tension);
    }

    @Override
    public byte payloadId() {
        return PAYLOAD_ID;
    }

    public static BowTensionPayloadToServer read(FriendlyByteBuf buffer) {
        return new BowTensionPayloadToServer(buffer.readFloat());
    }
}