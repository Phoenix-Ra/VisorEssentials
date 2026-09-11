package org.vmstudio.essentials.core.client.gui.screens;

import com.google.common.collect.Ordering;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class VRInvEffectInvScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    //? if >=1.20.2 {
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE =
            McVersionUtils.newResourceLoc("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE =
            McVersionUtils.newResourceLoc("container/inventory/effect_background_small");
    //?}

    @Setter
    @Getter
    protected boolean fullInventory;
    protected boolean hasEffects;
    public VRInvEffectInvScreen(AbstractContainerMenu menu,
                                Inventory inventory,
                                Component component) {
        super(menu, inventory, component);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderEffects(guiGraphics, mouseX, mouseY);
    }

    private void renderEffects(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startPos = this.leftPos + this.imageWidth + 2;
        // [-- Modified
        startPos -= - (fullInventory ? 0 : 36);
        // --]
        int j = this.width - startPos;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean bl = j >= 120;
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
            // [-- Modified
            hasEffects = iterable.iterator().hasNext();
            // --]
            this.renderBackgrounds(guiGraphics, startPos, k, iterable, bl);
            this.renderIcons(guiGraphics, startPos, k, iterable, bl);
            if (bl) {
                this.renderLabels(guiGraphics, startPos, k, iterable);
            } else if (mouseX >= startPos && mouseX <= startPos + 33) {
                int l = this.topPos;
                MobEffectInstance mobEffectInstance = null;

                for(MobEffectInstance mobEffectInstance2 : iterable) {
                    if (mouseY >= l && mouseY <= l + k) {
                        mobEffectInstance = mobEffectInstance2;
                    }

                    l += k;
                }

                if (mobEffectInstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobEffectInstance), this.getEffectDuration(mobEffectInstance));
                    guiGraphics.renderTooltip(this.font, list, Optional.empty(), mouseX, mouseY);
                }
            }

        }else{
            hasEffects = false;
        }
    }


    //---Nothing modified below
    private void renderBackgrounds(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        int i = this.topPos;

        for(MobEffectInstance mobEffectInstance : effects) {
            //? if <1.20.2 {
            /*if (isSmall) {
                guiGraphics.blit(INVENTORY_LOCATION, renderX, i, 0, 166, 120, 32);
            } else {
                guiGraphics.blit(INVENTORY_LOCATION, renderX, i, 0, 198, 32, 32);
            }
            *///?} else {
            if (isSmall) {
                guiGraphics.blitSprite(EFFECT_BACKGROUND_LARGE_SPRITE, renderX, i, 120, 32);
            } else {
                guiGraphics.blitSprite(EFFECT_BACKGROUND_SMALL_SPRITE, renderX, i, 32, 32);
            }
            //?}

            i += yOffset;
        }

    }

    private void renderIcons(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for(MobEffectInstance mobEffectInstance : effects) {
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffectInstance.getEffect());
            guiGraphics.blit(renderX + (isSmall ? 6 : 7), i + 7, 0, 18, 18, textureAtlasSprite);
            i += yOffset;
        }

    }

    private void renderLabels(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects) {
        int i = this.topPos;

        for(MobEffectInstance mobEffectInstance : effects) {
            Component component = this.getEffectName(mobEffectInstance);
            guiGraphics.drawString(this.font, component, renderX + 10 + 18, i + 6, 16777215);
            Component component2 = this.getEffectDuration(mobEffectInstance);
            guiGraphics.drawString(this.font, component2, renderX + 10 + 18, i + 6 + 10, 8355711);
            i += yOffset;
        }

    }

    private Component getEffectDuration(MobEffectInstance effect) {
        //? if <1.20.3 {
        /*return MobEffectUtil.formatDuration(effect, 1.0F);
        *///?} else {
        // 1.20.3 made the displayed duration follow the world's tick rate
        return MobEffectUtil.formatDuration(effect, 1.0F, this.minecraft.level.tickRateManager().tickrate());
        //?}
    }

    //? if <1.20.5 {
    /*private Component getEffectName(MobEffectInstance effect) {
        MutableComponent mutableComponent = effect.getEffect().getDisplayName().copy();
    *///?} else {
    private Component getEffectName(MobEffectInstance effect) {
        MutableComponent mutableComponent = effect.getEffect().value().getDisplayName().copy();
    //?}
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            MutableComponent var10000 = mutableComponent.append(CommonComponents.SPACE);
            int var10001 = effect.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutableComponent;
    }




    public boolean canSeeEffects() {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        return j >= 32;
    }






}
