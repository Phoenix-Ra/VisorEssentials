package me.phoenixra.visoressentials.core.mixin;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;

import me.phoenixra.visoressentials.core.common.VisorEssentials;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.mojang.text2speech.Narrator.LOGGER;

public class MixinConfig implements IMixinConfigPlugin {

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void onLoad(String mixinPackage) {
    }


    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!ModLoader.get().isModLoaded(VisorEssentials.MOD_ID)) {
            LOGGER.info("VisorEssentials failed to load, canceled applying mixin '{}'", mixinClassName);
            return false;
        }


        return true;
    }
}
