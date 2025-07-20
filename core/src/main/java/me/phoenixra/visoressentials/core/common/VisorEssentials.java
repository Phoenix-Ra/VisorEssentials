package me.phoenixra.visoressentials.core.common;

import me.phoenixra.visoressentials.core.client.EssentialsAddonClient;
import me.phoenixra.visoressentials.core.server.EssentialsAddonServer;

public interface VisorEssentials {
    String MOD_ID = "visor_essentials";
    String MOD_NAME = "VisorEssentials";

    static boolean isActive(){
        return EssentialsAddonClient.ACTIVE
                || EssentialsAddonServer.ACTIVE;
    }
}
