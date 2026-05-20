package com.cukkoo.notifcontrol;

import com.cukkoo.notifcontrol.modmenu.NotifControlScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class NotifControlCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var mainCmd = Commands.literal("toast")
                .executes(ctx -> {
                    net.minecraft.client.Minecraft.getInstance().execute(() ->
                        net.minecraft.client.Minecraft.getInstance().setScreen(new NotifControlScreen(null))
                    );
                    return Command.SINGLE_SUCCESS;
                })

                // ── toggle ────────────────────────────────────────────────────
                .then(Commands.literal("toggle")
                    .executes(ctx -> toggle("all", ctx))
                    .then(Commands.literal("recipes")
                        .executes(ctx -> toggle("recipes", ctx)))
                    .then(Commands.literal("advancements")
                        .executes(ctx -> toggle("advancements", ctx)))
                    .then(Commands.literal("tutorials")
                        .executes(ctx -> toggle("tutorials", ctx)))
                    .then(Commands.literal("system")
                        .executes(ctx -> toggle("system", ctx)))
                    .then(Commands.literal("all")
                        .executes(ctx -> toggle("all", ctx)))
                )

                // ── reload ────────────────────────────────────────────────────
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        NotifControlConfig.load();
                        sendFeedback(ctx, Component.literal("§a[NotifControl] Config reloaded.§r"));
                        return Command.SINGLE_SUCCESS;
                    })
                )

                // ── status ────────────────────────────────────────────────────
                .then(Commands.literal("status")
                    .executes(ctx -> {
                        NotifControlConfig cfg = NotifControlConfig.get();
                        sendFeedback(ctx, Component.literal("§6=== NotifControl Status ===§r"));
                        sendFeedback(ctx, Component.literal("  Recipes: " + stateText(cfg.recipe.enabled) + " Scale: " + cfg.recipe.scale));
                        sendFeedback(ctx, Component.literal("  Advancements: " + stateText(cfg.advancement.enabled) + " Scale: " + cfg.advancement.scale));
                        sendFeedback(ctx, Component.literal("  Tutorials: " + stateText(cfg.tutorial.enabled) + " Scale: " + cfg.tutorial.scale));
                        sendFeedback(ctx, Component.literal("  System: " + stateText(cfg.system.enabled) + " Scale: " + cfg.system.scale));
                        sendFeedback(ctx, Component.literal(
                            "  Duration: §e" + cfg.toastDurationMultiplier + "§r" +
                            "  Scale: §e" + cfg.toastScale + "§r" +
                            "  Opacity: §e" + cfg.toastOpacity + "§r" +
                            "  Stacking: " + stateText(cfg.stackingEnabled)));
                        sendFeedback(ctx, Component.literal(
                            "  Position: §e" + cfg.toastPosition + "§r" +
                            "  Animation: §e" + cfg.animationStyle + "§r"));
                        return Command.SINGLE_SUCCESS;
                    })
                )

                // ── send ──────────────────────────────────────────────────────
                .then(Commands.literal("send")
                    .then(Commands.argument("title", StringArgumentType.string())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String title = StringArgumentType.getString(ctx, "title");
                                String message = StringArgumentType.getString(ctx, "message");
                                net.minecraft.client.Minecraft.getInstance().execute(() ->
                                    net.minecraft.client.gui.components.toasts.SystemToast.add(
                                        net.minecraft.client.Minecraft.getInstance().getToastManager(),
                                        net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                        Component.literal(title.replace("&", "§")),
                                        Component.literal(message.replace("&", "§"))
                                    )
                                );
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )

                // ── test ──────────────────────────────────────────────────────
                .then(Commands.literal("test")
                    .executes(ctx -> {
                        net.minecraft.client.Minecraft.getInstance().execute(() ->
                            net.minecraft.client.gui.components.toasts.SystemToast.add(
                                net.minecraft.client.Minecraft.getInstance().getToastManager(),
                                net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                Component.translatable("text.notifcontrol.test_title"),
                                Component.translatable("text.notifcontrol.test_msg")
                            )
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )

                // ── profile ───────────────────────────────────────────────────
                .then(Commands.literal("profile")
                    .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                NotifControlConfig.saveProfile(name);
                                sendFeedback(ctx, Component.literal("§a[NotifControl] Profile saved: " + name + "§r"));
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                    .then(Commands.literal("load")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                boolean ok = NotifControlConfig.loadProfile(name);
                                sendFeedback(ctx, Component.literal(ok
                                    ? "§a[NotifControl] Profile loaded: " + name + "§r"
                                    : "§c[NotifControl] Profile not found: " + name + "§r"));
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                    .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                boolean ok = NotifControlConfig.deleteProfile(name);
                                sendFeedback(ctx, Component.literal(ok
                                    ? "§a[NotifControl] Profile deleted: " + name + "§r"
                                    : "§c[NotifControl] Profile not found: " + name + "§r"));
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(ctx -> {
                            List<String> profiles = NotifControlConfig.listProfiles();
                            if (profiles.isEmpty()) {
                                sendFeedback(ctx, Component.literal("§e[NotifControl] No saved profiles.§r"));
                            } else {
                                sendFeedback(ctx, Component.literal("§6=== Profiles (" + profiles.size() + ") ===§r"));
                                for (String p : profiles) {
                                    sendFeedback(ctx, Component.literal("  §e" + p + "§r"));
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )

                // ── history ───────────────────────────────────────────────────
                .then(Commands.literal("history")
                    .executes(ctx -> {
                        List<NotifControlHistory.Entry> entries = NotifControlHistory.getRecent();
                        if (entries.isEmpty()) {
                            sendFeedback(ctx, Component.literal("§e[NotifControl] No toast history yet.§r"));
                        } else {
                            sendFeedback(ctx, Component.literal("§6=== NotifControl History (last " + entries.size() + ") ===§r"));
                            for (NotifControlHistory.Entry entry : entries) {
                                sendFeedback(ctx, entry.toText());
                            }
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                );

        dispatcher.register(mainCmd);
    }

    // ── Toggle handler ───────────────────────────────────────────────────────

    private static int toggle(String type, com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        NotifControlConfig cfg = NotifControlConfig.get();

        switch (type) {
            case "recipes" -> {
                cfg.recipe.enabled = !cfg.recipe.enabled;
                sendFeedback(ctx, Component.literal("[NotifControl] Recipe toasts: " + stateText(cfg.recipe.enabled)));
            }
            case "advancements" -> {
                cfg.advancement.enabled = !cfg.advancement.enabled;
                sendFeedback(ctx, Component.literal("[NotifControl] Advancement toasts: " + stateText(cfg.advancement.enabled)));
            }
            case "tutorials" -> {
                cfg.tutorial.enabled = !cfg.tutorial.enabled;
                sendFeedback(ctx, Component.literal("[NotifControl] Tutorial toasts: " + stateText(cfg.tutorial.enabled)));
            }
            case "system" -> {
                cfg.system.enabled = !cfg.system.enabled;
                sendFeedback(ctx, Component.literal("[NotifControl] System toasts: " + stateText(cfg.system.enabled)));
            }
            case "all" -> {
                boolean allOff = !cfg.recipe.enabled
                        && !cfg.advancement.enabled
                        && !cfg.tutorial.enabled
                        && !cfg.system.enabled;
                boolean newState = allOff;
                cfg.recipe.enabled = newState;
                cfg.advancement.enabled = newState;
                cfg.tutorial.enabled = newState;
                cfg.system.enabled = newState;
                sendFeedback(ctx, Component.literal("[NotifControl] All toasts: " + stateText(newState)));
            }
            default -> {
                sendFeedback(ctx, Component.literal("§c[NotifControl] Unknown toggle type.§r"));
                return 0;
            }
        }

        NotifControlConfig.save();
        return Command.SINGLE_SUCCESS;
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    private static void sendFeedback(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, Component msg) {
        // Send as system message to player if available, else use server feedback
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player != null) {
            player.sendSystemMessage(msg);
        } else {
            // Fallback: send via command source
            src.sendSuccess(() -> msg, false);
        }
    }

    private static String stateText(boolean value) {
        return value ? "§aON§r" : "§cOFF§r";
    }
}
