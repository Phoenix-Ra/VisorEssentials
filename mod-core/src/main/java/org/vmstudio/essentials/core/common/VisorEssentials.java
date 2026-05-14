package org.vmstudio.essentials.core.common;

import net.minecraft.client.Minecraft;
import org.vmstudio.essentials.core.client.EssentialsAddonClient;
import org.vmstudio.essentials.core.server.EssentialsAddonServer;

public abstract class VisorEssentials {
    public static final String MOD_ID = "visor_essentials";
    public static final String MOD_NAME = "VisorEssentials";

    public static Minecraft MC;

    public static boolean isActive(){
        return EssentialsAddonClient.ACTIVE
                || EssentialsAddonServer.ACTIVE;
    }
}
