package org.pirozhok.sniper.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.system.SecuritySystem;
import org.pirozhok.sniper.system.BorderShrinkingSystem;
import org.pirozhok.sniper.system.ChestSpawningSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Start {

    public static void startGame(MinecraftServer server) {
        // Проверка безопасности для первого игрока (хоста)
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (!players.isEmpty() && !SecuritySystem.hasAccess(players.get(0))) {
            throw new RuntimeException("Недостаточно прав для запуска игры!");
        }

        try {
            // 1. Выход из всех команд
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team leave @a");

            // 2. Очистка инвентаря
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "clear @a");

            // 3. Режим приключений
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode adventure @a");

            // 4. Исцеление и левитация
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "effect give @a minecraft:instant_health 1 10");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "effect give @a minecraft:levitation 1 2");

            // 5. Установка границы мира
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "worldborder set 200");

            // 6. Спавн игроков
            String spawnMode = Config.SERVER.spawnMode.get();
            if ("sky".equals(spawnMode)) {
                teleportPlayersToSky(server);
            } else {
                teleportPlayersRandom(server);
            }

            // 7. Выдача оружия
            String gunsMode = Config.SERVER.gunsMode.get();
            if ("players".equals(gunsMode)) {
                giveWeaponsToPlayers(server);
            } else {
                ChestSpawningSystem.spawnItemsInChests(server);
            }

            // 8. Распределение по командам
            String teamsMode = Config.SERVER.teamsMode.get();
            if ("solo".equals(teamsMode)) {
                setupSoloTeams(server);
            } else {
                distributeToTeams(server);
            }

            // 9. Запуск сужения области если включено
            if (Config.SERVER.borderShrinkEnabled.get()) {
                BorderShrinkingSystem.startShrinking(server);
            }

            // 10. Title и звук
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "title @a title {\"text\":\"ИГРА НАЧАЛАСЬ\", \"color\":\"green\", \"bold\":true}");

            // Проигрывание кастомного звука (пример)
           /* server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "playsound sniper:game_start master @a");
           */

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при запуске игры: " + e.getMessage(), e);
        }
    }

    private static void teleportPlayersToSky(MinecraftServer server) {
        double x = Config.SERVER.skySpawnX.get();
        double y = Config.SERVER.skySpawnY.get();
        double z = Config.SERVER.skySpawnZ.get();

        server.getPlayerList().getPlayers().forEach(player -> {
            player.teleportTo(server.getLevel(player.level().dimension()), x, y, z,
                    player.getYRot(), player.getXRot());
        });
    }

    private static void teleportPlayersRandom(MinecraftServer server) {
        int minY = Config.SERVER.minSpawnY.get();
        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        Collections.shuffle(players);

        Random random = new Random();
        int worldBorder = (int) server.overworld().getWorldBorder().getSize();
        int halfBorder = worldBorder / 2;

        for (ServerPlayer player : players) {
            boolean foundValidPosition = false;
            int attempts = 0;

            while (!foundValidPosition && attempts < 50) {
                int x = random.nextInt(worldBorder) - halfBorder;
                int z = random.nextInt(worldBorder) - halfBorder;

                // Находим высоту поверхности
                int y = findSurfaceY(server, x, z, minY);

                if (y > minY) {
                    BlockPos spawnPos = new BlockPos(x, y + 1, z);
                    if (isSafeSpawnPosition(server, spawnPos)) {
                        player.teleportTo(server.getLevel(player.level().dimension()),
                                x + 0.5, y + 1, z + 0.5,
                                player.getYRot(), player.getXRot());
                        foundValidPosition = true;
                    }
                }
                attempts++;
            }

            // Если не нашли валидную позицию, телепортируем в центр
            if (!foundValidPosition) {
                player.teleportTo(server.getLevel(player.level().dimension()),
                        0, minY + 10, 0, player.getYRot(), player.getXRot());
            }
        }
    }

    private static int findSurfaceY(MinecraftServer server, int x, int z, int minY) {
        var level = server.overworld();
        // Ищем от максимальной высоты вниз до minY
        for (int y = level.getMaxBuildHeight(); y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return y;
            }
        }
        return minY;
    }

    private static boolean isSafeSpawnPosition(MinecraftServer server, BlockPos pos) {
        var level = server.overworld();
        // Проверяем, что блок под ногами твердый, а сам блок и блок выше - воздушные
        return !level.getBlockState(pos.below()).isAir() &&
                level.isEmptyBlock(pos) &&
                level.isEmptyBlock(pos.above());
    }

    private static void giveWeaponsToPlayers(MinecraftServer server) {
        // Выдача стартовых предметов через команды
        // Пример для мода TaCZ - замени на актуальные предметы
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "give @a tacz:pistol 1");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "give @a tacz:ammo 32");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "give @a minecraft:bread 16");
    }

    private static void setupSoloTeams(MinecraftServer server) {
        // Создаем команду sniper если не существует
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "team add sniper \"{\\\"text\\\":\\\"Sniper\\\",\\\"color\\\":\\\"white\\\"}\"");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "team join sniper @a");
    }

    private static void distributeToTeams(MinecraftServer server) {
        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        Collections.shuffle(players);

        // Создаем команды
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "team add yellow \"{\\\"text\\\":\\\"Yellow\\\",\\\"color\\\":\\\"yellow\\\"}\"");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "team add purple \"{\\\"text\\\":\\\"Purple\\\",\\\"color\\\":\\\"purple\\\"}\"");

        // Распределяем игроков
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            String team = (i % 2 == 0) ? "yellow" : "purple";
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "team join " + team + " " + player.getGameProfile().getName());
        }
    }
}