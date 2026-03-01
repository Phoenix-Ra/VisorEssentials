package org.vmstudio.essentials.loader.forge;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.essentials.core.client.EssentialsAddonClient;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.server.EssentialsAddonServer;
import net.minecraftforge.fml.common.Mod;

@Mod(VisorEssentials.MOD_ID)
public class VisorEssentialsMod {
    public VisorEssentialsMod(){
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
