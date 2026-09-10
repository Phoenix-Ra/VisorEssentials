package org.vmstudio.essentials.core.client;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.vmstudio.essentials.core.common.EssentialsFeature;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.server.EssentialsServerSettings;

import java.nio.file.Path;
import java.util.List;


public class EssentialsClientSettings {

    @Getter
    private static EssentialsFeature betterInventory = new EssentialsFeature("better_inventory");
    @Getter
    private static EssentialsFeature betterBow = new EssentialsFeature("better_bow");
    @Getter
    private static EssentialsFeature overlayFocusVisualizer = new EssentialsFeature("overlay_focus_visualizer");

    private static ConfigFile config;

    public static List<EssentialsFeature> getFeatures(){
        return List.of(betterInventory, betterBow, overlayFocusVisualizer);
    }


    public static boolean isBetterBowActive(){
        return betterBow.isEnabled()
                && EssentialsServerSettings.isBetterBow();
    }

    public static void load(){
        try {
            config = VisorEssentials.configManager().createConfigFile(
                    ConfigType.YAML,
                    "settings",
                    Path.of("settings.yml"),
                    false
            );
        } catch (Exception e) {
            AddonEntryClient.LOGGER.error(
                    "Failed to load VisorEssentials client settings", e);
            return;
        }
        for (EssentialsFeature feature : getFeatures()) {
            feature.setEnabled(config.getBoolOrDefault(
                    featureKey(feature),
                    feature.isEnabled()
            ));
        }
        //to sync config with fields
        save();
    }

    public static void save(){
        if (config == null) return;
        for (EssentialsFeature feature : getFeatures()) {
            config.set(featureKey(feature), feature.isEnabled());
        }
        try {
            config.save();
        } catch (Exception e) {
            AddonEntryClient.LOGGER.error(
                    "Failed to save VisorEssentials client settings", e);
        }
    }

    private static String featureKey(EssentialsFeature feature){
        return "features." + feature.getId();
    }
}
