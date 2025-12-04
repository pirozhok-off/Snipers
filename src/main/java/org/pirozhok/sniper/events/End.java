package org.pirozhok.sniper.events;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.system.BorderShrinkingSystem;

import java.util.*;

public class End
{

    public static void endGame(MinecraftServer server)
    {
        try {
            // 1. Очистка инвентаря
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "clear @a");

            // 2. Выход из всех команд и распределение по лобби/админ командам
            setupLobbyTeams(server);

            // 3. Режим приключений
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "gamemode adventure @a");

            // 4. Телепортация в лобби
            teleportToLobbies(server);

            // 5. Исцеление
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "effect give @a minecraft:instant_health 1 1");

            // 6. Остановка сужения границы
            BorderShrinkingSystem.stopShrinking();

            // 7. Очищение чата
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            {
                player.connection.send(new ClientboundSystemChatPacket(Component.literal("\n\n\n\n\n\n\n"), false));
            }

        } catch (Exception e)
        {
            throw new RuntimeException("Ошибка при завершении игры: " + e.getMessage(), e);
        }
    }

    private static void setupLobbyTeams(MinecraftServer server) {
        Scoreboard scoreboard = server.getLevel(net.minecraft.world.level.Level.OVERWORLD).getScoreboard();

        // Создаем команды если не существуют
        PlayerTeam lobbyTeam = scoreboard.getPlayerTeam("lobby");
        if (lobbyTeam == null) {
            lobbyTeam = scoreboard.addPlayerTeam("lobby");
            lobbyTeam.setDisplayName(net.minecraft.network.chat.Component.literal("Lobby"));
            lobbyTeam.setColor(net.minecraft.ChatFormatting.GRAY);
        }

        PlayerTeam adminTeam = scoreboard.getPlayerTeam("admin");
        if (adminTeam == null) {
            adminTeam = scoreboard.addPlayerTeam("admin");
            adminTeam.setDisplayName(net.minecraft.network.chat.Component.literal("Admin"));
            adminTeam.setColor(net.minecraft.ChatFormatting.RED);
        }

        List<String> adminNames = (List<String>) Config.SERVER.admins.get();

        // Сначала всех в лобби
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team join lobby @a");

        // Затем админов в админ команду
        for (String adminName : adminNames) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "team join admin " + adminName);
        }
    }

    private static void teleportToLobbies(MinecraftServer server) {
        double lobbyX = Config.SERVER.lobbyX.get();
        double lobbyY = Config.SERVER.lobbyY.get();
        double lobbyZ = Config.SERVER.lobbyZ.get();

        double adminLobbyX = Config.SERVER.adminLobbyX.get();
        double adminLobbyY = Config.SERVER.adminLobbyY.get();
        double adminLobbyZ = Config.SERVER.adminLobbyZ.get();

        List<String> adminNames = (List<String>) Config.SERVER.admins.get();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String playerName = player.getGameProfile().getName();

            if (adminNames.contains(playerName)) {
                // Телепортация в админ-лобби
                player.teleportTo(server.getLevel(player.level().dimension()),
                        adminLobbyX, adminLobbyY, adminLobbyZ,
                        player.getYRot(), player.getXRot());
            } else {
                // Телепортация в обычное лобби
                player.teleportTo(server.getLevel(player.level().dimension()),
                        lobbyX, lobbyY, lobbyZ,
                        player.getYRot(), player.getXRot());
            }
        }
    }
}