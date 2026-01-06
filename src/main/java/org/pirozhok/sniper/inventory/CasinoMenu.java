package org.pirozhok.sniper.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.SniperMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CasinoMenu extends AbstractContainerMenu {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public final BlockPos pos;
    private final ContainerLevelAccess access;

    private final ItemStackHandler currencyInputHandler = new ItemStackHandler(1);
    private final ItemStackHandler currencyOutputHandler = new ItemStackHandler(1);

    // Определяем валюту на основе конфига
    private Item currencyItem;

    public CasinoMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(SniperMenu.CASINO.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();

        if (extraData != null) {
            this.pos = extraData.readBlockPos();
            this.access = ContainerLevelAccess.create(world, pos);
        } else {
            this.pos = BlockPos.ZERO;
            this.access = ContainerLevelAccess.NULL;
        }

        // Определяем валюту
        determineCurrencyItem();

        this.addSlot(new SlotItemHandler(currencyInputHandler, 0, 25, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == currencyItem;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Слот для выигрыша (вывод)
        this.addSlot(new SlotItemHandler(currencyOutputHandler, 0, 130, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Нельзя класть предметы вручную
            }

            @Override
            public boolean mayPickup(Player player) {
                return true;
            }
        });

        // Слоты инвентаря игрока (стандартные)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Слоты хотбара
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    private void determineCurrencyItem() {
        // Если coinmod включен, используем монетку, иначе алмаз
        try {
            if (Config.SERVER.coinmod.get()) {
                // Пытаемся получить монетку из мода coinmod
                Class<?> coinModClass = Class.forName("org.pirozhok.coin.ModItems");
                Object coinField = coinModClass.getField("COIN").get(null);
                if (coinField instanceof net.minecraftforge.registries.RegistryObject) {
                    currencyItem = ((net.minecraftforge.registries.RegistryObject<Item>) coinField).get();
                }
            }
        } catch (Exception e) {
            // Если не получилось получить монетку, используем алмаз
            currencyItem = net.minecraft.world.item.Items.DIAMOND;
        }

        if (currencyItem == null) {
            currencyItem = net.minecraft.world.item.Items.DIAMOND;
        }
    }

    public void handleBet() {
        ItemStack betStack = currencyInputHandler.getStackInSlot(0);

        if (betStack.isEmpty() || betStack.getItem() != currencyItem) {
            return;
        }

        int betAmount = betStack.getCount();

        // Очищаем слот ставки
        currencyInputHandler.setStackInSlot(0, ItemStack.EMPTY);

        // Генерируем случайное число для определения выигрыша
        double random = Math.random();
        double multiplier = 0;

        // Определяем шансы (можно настроить)
        if (random < 0.6) { // 60% шанс проигрыша
            multiplier = 0;
        } else if (random < 0.85) { // 25% шанс x1.5
            multiplier = 1.5;
        } else if (random < 0.95) { // 10% шанс x2
            multiplier = 2.0;
        } else if (random < 0.99) { // 4% шанс x3
            multiplier = 3.0;
        } else { // 1% шанс джекпота x5
            multiplier = 5.0;
        }

        if (multiplier > 0) {
            int winAmount = (int) (betAmount * multiplier);

            // Создаем выигрышный стак
            ItemStack winStack = new ItemStack(currencyItem, winAmount);
            currencyOutputHandler.setStackInSlot(0, winStack);

            // Проигрыш звука при джекпоте
            if (multiplier >= 2.0) {
                world.playSound(null, pos,
                        net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        1.0F, 1.0F);
            }

            if (multiplier >= 3.0) {
                // Дополнительный эффект для джекпота
                world.playSound(null, pos,
                        net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        0.5F, 1.0F);
            }
        }
    }

    public void collectWinnings() {
        ItemStack winnings = currencyOutputHandler.getStackInSlot(0);
        if (!winnings.isEmpty()) {
            // Пытаемся добавить выигрыш в инвентарь
            if (!entity.getInventory().add(winnings)) {
                // Если не помещается, выкидываем на землю
                entity.drop(winnings, false);
            }
            currencyOutputHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 2) {
                // Слоты казино -> инвентарь
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                // Инвентарь -> слоты казино
                if (itemstack1.getItem() == currencyItem) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 29) {
                    // Основной инвентарь -> хотбар
                    if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 38) {
                    // Хотбар -> основной инвентарь
                    if (!this.moveItemStackTo(itemstack1, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public ItemStackHandler getInputHandler() {
        return currencyInputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return currencyOutputHandler;
    }

    public Item getCurrencyItem() {
        return currencyItem;
    }
}