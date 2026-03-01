package org.vmstudio.essentials.core.common;

import org.vmstudio.essentials.core.client.EssentialsAddonClient;
import org.vmstudio.essentials.core.server.EssentialsAddonServer;

public interface VisorEssentials {
    String MOD_ID = "visor_essentials";
    String MOD_NAME = "VisorEssentials";

    static boolean isActive(){
        return EssentialsAddonClient.ACTIVE
                || EssentialsAddonServer.ACTIVE;
    }
}
