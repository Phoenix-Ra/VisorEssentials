package me.phoenixra.visoressentials.loader.forge;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visoressentials.core.client.EssentialsAddonClient;
import me.phoenixra.visoressentials.core.common.VisorEssentials;
import me.phoenixra.visoressentials.core.server.EssentialsAddonServer;
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
