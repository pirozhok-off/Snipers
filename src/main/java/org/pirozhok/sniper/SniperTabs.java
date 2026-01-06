package org.pirozhok.sniper;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.pirozhok.sniper.blocks.SniperBlocks;
import org.pirozhok.sniper.items.SniperIco;
import org.pirozhok.sniper.items.SniperItems;

import static org.pirozhok.sniper.Sniper.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SniperTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sniper.MODID);
    public static final RegistryObject<CreativeModeTab> SNIPER_TAB = REGISTRY.register("sniper_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.sniper.sniper_tab")).icon(() -> new ItemStack(SniperItems.SNIPER.get())).displayItems((parameters, tabData) -> {

                        tabData.accept(SniperBlocks.CASINOTABLE_1.get().asItem());
                        tabData.accept(SniperBlocks.SLOTSCASINO_1.get().asItem());
                        tabData.accept(SniperBlocks.SLOTSCASINO_2.get().asItem());
                        tabData.accept(SniperBlocks.SLOTSCASINO_3.get().asItem());
                        tabData.accept(SniperBlocks.POKERTABLE.get().asItem());
                        tabData.accept(SniperBlocks.SLOTSCASINO.get().asItem());
                    })

                    .build());
}
