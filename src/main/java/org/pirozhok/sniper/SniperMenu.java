package org.pirozhok.sniper;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.pirozhok.sniper.inventory.CasinoMenu;

public class SniperMenu {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Sniper.MODID);
    public static final RegistryObject<MenuType<CasinoMenu>> CASINO = REGISTRY.register("casino", () -> IForgeMenuType.create(CasinoMenu::new));
}
