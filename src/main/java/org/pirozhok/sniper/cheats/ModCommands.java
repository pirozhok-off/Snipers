package org.pirozhok.sniper.cheats;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.pirozhok.sniper.Config;
import org.pirozhok.sniper.events.Start;
import org.pirozhok.sniper.events.End;
import org.pirozhok.sniper.system.SecuritySystem;
import org.pirozhok.sniper.system.BorderShrinkingSystem;

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
                .then(Commands.literal("game")
                        .then(Commands.literal("area")
                                .then(Commands.literal("on")
                                        .executes(context -> {
                                            Config.SERVER.borderShrinkEnabled.set(true);
                                            Config.SERVER.borderShrinkEnabled.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Сужение области включено"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("off")
                                        .executes(context -> {
                                            Config.SERVER.borderShrinkEnabled.set(false);
                                            Config.SERVER.borderShrinkEnabled.save();
                                            BorderShrinkingSystem.stopShrinking();
                                            context.getSource().sendSuccess(() -> Component.literal("Сужение области выключено"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("slow")
                                        .executes(context -> {
                                            Config.SERVER.borderShrinkMode.set("slow");
                                            Config.SERVER.borderShrinkMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Режим сужения: медленный"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("standard")
                                        .executes(context -> {
                                            Config.SERVER.borderShrinkMode.set("standard");
                                            Config.SERVER.borderShrinkMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Режим сужения: стандартный"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("fast")
                                        .executes(context -> {
                                            Config.SERVER.borderShrinkMode.set("fast");
                                            Config.SERVER.borderShrinkMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Режим сужения: быстрый"), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("teams")
                                .then(Commands.literal("solo")
                                        .executes(context -> {
                                            Config.SERVER.teamsMode.set("solo");
                                            Config.SERVER.teamsMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Режим команд: каждый за себя"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("team")
                                        .executes(context -> {
                                            Config.SERVER.teamsMode.set("team");
                                            Config.SERVER.teamsMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Режим команд: две команды"), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("guns")
                                .then(Commands.literal("players")
                                        .executes(context -> {
                                            Config.SERVER.gunsMode.set("players");
                                            Config.SERVER.gunsMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Оружие выдается игрокам"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("random")
                                        .executes(context -> {
                                            Config.SERVER.gunsMode.set("random");
                                            Config.SERVER.gunsMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Оружие спавнится в сундуках"), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("playerSpawn")
                                .then(Commands.literal("sky")
                                        .executes(context -> {
                                            Config.SERVER.spawnMode.set("sky");
                                            Config.SERVER.spawnMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Спавн игроков: в воздухе"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("random")
                                        .executes(context -> {
                                            Config.SERVER.spawnMode.set("random");
                                            Config.SERVER.spawnMode.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Спавн игроков: случайный"), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}