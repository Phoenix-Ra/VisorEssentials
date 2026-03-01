package org.vmstudio.essentials.loader.fabric;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.essentials.core.client.EssentialsAddonClient;
import org.vmstudio.essentials.core.server.EssentialsAddonServer;
import net.fabricmc.api.ModInitializer;

public class VisorEssentialsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        if(ModLoader.get().isDedicatedServer()){
            VisorAPI.registerAddon(
                    new EssentialsAddonServer()
            );
        }else{
            VisorAPI.registerAddon(
                    new EssentialsAddonClient()
            );
        }


    }
}
