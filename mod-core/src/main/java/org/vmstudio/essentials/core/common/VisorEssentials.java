package org.vmstudio.essentials.core.common;

import org.vmstudio.essentials.core.client.AddonEntryClient;
import org.vmstudio.essentials.core.server.AddonEntryDedicatedServer;
import org.vmstudio.essentials.core.server.EssentialsServer;

public abstract class VisorEssentials {
    public static final String MOD_ID = "visor_essentials";
    public static final String MOD_NAME = "VisorEssentials";



    public static EssentialsServer SERVER;


    public static boolean isActive(){
        return AddonEntryClient.ACTIVE
                || AddonEntryDedicatedServer.ACTIVE;
    }
}
