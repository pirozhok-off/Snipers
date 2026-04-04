package org.pirozhok.sniper;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pirozhok.sniper.blocks.SniperBlocks;
import org.pirozhok.sniper.commands.SniperCommands;
import org.pirozhok.sniper.items.SniperItems;
import org.pirozhok.sniper.networking.ModNetwork;
import org.slf4j.Logger;

@Mod(Sniper.MODID)
public class Sniper
{

    public static final String MODID = "sniper";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Sniper() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC, "sniper-server.toml");

        SniperTabs.REGISTRY.register(bus);
        SniperBlocks.BLOCKS.register(bus);
        SniperItems.ITEMS.register(bus);
        SniperMenu.REGISTRY.register(bus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SniperCommands.register(event.getDispatcher());
    }

}