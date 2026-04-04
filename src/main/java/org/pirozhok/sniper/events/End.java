package org.pirozhok.sniper.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.system.BorderShrinkingSystem;
import org.pirozhok.sniper.system.GlowScheduler;

import java.util.*;

public class End
{

    public static void endGame(MinecraftServer server)
    {
        try {
            // 1. Очистка инвентаря
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "clear @a");

            // 2. Выход из всех команд и распределение по лобби/админ командам
            setupLobbyTeams(server);

            // 3. Режим приключений
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode adventure @a");

            // 4. Телепортация в лобби
            teleportToLobbies(server);

            // 5. Исцеление
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "effect give @a minecraft:instant_health 1 1");

            // 6. Остановка сужения границы
            BorderShrinkingSystem.stopShrinking();

            // 7. Остановка свечения
            GlowScheduler.stop();

            // 8. Очистка сундуков
            clearChests(server);

            // 9. Очистка предметов
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "kill @e[type=item]");

        } catch (Exception e)
        {
            throw new RuntimeException("Ошибка при завершении игры: " + e.getMessage(), e);
        }
    }

    private static void setupLobbyTeams(MinecraftServer server) {


        List<String> adminNames = (List<String>) Config.SERVER.admins.get();

        // Сначала всех в лобби
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team join lobby @a");

        // Затем админов в админ команду
        for (String adminName : adminNames) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "team join admin " + adminName);
        }
    }

    private static void teleportToLobbies(MinecraftServer server) {
        double lobbyX = Config.SERVER.lobbyX.get();
        double lobbyY = Config.SERVER.lobbyY.get();
        double lobbyZ = Config.SERVER.lobbyZ.get();

        double adminLobbyX = Config.SERVER.adminLobbyX.get();
        double adminLobbyY = Config.SERVER.adminLobbyY.get();
        double adminLobbyZ = Config.SERVER.adminLobbyZ.get();

        List<String> adminNames = (List<String>) Config.SERVER.admins.get();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String playerName = player.getGameProfile().getName();

            if (adminNames.contains(playerName)) {
                // Телепортация в админ-лобби
                player.teleportTo(server.getLevel(player.level().dimension()),
                        adminLobbyX, adminLobbyY, adminLobbyZ,
                        player.getYRot(), player.getXRot());
            } else {
                // Телепортация в обычное лобби
                player.teleportTo(server.getLevel(player.level().dimension()),
                        lobbyX, lobbyY, lobbyZ,
                        player.getYRot(), player.getXRot());
            }
        }
    }

    private static void clearChests(MinecraftServer server) {
        List<? extends String> chestCoords = Config.SERVER.chestCoordinates.get();
        var level = server.overworld();
        for (String coord : chestCoords) {
            try {
                String[] parts = coord.split(";");
                if (parts.length != 3) continue;
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());
                BlockPos pos = new BlockPos(x, y, z);
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ChestBlockEntity chest) {
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        chest.setItem(i, ItemStack.EMPTY);
                    }
                    chest.setChanged();
                }
            } catch (Exception e) {
                System.err.println("Ошибка очистки сундука: " + coord);
            }
        }
    }
}
