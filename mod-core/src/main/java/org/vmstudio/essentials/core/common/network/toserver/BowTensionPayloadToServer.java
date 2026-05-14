package org.vmstudio.essentials.core.common.network.toserver;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;

public record BowTensionPayloadToServer(float tension) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(tension);
    }

    @Override
    public byte payloadId() {
        return 0;
    }

    public static BowTensionPayloadToServer read(FriendlyByteBuf buffer) {
        return new BowTensionPayloadToServer(buffer.readFloat());
    }
}