package org.pirozhok.sniper.system;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pirozhok.sniper.Config;

@Mod.EventBusSubscriber
public class SecuritySystem {
    private static final String CORRECT_PASSWORD = "OperationAbuba123";

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        validatePassword();
    }

    public static boolean hasAccess(ServerPlayer player) {
        if (Config.SERVER == null) return false;

        // Админы имеют доступ без проверки пароля
        if (Config.SERVER.admins.get().contains(player.getGameProfile().getName())) {
            return true;
        }

        // Проверка пароля только для хоста
        if (isHost(player)) {
            String password = Config.SERVER.password.get();
            return CORRECT_PASSWORD.equals(password);
        }

        return false;
    }

    public static boolean canUseCheats(ServerPlayer player) {
        if (Config.SERVER == null) return false;
        return Config.SERVER.allowedCheaters.get().contains(player.getGameProfile().getName()) && hasAccess(player);
    }

    private static boolean isHost(ServerPlayer player) {
        return player.getServer() != null &&
                player.getServer().isSingleplayer() &&
                player.getServer().isSingleplayerOwner(player.getGameProfile());
    }

    public static void validatePassword() {
        if (Config.SERVER == null) {
            throw new RuntimeException("Конфигурация не загружена!");
        }

        String password = Config.SERVER.password.get();
        if (!CORRECT_PASSWORD.equals(password)) {
            throw new RuntimeException("Неверный пароль! Установите правильный пароль в конфигурационном файле.");
        }
    }
}