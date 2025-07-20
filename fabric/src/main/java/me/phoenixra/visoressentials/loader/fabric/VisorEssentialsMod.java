package me.phoenixra.visoressentials.loader.fabric;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visoressentials.core.client.EssentialsAddonClient;
import me.phoenixra.visoressentials.core.server.EssentialsAddonServer;
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
