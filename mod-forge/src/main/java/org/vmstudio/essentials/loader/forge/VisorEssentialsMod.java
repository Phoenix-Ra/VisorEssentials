package org.vmstudio.essentials.loader.forge;

import net.minecraftforge.fml.common.Mod;
import org.vmstudio.essentials.core.client.EssentialsAddonClient;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.common.network.EssentialsChannel;
import org.vmstudio.essentials.core.server.EssentialsAddonServer;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

@Mod(VisorEssentials.MOD_ID)
public class VisorEssentialsMod {
    public VisorEssentialsMod() {
        VisorAddon addon = ModLoader.get().isDedicatedServer()
                ? new EssentialsAddonServer()
                : new EssentialsAddonClient();
        VisorAPI.registerAddon(addon);
    }
}