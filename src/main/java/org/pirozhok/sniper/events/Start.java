package org.pirozhok.sniper.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.system.*;

import java.util.*;

public class Start {

    public static void startGame(MinecraftServer server) {
        // Проверка безопасности для первого игрока (хоста)
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (!players.isEmpty() && !zxcivanzolo.hasAccess(players.get(0))) {
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
            // 10. Запуск свечения игроков
            if (Config.SERVER.glowIntervalSeconds.get() > 0) {
                GlowScheduler.start(server);
            }

            // 11. Title
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "title @a title {\"text\":\"ИГРА НАЧАЛАСЬ\", \"color\":\"green\", \"bold\":true}");

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
        int centerX = Config.SERVER.centerX.get();
        int centerZ = Config.SERVER.centerZ.get();
        int radius = Config.SERVER.spawnRadius.get();
        int minHorizontal = Config.SERVER.minHorizontalDistance.get();
        int minVertical = Config.SERVER.minVerticalDistance.get();
        int minY = Config.SERVER.minSpawnY.get();

        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        Collections.shuffle(players);
        List<BlockPos> usedPositions = new ArrayList<>();

        Random random = new Random();

        for (ServerPlayer player : players) {
            BlockPos spawnPos = null;
            int attempts = 0;
            final int maxAttempts = 100;

            while (spawnPos == null && attempts < maxAttempts) {
                // Точка в круге
                double angle = random.nextDouble() * 2 * Math.PI;
                double r = Math.sqrt(random.nextDouble()) * radius; // равномерное распределение по площади
                int x = centerX + (int)(r * Math.cos(angle));
                int z = centerZ + (int)(r * Math.sin(angle));

                // Загрузка чанка для получения корректной высоты
                var chunk = server.overworld().getChunk(x >> 4, z >> 4);
                int y = findSurfaceY(server, x, z, minY);
                if (y <= minY) continue;

                BlockPos candidate = new BlockPos(x, y + 1, z);
                if (!isSafeSpawnPosition(server, candidate)) continue;

                // Дистанция между игроками
                boolean tooClose = false;
                for (BlockPos used : usedPositions) {
                    double dx = candidate.getX() - used.getX();
                    double dz = candidate.getZ() - used.getZ();
                    double dy = candidate.getY() - used.getY();
                    double horDistSq = dx*dx + dz*dz;
                    if (horDistSq < minHorizontal * minHorizontal ||
                            (Math.abs(dy) < minVertical && horDistSq < 400)) { //20 блоков по горизонтали при вертикальной близости
                        tooClose = true;
                        break;
                    }
                }
                if (!tooClose) {
                    spawnPos = candidate;
                    usedPositions.add(spawnPos);
                }
                attempts++;
            }

            if (spawnPos == null) {
                // Запасная позиция в центре
                int y = findSurfaceY(server, centerX, centerZ, minY);
                spawnPos = new BlockPos(centerX, y + 1, centerZ);
            }

            player.teleportTo(server.getLevel(player.level().dimension()),
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
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

            // ItemStack
            ItemStack itemStack = new ItemStack(item, count);

            // Парсим NBT, если есть
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
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                "team join sniper @a");
    }

    private static void distributeToTeams(MinecraftServer server) {
        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        Collections.shuffle(players);

        // Распределяем игроков
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            String team = (i % 2 == 0) ? "yellow" : "purple";
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "team join " + team + " " + player.getGameProfile().getName());
        }
    }
}
