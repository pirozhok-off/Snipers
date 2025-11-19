package org.pirozhok.sniper.cheats;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.pirozhok.sniper.networking.CheatPacket;
import org.pirozhok.sniper.networking.ModNetwork;

public class CheatsScreen extends Screen {

    public CheatsScreen() {
        super(Component.literal("Читы"));
    }

    @Override
    protected void init() {
        super.init();

        // Кнопка невидимости
        this.addRenderableWidget(Button.builder(Component.literal("Невидимость 5 сек"), button -> {
                    ModNetwork.sendToServer(new CheatPacket(1));
                    this.onClose();
                })
                .bounds(this.width / 2 - 100, this.height / 2 - 40, 200, 20)
                .build());

        // Кнопка дополнительных сердец
        this.addRenderableWidget(Button.builder(Component.literal("+10 сердец"), button -> {
                    ModNetwork.sendToServer(new CheatPacket(2));
                    this.onClose();
                })
                .bounds(this.width / 2 - 100, this.height / 2 - 15, 200, 20)
                .build());

        // Кнопка скорости
        this.addRenderableWidget(Button.builder(Component.literal("Скорость II (6 сек)"), button -> {
                    ModNetwork.sendToServer(new CheatPacket(3));
                    this.onClose();
                })
                .bounds(this.width / 2 - 100, this.height / 2 + 10, 200, 20)
                .build());
    }

    /*@Override
    public void render(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
    */
}