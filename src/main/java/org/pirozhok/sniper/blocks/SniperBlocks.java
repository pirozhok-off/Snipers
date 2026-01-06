package org.pirozhok.sniper.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static org.pirozhok.sniper.Sniper.MODID;

public class SniperBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Block> CASINOTABLE_1 = BLOCKS.register("casinotable_1", () -> new CasinoTable());
    public static final RegistryObject<Block> SLOTSCASINO_1 = BLOCKS.register("slots_casino1", () -> new SlotsCasino1());
    public static final RegistryObject<Block> SLOTSCASINO_2 = BLOCKS.register("slots_casino2", () -> new SlotsCasino2());
    public static final RegistryObject<Block> SLOTSCASINO_3 = BLOCKS.register("slots_casino3", () -> new SlotsCasino3());
    public static final RegistryObject<Block> SLOTSCASINO = BLOCKS.register("slots_casino", () -> new SlotsCasino());
    public static final RegistryObject<Block> POKERTABLE = BLOCKS.register("poker_table", () -> new PokerTable());
}
