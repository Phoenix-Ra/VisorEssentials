package org.vmstudio.essentials.core.client;

import lombok.Getter;
import org.vmstudio.essentials.core.common.EssentialsFeature;


public class EssentialsClientSettings {

    @Getter
    private static EssentialsFeature betterInventory = new EssentialsFeature("better_inventory");
    @Getter
    private static EssentialsFeature betterBow = new EssentialsFeature("better_bow");


}
