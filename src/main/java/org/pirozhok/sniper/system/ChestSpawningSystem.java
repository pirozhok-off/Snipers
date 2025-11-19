package org.pirozhok.sniper.system;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.pirozhok.sniper.Config;

import java.util.List;

public class ChestSpawningSystem {

    public static void spawnItemsInChests(MinecraftServer server) {
        List<? extends String> chestCoords = Config.SERVER.chestCoordinates.get();

        for (String coord : chestCoords) {
            try {
                String[] parts = coord.split(";");
                if (parts.length != 3) continue;

                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());

                BlockPos pos = new BlockPos(x, y, z);
                var level = server.getLevel(server.overworld().dimension());

                if (level != null) {
                    var blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof ChestBlockEntity chest) {
                        fillChestWithItems(chest);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка при обработке сундука с координатами: " + coord);
            }
        }
    }

    private static void fillChestWithItems(ChestBlockEntity chest) {
        // Очищаем сундук
        for (int i = 0; i < chest.getContainerSize(); i++) {
            chest.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
        }

        List<? extends String> itemsConfig = Config.SERVER.chestItems.get();
        java.util.Random random = new java.util.Random();

        // Заполняем 3-6 случайными предметами
        int itemsToAdd = random.nextInt(4) + 3;
        for (int i = 0; i < itemsToAdd; i++) {
            if (itemsConfig.isEmpty()) continue;

            String itemConfig = itemsConfig.get(random.nextInt(itemsConfig.size()));
            net.minecraft.world.item.ItemStack stack = parseItemStack(itemConfig);

            if (!stack.isEmpty()) {
                int slot = findEmptySlot(chest, random);
                if (slot != -1) {
                    chest.setItem(slot, stack);
                }
            }
        }
    }

    private static net.minecraft.world.item.ItemStack parseItemStack(String config) {
        try {
            String[] parts = config.split("@");
            String itemId = parts[0].trim();
            int count = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;

            var item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation(itemId));

            if (item != null) {
                return new net.minecraft.world.item.ItemStack(item, count);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга предмета: " + config);
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private static int findEmptySlot(ChestBlockEntity chest, java.util.Random random) {
        // Пытаемся найти случайный пустой слот
        for (int i = 0; i < 10; i++) {
            int slot = random.nextInt(chest.getContainerSize());
            if (chest.getItem(slot).isEmpty()) {
                return slot;
            }
        }
        // Если не нашли, ищем любой пустой
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}