package org.vmstudio.essentials.loader.neoforge;

import net.neoforged.fml.common.Mod;
import org.vmstudio.essentials.core.client.AddonEntryClient;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.server.AddonEntryDedicatedServer;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

@Mod(VisorEssentials.MOD_ID)
public class VisorEssentialsMod {
    public VisorEssentialsMod() {
        VisorAddon addon = ModLoader.get().isDedicatedServer()
                ? new AddonEntryDedicatedServer()
                : new AddonEntryClient();
        VisorAPI.registerAddon(addon);
    }
}
