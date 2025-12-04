package org.pirozhok.sniper.cheats;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.pirozhok.sniper.events.Start;
import org.pirozhok.sniper.events.End;
import org.pirozhok.sniper.system.SecuritySystem;
import org.pirozhok.sniper.system.BorderShrinkingSystem;
import org.pirozhok.sniper.system.States;

import java.util.Map;

public class ModCommands
{

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("game")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start")
                        .executes(context -> {
                            // Проверяем доступ
                            if (context.getSource().getPlayer() != null &&
                                    !SecuritySystem.hasAccess(context.getSource().getPlayer())) {
                                context.getSource().sendFailure(Component.literal("Недостаточно прав для запуска игры!"));
                                return 0;
                            }
                            Start.startGame(context.getSource().getServer());
                            context.getSource().sendSuccess(() -> Component.literal("Игра начата!"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("end")
                        .executes(context -> {
                            // Проверяем доступ
                            if (context.getSource().getPlayer() != null &&
                                    !SecuritySystem.hasAccess(context.getSource().getPlayer())) {
                                context.getSource().sendFailure(Component.literal("Недостаточно прав для завершения игры!"));
                                return 0;
                            }
                            End.endGame(context.getSource().getServer());
                            context.getSource().sendSuccess(() -> Component.literal("Игра завершена!"), true);
                            return 1;
                        })
                )
        );

        dispatcher.register(Commands.literal("sniper")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .executes(context -> {
                            Map<String, Object> states = States.getAllStates();
                            StringBuilder message = new StringBuilder("Текущие настройки:\n");
                            for (Map.Entry<String, Object> entry : states.entrySet()) {
                                message.append(entry.getKey())
                                        .append(": ")
                                        .append(entry.getValue())
                                        .append("\n");
                            }
                            context.getSource().sendSuccess(() ->
                                    Component.literal(message.toString()), false);
                            return 1;
                        })
                )
                .then(Commands.literal("reset")
                        .executes(context -> {
                            States.resetToDefaults();
                            context.getSource().sendSuccess(() ->
                                    Component.literal("Настройки сброшены к значениям по умолчанию"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("game")
                        .then(Commands.literal("area")
                                .then(Commands.literal("on")
                                        .executes(context -> {
                                            States.setBorderShrinkEnabled(true);
                                            context.getSource().sendSuccess(() -> Component.literal("Сужение области включено"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("off")
                                        .executes(context -> {
                                            States.setBorderShrinkEnabled(false);
                                            BorderShrinkingSystem.stopShrinking();
                                            context.getSource().sendSuccess(() -> Component.literal("Сужение области выключено"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("slow")
                                        .executes(context -> {
                                            States.setBorderShrinkMode("slow");
                                            context.getSource().sendSuccess(() -> Component.literal("Режим сужения: медленный"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("standard")
                                        .executes(context -> {
                                            States.setBorderShrinkMode("standard");
                                            context.getSource().sendSuccess(() -> Component.literal("Режим сужения: стандартный"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("fast")
                                        .executes(context -> {
                                            States.setBorderShrinkMode("fast");
                                            context.getSource().sendSuccess(() -> Component.literal("Режим сужения: быстрый"), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("teams")
                                .then(Commands.literal("solo")
                                        .executes(context -> {
                                            States.setTeamsMode("solo");
                                            context.getSource().sendSuccess(() -> Component.literal("Режим команд: каждый за себя"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("team")
                                        .executes(context -> {
                                            States.setTeamsMode("team");
                                            context.getSource().sendSuccess(() -> Component.literal("Режим команд: две команды"), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("guns")
                                .then(Commands.literal("players")
                                        .executes(context -> {
                                            States.setGunsMode("players");
                                            context.getSource().sendSuccess(() -> Component.literal("Оружие выдается игрокам"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("random")
                                        .executes(context -> {
                                            States.setGunsMode("random");
                                            context.getSource().sendSuccess(() -> Component.literal("Оружие спавнится в сундуках"), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("playerSpawn")
                                .then(Commands.literal("sky")
                                        .executes(context -> {
                                            States.setSpawnMode("sky");
                                            context.getSource().sendSuccess(() -> Component.literal("Спавн игроков: в воздухе"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("random")
                                        .executes(context -> {
                                            States.setSpawnMode("random");
                                            context.getSource().sendSuccess(() -> Component.literal("Спавн игроков: случайный"), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}