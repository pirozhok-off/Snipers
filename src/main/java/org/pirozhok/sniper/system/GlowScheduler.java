package org.pirozhok.sniper.system;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.pirozhok.sniper.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GlowScheduler {
    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> task;
    private static MinecraftServer server;

    public static void start(MinecraftServer serverInstance) {
        if (task != null) stop();
        int interval = Config.SERVER.glowIntervalSeconds.get();
        if (interval <= 0) return;

        server = serverInstance;
        executor = Executors.newSingleThreadScheduledExecutor();
        task = executor.scheduleAtFixedRate(() -> {
            if (server.isRunning() && States.isGameIsRunning()) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    // Проверка, жив ли игрок
                    if (!player.isSpectator() && player.isAlive()) {
                        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                    }
                }
            }
        }, interval, interval, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        server = null;
    }
}