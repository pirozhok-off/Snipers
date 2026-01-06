package org.pirozhok.sniper.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.pirozhok.sniper.inventory.CasinoMenu;
import org.pirozhok.sniper.networking.ModNetwork;
import org.pirozhok.sniper.networking.packets.CasinoBetPacket;
import org.pirozhok.sniper.networking.packets.CasinoCollectPacket;

import java.util.HashMap;

public class CasinoScreen extends AbstractContainerScreen<CasinoMenu> {
    private final static HashMap<String, Object> guistate = CasinoMenu.guistate;
    private final Level world;
    private final BlockPos pos;
    private final Player entity;

    private Button betButton;
    private Button collectButton;

    public CasinoScreen(CasinoMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.pos = container.pos;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private static final ResourceLocation texture = new ResourceLocation("sniper:textures/screens/casino.png");

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Отображаем информацию о валюте вверху
        String currencyName = menu.getCurrencyItem().getDescription().getString();
        guiGraphics.drawString(this.font, "Валюта: " + currencyName,
                this.leftPos + 8, this.topPos + 62, 0x049c02, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void init() {
        super.init();

        // Кнопка додепа
        this.betButton = Button.builder(Component.translatable("gui.button.sniper.dodep"), button -> {
                    // Отправляем пакет на сервер для обработки ставки
                    ModNetwork.CHANNEL.sendToServer(new CasinoBetPacket(this.pos));
                })
                .bounds(this.leftPos + 57, this.topPos + 40, 54, 20)
                .build();
        this.addRenderableWidget(this.betButton);

        // Кнопка "Забрать"
        this.collectButton = Button.builder(Component.translatable("gui.button.sniper.getdep"), button -> {
                    // Отправляем пакет на сервер для сбора выигрыша
                    ModNetwork.CHANNEL.sendToServer(new CasinoCollectPacket(this.pos));
                })
                .bounds(this.leftPos + 116, this.topPos + 55, 54, 20)
                .build();
        this.addRenderableWidget(this.collectButton);

        // Кнопка с шансами
        this.addRenderableWidget(Button.builder(Component.translatable("gui.sniper.chanses"), button -> {
                    entity.sendSystemMessage(Component.translatable("gui.sniper.chanses_casino"));
                    entity.sendSystemMessage(Component.translatable("gui.sniper.chanse_x0"));
                    entity.sendSystemMessage(Component.translatable("gui.sniper.chanse_x15"));
                    entity.sendSystemMessage(Component.translatable("gui.sniper.chanse_x2"));
                    entity.sendSystemMessage(Component.translatable("gui.sniper.chanse_x3"));
                    entity.sendSystemMessage(Component.translatable("gui.sniper.chanse_x5"));
                })
                .bounds(this.leftPos + 130, this.topPos + 5, 40, 20) // Ширина 40 вместо 30
                .build());
    }
}