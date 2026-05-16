package org.vmstudio.essentials.loader.fabric;

import net.fabricmc.api.ModInitializer;
import org.vmstudio.essentials.core.client.AddonEntryClient;
import org.vmstudio.essentials.core.server.AddonEntryDedicatedServer;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

public class VisorEssentialsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        VisorAddon addon = ModLoader.get().isDedicatedServer()
                ? new AddonEntryDedicatedServer()
                : new AddonEntryClient();
        VisorAPI.registerAddon(addon);
    }
}