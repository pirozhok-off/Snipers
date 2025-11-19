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
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> allowedCheaters;

        // Настройки спавна
        public final ForgeConfigSpec.ConfigValue<String> spawnMode;
        public final ForgeConfigSpec.IntValue minSpawnY;
        public final ForgeConfigSpec.ConfigValue<Double> skySpawnX;
        public final ForgeConfigSpec.ConfigValue<Double> skySpawnY;
        public final ForgeConfigSpec.ConfigValue<Double> skySpawnZ;

        // Настройки лобби
        public final ForgeConfigSpec.ConfigValue<Double> lobbyX;
        public final ForgeConfigSpec.ConfigValue<Double> lobbyY;
        public final ForgeConfigSpec.ConfigValue<Double> lobbyZ;
        public final ForgeConfigSpec.ConfigValue<Double> adminLobbyX;
        public final ForgeConfigSpec.ConfigValue<Double> adminLobbyY;
        public final ForgeConfigSpec.ConfigValue<Double> adminLobbyZ;

        // Настройки оружия и команд
        public final ForgeConfigSpec.ConfigValue<String> gunsMode;
        public final ForgeConfigSpec.ConfigValue<String> teamsMode;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> itemsOnStart;

        // Настройки границы мира
        public final ForgeConfigSpec.ConfigValue<Boolean> borderShrinkEnabled;
        public final ForgeConfigSpec.ConfigValue<String> borderShrinkMode;

        // Координаты сундуков
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> chestCoordinates;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> chestItems;

        public ServerConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("security");
            password = builder
                    .comment("Пароль для работы мода. По умолчанию: default_password")
                    .define("password", "default_password");
            admins = builder
                    .comment("Список админов по никнеймам")
                    .defineList("admins", Arrays.asList("pirozhoK_off", "Admin2"), obj -> obj instanceof String);
            allowedCheaters = builder
                    .comment("Список игроков с доступом к читам")
                    .defineList("allowedCheaters", Arrays.asList("pirozhoK_off", "Player2"), obj -> obj instanceof String);
            builder.pop();

            builder.push("spawn");
            spawnMode = builder.define("spawnMode", "random");
            minSpawnY = builder.defineInRange("minSpawnY", 23, -64, 320);
            skySpawnX = builder.define("skySpawnX", 94.0);
            skySpawnY = builder.define("skySpawnY", 150.0);
            skySpawnZ = builder.define("skySpawnZ", -77.0);
            builder.pop();

            builder.push("lobby");
            lobbyX = builder.define("lobbyX", 99.0);
            lobbyY = builder.define("lobbyY", -12.9);
            lobbyZ = builder.define("lobbyZ", -55.0);
            adminLobbyX = builder.define("adminLobbyX", 96.0);
            adminLobbyY = builder.define("adminLobbyY", -18.9);
            adminLobbyZ = builder.define("adminLobbyZ", -50.0);
            builder.pop();

            builder.push("game_settings");
            gunsMode = builder.define("gunsMode", "players");
            teamsMode = builder.define("teamsMode", "solo");
            borderShrinkEnabled = builder.define("borderShrinkEnabled", true);
            borderShrinkMode = builder.define("borderShrinkMode", "standard");
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
        }

        public static void register()
        {

        }
    }
}
