package org.pirozhok.sniper.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.pirozhok.sniper.blocks.SniperBlocks;

import static org.pirozhok.sniper.Sniper.MODID;

public class SniperItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> SNIPER = ITEMS.register("sniper", () -> new SniperIco());
    public static final RegistryObject<Item> CASINOTABLE_1 = block(SniperBlocks.CASINOTABLE_1);
    public static final RegistryObject<Item> SLOTSCASINO_3 = block(SniperBlocks.SLOTSCASINO_3);
    public static final RegistryObject<Item> SLOTSCASINO_1 = block(SniperBlocks.SLOTSCASINO_1);
    public static final RegistryObject<Item> SLOTSCASINO_2 = block(SniperBlocks.SLOTSCASINO_2);
    public static final RegistryObject<Item> SLOTSCASINO = block(SniperBlocks.SLOTSCASINO);
    public static final RegistryObject<Item> POKERTABLE = block(SniperBlocks.POKERTABLE);

    private static RegistryObject<Item> block(RegistryObject<Block> block)
    {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
