package org.pirozhok.sniper.system;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.pirozhok.sniper.Config;

import java.util.List;

public class Setup
{
    public static void SetupGame (MinecraftServer server)
    {
        // Проверка безопасности для первого игрока (хоста)
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (!players.isEmpty() && !SecuritySystem.hasAccess(players.get(0))) {
            throw new RuntimeException("Недостаточно прав для запуска игры!");
        }

        try {
            Scoreboard scoreboard = server.getLevel(net.minecraft.world.level.Level.OVERWORLD).getScoreboard();

            Boolean daylightCycle = Config.SERVER.doDaylightCycle.get();
            Boolean weatherCycle = Config.SERVER.doWeatherCycle.get();
            Boolean keepInventory = Config.SERVER.keepInventory.get();
            Boolean showDeathMessages = Config.SERVER.showDeathMessages.get();

            // Доп моды
            Boolean mpm = Config.SERVER.mpm.get();

            // Нужно чтобы не засорять чат
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule sendCommandFeedback false"); // В конце класса sendCommandFeedback снова true

            // Установка базовых настроек для игры
            // Команды
            PlayerTeam lobbyTeam = scoreboard.getPlayerTeam("lobby");
            if (lobbyTeam == null) {
                lobbyTeam = scoreboard.addPlayerTeam("lobby");
                lobbyTeam.setNameTagVisibility(Team.Visibility.ALWAYS);
                lobbyTeam.setDisplayName(Component.literal("Lobby"));
                lobbyTeam.setColor(ChatFormatting.GRAY);
            }

            PlayerTeam adminTeam = scoreboard.getPlayerTeam("admin");
            if (adminTeam == null) {
                adminTeam = scoreboard.addPlayerTeam("admin");
                adminTeam.setNameTagVisibility(Team.Visibility.ALWAYS);
                adminTeam.setDisplayName(Component.literal("Admin"));
                adminTeam.setColor(ChatFormatting.DARK_PURPLE);
            }

            PlayerTeam yellowTeam = scoreboard.getPlayerTeam("yellow");
            if (yellowTeam == null) {
                yellowTeam = scoreboard.addPlayerTeam("yellow");
                yellowTeam.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
                yellowTeam.setColor(ChatFormatting.YELLOW);
                yellowTeam.setDisplayName(Component.literal("Yellow"));
            }

            PlayerTeam purpleTeam = scoreboard.getPlayerTeam("purple");
            if (purpleTeam == null) {
                purpleTeam = scoreboard.addPlayerTeam("purple");
                purpleTeam.setColor(ChatFormatting.LIGHT_PURPLE);
                purpleTeam.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
                purpleTeam.setDisplayName(Component.literal("Purple"));
            }

            PlayerTeam sniperTeam = scoreboard.getPlayerTeam("sniper");
            if (sniperTeam == null) {
                sniperTeam = scoreboard.addPlayerTeam("sniper");
                sniperTeam.setNameTagVisibility(Team.Visibility.NEVER);
                sniperTeam.setColor(ChatFormatting.GRAY);
                sniperTeam.setDisplayName(Component.literal("Sniper"));
            }

            PlayerTeam greenTeam = scoreboard.getPlayerTeam("green");
            if (greenTeam == null) {
                greenTeam = scoreboard.addPlayerTeam("green");
                greenTeam.setColor(ChatFormatting.DARK_GREEN);
                greenTeam.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
                greenTeam.setDisplayName(Component.literal("Green"));
            }

            PlayerTeam redTeam = scoreboard.getPlayerTeam("red");
            if (redTeam == null) {
                redTeam = scoreboard.addPlayerTeam("red");
                redTeam.setDisplayName(Component.literal("Red"));
                redTeam.setColor(ChatFormatting.RED);
                redTeam.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
            }

            // Скорборды
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "scoreboard objectives add death deathCount");
            // Сообщение что нужно сделать для перевода в ГМ3
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "tellraw @p [\"\",{\"color\":\"#ffff55\",\"text\":\"-------------------------\"},{\"color\":\"#ffff55\",\"text\":\"\\nСкорборд для смертей создан.\"},\"\\nДля того, чтобы игроков переводило в режим \",{\"color\":\"#55ffff\",\"text\":\"наблюдателя после смерти\"},\", нужно поставить 2 командных блока: \",{\"color\":\"#55ffff\",\"text\":\"цикличный (всегда активен) и цепной.\\n\"},{\"color\":\"#ffffff\",\"text\":\"В первый (цикличный) нужно ввести команду \"},{\"color\":\"#ff55ff\",\"text\":\"execute as @a[scores={death=1..}] at @s run gamemode spectator @s\"},\"\\nВо второй (цепной) нужно ввести команду \",{\"color\":\"#ff55ff\",\"text\":\"execute as @a[scores={death=1..}] at @s run scoreboard players reset @s death\\n\"},{\"color\":\"#ffff55\",\"text\":\"-------------------------\"}]");

            // Геймрулы
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule blockExplosionDropDecay false");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule announceAdvancements false");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule commandBlockOutput false");
            if (!daylightCycle) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                        "gamerule doDaylightCycle false");
            }
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule doImmediateRespawn true");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule doMobSpawning false");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule doVinesSpread false");
            if (!weatherCycle) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                        "gamerule doWeatherCycle false");
            }
            if (keepInventory) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                        "gamerule keepInventory true");
            }
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule mobGriefing false");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule reducedDebugInfo true");
            if (!showDeathMessages) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                        "gamerule showDeathMessages false");
            }
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule spawnRadius 1");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule tntExplosionDropDecay false");
            // Если есть мод MorePlayerModels
            if (mpm) {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                        "gamerule mpmAllowEntityModels false");
            }

            // Возвращение sendCommandFeedback обратно в true
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "gamerule sendCommandFeedback true");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при установке базовых настроек: " + e.getMessage(), e);
        }
    }
}
