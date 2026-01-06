package org.pirozhok.sniper.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SniperIco extends Item {
    public SniperIco() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
    }
}
