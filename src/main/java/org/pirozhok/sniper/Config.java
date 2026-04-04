package org.pirozhok.sniper;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber
public class Config
{
    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static
    {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = specPair.getLeft();
        SERVER_SPEC = specPair.getRight();
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.ConfigValue<String> password;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> admins;

        // Настройки спавна
        public final ForgeConfigSpec.IntValue minSpawnY;
        public final ForgeConfigSpec.ConfigValue<Double> skySpawnX;
        public final ForgeConfigSpec.ConfigValue<Double> skySpawnY;
        public final ForgeConfigSpec.ConfigValue<Double> skySpawnZ;
        public final ForgeConfigSpec.IntValue centerX;
        public final ForgeConfigSpec.IntValue centerZ;
        public final ForgeConfigSpec.IntValue spawnRadius;
        public final ForgeConfigSpec.IntValue minHorizontalDistance;
        public final ForgeConfigSpec.IntValue minVerticalDistance;

        // Настройки лобби
        public final ForgeConfigSpec.ConfigValue<Double> lobbyX;
        public final ForgeConfigSpec.ConfigValue<Double> lobbyY;
        public final ForgeConfigSpec.ConfigValue<Double> lobbyZ;
        public final ForgeConfigSpec.ConfigValue<Double> adminLobbyX;
        public final ForgeConfigSpec.ConfigValue<Double> adminLobbyY;
        public final ForgeConfigSpec.ConfigValue<Double> adminLobbyZ;

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> itemsOnStart;

        // Координаты сундуков
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> chestCoordinates;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> chestItems;

        // Настройки геймрулов
        public final ForgeConfigSpec.ConfigValue<Boolean> doDaylightCycle;
        public final ForgeConfigSpec.ConfigValue<Boolean> doWeatherCycle;
        public final ForgeConfigSpec.ConfigValue<Boolean> keepInventory;
        public final ForgeConfigSpec.ConfigValue<Boolean> showDeathMessages;

        // Доп. моды
        public final ForgeConfigSpec.ConfigValue<Boolean> mpm;
        public final ForgeConfigSpec.ConfigValue<Boolean> coinmod;

        public final ForgeConfigSpec.IntValue glowIntervalSeconds;

        public ServerConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("security");
            password = builder
                    .comment("Пароль для работы мода. По умолчанию: default_password")
                    .define("password", "default_password");
            admins = builder
                    .comment("Список админов по никнеймам")
                    .defineList("admins", Arrays.asList("pirozhoK_off", "Admin2"), obj -> obj instanceof String);
            builder.pop();

            builder.push("spawn");
            minSpawnY = builder.defineInRange("minSpawnY", 23, -64, 320);
            skySpawnX = builder.define("skySpawnX", 94.0);
            skySpawnY = builder.define("skySpawnY", 150.0);
            skySpawnZ = builder.define("skySpawnZ", -77.0);
            centerX = builder.defineInRange("centerX", 0, -30000000, 30000000);
            centerZ = builder.defineInRange("centerZ", 0, -30000000, 30000000);
            spawnRadius = builder.defineInRange("spawnRadius", 1000, 100, 5000);
            minHorizontalDistance = builder.defineInRange("minHorizontalDistance", 30, 10, 100);
            minVerticalDistance = builder.defineInRange("minVerticalDistance", 8, 2, 20);
            builder.pop();

            builder.push("lobby");
            lobbyX = builder.define("lobbyX", 99.0);
            lobbyY = builder.define("lobbyY", -12.9);
            lobbyZ = builder.define("lobbyZ", -55.0);
            adminLobbyX = builder.define("adminLobbyX", 96.0);
            adminLobbyY = builder.define("adminLobbyY", -18.9);
            adminLobbyZ = builder.define("adminLobbyZ", -50.0);
            builder.pop();

            builder.push("chests");
            chestCoordinates = builder
                    .comment("Координаты сундуков (формат: x;y;z)")
                    .defineList("chestCoordinates", Arrays.asList(
                            "100;64;100",
                            "-100;64;-100",
                            "150;64;-150",
                            "-150;64;150"
                    ), obj -> obj instanceof String);
            chestItems = builder
                    .comment("Предметы для спавна в сундуках (формат: modid:item@count@nbt)")
                    .defineList("chestItems", Arrays.asList(
                            "tacz:pistol@1@{}",
                            "tacz:ammo@16@{}",
                            "tacz:rifle@1@{}",
                            "minecraft:golden_apple@3@{}",
                            "minecraft:ender_pearl@4@{}"
                    ), obj -> obj instanceof String);
            builder.pop();

            builder.push("guns");
            itemsOnStart = builder
                    .comment("Список предметов которые выдаются игрокам при старте игры. Формат: modid:item@count@nbt")
                    .defineList("itemsOnStart", Arrays.asList(
                            "tacz:modern_kinetic_gun@1@{AttachmentSCOPE:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tti_gunpack:scope_lpvo_1_6\",ZoomNumber:6}},GunCurrentAmmoCount:5,GunFireMode:\"SEMI\",GunId:\"tacz:ai_awp\",HasBulletInBarrel:1b}",
                            "tacz:modern_kinetic_gun@1@{AttachmentSCOPE:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tacz:sight_rmr_dot\"}},GunCurrentAmmoCount:12,GunFireMode:\"SEMI\",GunId:\"tacz:p320\",HasBulletInBarrel:1b}",
                            "tacz:ammo_box@1@{AllTypeCreative:1b}"
                    ), obj -> obj instanceof String);
            builder.pop();

            builder.push("gameplay");
            glowIntervalSeconds = builder
                    .comment("Интервал в секундах между выдачей эффекта свечения всем живым игрокам (0 - отключить)")
                    .defineInRange("glowIntervalSeconds", 30, 0, 300);
            builder.pop();

            builder.push("setup").comment("Для команды /sniper setup");
            doDaylightCycle = builder
                    .comment("Нужно автоматически запрещать смену дня и ночи? True - смена дня и ночи не будет изменена, false - будет отключена смена дня и ночи")
                    .define("doDaylightCycle", false);
            doWeatherCycle = builder
                    .comment("Нужно автоматически запрещать смену погоды? True - смена погоды не будет изменена, false - будет отключена смена погоды")
                    .define("doWeatherCycle", false);
            keepInventory = builder
                    .comment("Нужно автоматически запрещать смену погоды? True - сохранение инвентаря будет включено, false - сохранение инвентаря не будет изменено")
                    .define("keepInventory", false);
            showDeathMessages = builder
                    .comment("Показывать сообщение о смерти игрока? True = не менять эту настройку, false - игрокам не будет показыватся сообщение о смерти")
                    .define("showDeathMessages", true);
            builder.pop();

            builder.push("Additional mods").comment("Дополнительные моды");
            mpm = builder
                    .comment("Есть в сборке мод MorePlayerModels? По умолчанию: есть")
                    .define("mpm", true);
            coinmod = builder
                    .comment("Есть ли в сборке мод на золотую монетку от пирожКа? По умолчанию: есть")
                    .define("coinmod", true);
        }
    }
}
