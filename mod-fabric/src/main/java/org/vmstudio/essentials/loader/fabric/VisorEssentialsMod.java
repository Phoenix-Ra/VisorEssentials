package org.vmstudio.essentials.loader.fabric;

import net.fabricmc.api.ModInitializer;
import org.vmstudio.essentials.core.client.EssentialsAddonClient;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.essentials.core.server.EssentialsAddonServer;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

public class VisorEssentialsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        VisorAddon addon = ModLoader.get().isDedicatedServer()
                ? new EssentialsAddonServer()
                : new EssentialsAddonClient();
        VisorAPI.registerAddon(addon);
    }
}