package org.vmstudio.essentials.core.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.common.EssentialsFeature;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.server.EssentialsServerSettings;


public class EssentialsSettingsScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int ROW_HEIGHT = 24;

    private final Screen parent;

    public EssentialsSettingsScreen(Screen parent) {
        super(Component.translatable(VisorEssentials.MOD_ID + ".gui.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = 48;
        for (EssentialsFeature feature : EssentialsClientSettings.getFeatures()) {
            addRenderableWidget(Button.builder(
                            toggleLabel(feature),
                            button -> {
                                feature.setEnabled(!feature.isEnabled());
                                EssentialsClientSettings.save();
                                button.setMessage(toggleLabel(feature));
                            })
                    .bounds(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, 20)
                    .tooltip(Tooltip.create(featureTooltip(feature)))
                    .build());
            y += ROW_HEIGHT;
        }

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.back"),
                        button -> onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
    }

    private Component featureTooltip(EssentialsFeature feature) {
        var tooltip = feature.getDescription().copy();
        if (feature == EssentialsClientSettings.getBetterBow()
                && !EssentialsServerSettings.isBetterBow()) {
            tooltip.append("\n").append(
                    Component.translatable(VisorEssentials.MOD_ID + ".gui.blocked_by_server")
                            .withStyle(ChatFormatting.RED)
            );
        }
        return tooltip;
    }

    private static Component toggleLabel(EssentialsFeature feature) {
        return feature.getDisplayName().copy()
                .append(": ")
                .append(feature.isEnabled()
                        ? CommonComponents.OPTION_ON
                        : CommonComponents.OPTION_OFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
    }
}
