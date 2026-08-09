package org.vmstudio.essentials.core.common;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
@Getter
public class EssentialsFeature {
    private final String id;
    @Setter
    private boolean enabled = true;

    public EssentialsFeature(@NotNull String id){
        this.id = id;
    }



    public Component getDisplayName() {
        return Component.translatable(
                VisorEssentials.MOD_ID+".feature." + getId() + ".name"
        );
    }
    public Component getDescription() {
        return Component.translatable(
                VisorEssentials.MOD_ID+".feature." + getId() + ".desc");
    }


}
