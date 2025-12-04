package org.pirozhok.sniper.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.system.SecuritySystem;
import org.pirozhok.sniper.system.BorderShrinkingSystem;
import org.pirozhok.sniper.system.ChestSpawningSystem;
import org.pirozhok.sniper.system.States;

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
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "effect give @a minecraft:slow_falling 1 2");

            // 5. Установка границы мира
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "worldborder set 200");

            // 6. Спавн игроков
            String spawnMode = States.getSpawnMode();
            if ("sky".equals(spawnMode)) {
                teleportPlayersToSky(server);
            } else {
                teleportPlayersRandom(server);
            }

            // 7. Выдача оружия
            String gunsMode = States.getGunsMode();
            if ("players".equals(gunsMode)) {
                giveWeaponsToPlayers(server);
            } else {
                ChestSpawningSystem.spawnItemsInChests(server);
            }
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "give @a paraglider:paraglider 1");

            // 8. Распределение по командам
            String teamsMode = States.getTeamsMode();
            if ("solo".equals(teamsMode)) {
                setupSoloTeams(server);
            } else {
                distributeToTeams(server);
            }

            // 9. Запуск сужения области если включено
            if (States.isBorderShrinkEnabled()) {
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

    private static void giveWeaponsToPlayers(MinecraftServer server)
    {
        List<? extends String> itemList = Config.SERVER.itemsOnStart.get();
        if (itemList.isEmpty())
        {
            System.out.println("Список для выдачи предметов пуст!");
        }

        //Получаем всех игроков
        List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();

        for (ServerPlayer player : players)
        {
            giveItemsToPlayers(player, itemList);
        }


        /*
        // Выдача стартовых предметов через команды
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "give @a tacz:modern_kinetic_gun{AttachmentSCOPE:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tti_gunpack:scope_lpvo_1_6\",ZoomNumber:6}},GunCurrentAmmoCount:5,GunFireMode:\"SEMI\",GunId:\"tacz:ai_awp\",HasBulletInBarrel:1b} 1");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "give @a tacz:modern_kinetic_gun{AttachmentSCOPE:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tacz:sight_rmr_dot\"}},GunCurrentAmmoCount:12,GunFireMode:\"SEMI\",GunId:\"tacz:p320\",HasBulletInBarrel:1b} 1");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "give @a tacz:ammo_box{AllTypeCreative:1b} 1");

         */
    }

    private static void giveItemsToPlayers(ServerPlayer player, List<? extends String> itemList)
    {
        for (String itemString : itemList)
        {
            try
            {
                ItemStack itemStack = parseItemStack(itemString);
                if (!itemStack.isEmpty())
                {
                    //Выдача предмета в инвентарь
                    boolean added = player.getInventory().add(itemStack);
                    if (!added)
                    {
                        player.drop(itemStack, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static  ItemStack parseItemStack(String itemString)
    {
        try {
            String[] parts = itemString.split("@", 3);

            if (parts.length<1)
            {
                return ItemStack.EMPTY;
            }

            //Парс ID предмета
            String itemId = parts[0];
            ResourceLocation resourceLocation = new ResourceLocation(itemId);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

            if (item == null)
            {
                return ItemStack.EMPTY;
            }

            //Парс количества
            int count = 1;
            if (parts.length >= 2) {
                try {
                    count = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Неверный формат количества: " + parts[1]);
                    count = 1;
                }
            }

            // Создаем ItemStack
            ItemStack itemStack = new ItemStack(item, count);

            // Парсим NBT тег, если есть
            if (parts.length >= 3 && !parts[2].isEmpty()) {
                try {
                    CompoundTag nbt = TagParser.parseTag(parts[2]);
                    itemStack.setTag(nbt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return itemStack;
        } catch (Exception e)
        {
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
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
