package org.pirozhok.sniper.system;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pirozhok.sniper.Config;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class BorderShrinkingSystem {
    private static final Map<MinecraftServer, BorderShrinkData> serverData = new HashMap<>();
    private static boolean enabled = false;

    public static void startShrinking(MinecraftServer server) {
        enabled = Config.SERVER.borderShrinkEnabled.get();
        if (enabled) {
            serverData.put(server, new BorderShrinkData());
        }
    }

    public static void stopShrinking() {
        enabled = false;
        serverData.clear();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !enabled) return;

        MinecraftServer server = event.getServer();
        BorderShrinkData data = serverData.get(server);

        if (data == null || server.getPlayerCount() == 0) return;

        data.tickCounter++;
        WorldBorder worldBorder = server.overworld().getWorldBorder();
        String mode = Config.SERVER.borderShrinkMode.get();

        switch (mode) {
            case "slow" -> handleSlowShrink(server, data, worldBorder);
            case "standard" -> handleStandardShrink(server, data, worldBorder);
            case "fast" -> handleFastShrink(server, data, worldBorder);
        }
    }

    private static void handleSlowShrink(MinecraftServer server, BorderShrinkData data, WorldBorder worldBorder) {
        // Через 3 минуты (3600 тиков) начинаем сужение: -2 блока в 35 секунд (700 тиков)
        if (data.tickCounter >= 3600 && (data.tickCounter - 3600) % 700 == 0) {
            double newSize = Math.max(1, worldBorder.getSize() - 2);
            worldBorder.setSize(newSize);
        }
    }

    private static void handleStandardShrink(MinecraftServer server, BorderShrinkData data, WorldBorder worldBorder) {
        // Через 2 минуты (2400 тиков) начинаем сужение: -7 блоков в минуту (1200 тиков)
        if (data.tickCounter >= 2400 && (data.tickCounter - 2400) % 1200 == 0) {
            double newSize = Math.max(1, worldBorder.getSize() - 7);
            worldBorder.setSize(newSize);
        }
    }

    private static void handleFastShrink(MinecraftServer server, BorderShrinkData data, WorldBorder worldBorder) {
        // Через 1 минуту (1200 тиков) начинаем сужение: -5 блоков в 40 секунд (800 тиков)
        if (data.tickCounter >= 1200 && (data.tickCounter - 1200) % 800 == 0) {
            double newSize = Math.max(1, worldBorder.getSize() - 5);
            worldBorder.setSize(newSize);
        }
    }

    private static class BorderShrinkData {
        public int tickCounter = 0;
    }
}