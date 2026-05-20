package com.cukkoo.notifcontrol;

import com.cukkoo.notifcontrol.modmenu.NotifControlScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.List;

public class NotifControlCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            var mainCmd = ClientCommands.literal("toast")
                    .executes(ctx -> {
                        ctx.getSource().getClient().execute(() -> {
                            ctx.getSource().getClient().setScreen(new NotifControlScreen(null));
                        });
                        return Command.SINGLE_SUCCESS;
                    })

                    // ── toggle ────────────────────────────────────────────────────
                    .then(ClientCommands.literal("toggle")
                        .executes(ctx -> toggle("all", ctx.getSource()))
                        .then(ClientCommands.literal("recipes")
                            .executes(ctx -> toggle("recipes", ctx.getSource())))
                        .then(ClientCommands.literal("advancements")
                            .executes(ctx -> toggle("advancements", ctx.getSource())))
                        .then(ClientCommands.literal("tutorials")
                            .executes(ctx -> toggle("tutorials", ctx.getSource())))
                        .then(ClientCommands.literal("system")
                            .executes(ctx -> toggle("system", ctx.getSource())))
                        .then(ClientCommands.literal("all")
                            .executes(ctx -> toggle("all", ctx.getSource())))
                    )

                    // ── reload ────────────────────────────────────────────────────
                    .then(ClientCommands.literal("reload")
                        .executes(ctx -> {
                            NotifControlConfig.load();
                            ctx.getSource().sendFeedback(
                                Component.literal("§a[NotifControl] Config reloaded.§r"));
                            return Command.SINGLE_SUCCESS;
                        })
                    )

                    // ── status ────────────────────────────────────────────────────
                    .then(ClientCommands.literal("status")
                        .executes(ctx -> {
                            NotifControlConfig cfg = NotifControlConfig.get();
                            ctx.getSource().sendFeedback(Component.literal(
                                "§6=== NotifControl Status ===§r"));
                            ctx.getSource().sendFeedback(Component.literal(
                                "  Recipes: "      + stateText(cfg.recipe.enabled) +
                                " Scale: " + cfg.recipe.scale));
                            ctx.getSource().sendFeedback(Component.literal(
                                "  Advancements: " + stateText(cfg.advancement.enabled) +
                                " Scale: " + cfg.advancement.scale));
                            ctx.getSource().sendFeedback(Component.literal(
                                "  Tutorials: "    + stateText(cfg.tutorial.enabled) +
                                " Scale: " + cfg.tutorial.scale));
                            ctx.getSource().sendFeedback(Component.literal(
                                "  System: "       + stateText(cfg.system.enabled) +
                                " Scale: " + cfg.system.scale));
                            ctx.getSource().sendFeedback(Component.literal(
                                "  Duration: §e" + cfg.toastDurationMultiplier + "§r" +
                                "  Scale: §e" + cfg.toastScale + "§r" +
                                "  Opacity: §e" + cfg.toastOpacity + "§r" +
                                "  Stacking: " + stateText(cfg.stackingEnabled)));
                            ctx.getSource().sendFeedback(Component.literal(
                                "  Position: §e" + cfg.toastPosition + "§r" +
                                "  Animation: §e" + cfg.animationStyle + "§r"));
                            return Command.SINGLE_SUCCESS;
                        })
                    )

                    // ── send (custom toast) ───────────────────────────────────────
                    .then(ClientCommands.literal("send")
                        .then(ClientCommands.argument("title", StringArgumentType.string())
                            .then(ClientCommands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String title = StringArgumentType.getString(ctx, "title");
                                    String message = StringArgumentType.getString(ctx, "message");
                                    ctx.getSource().getClient().execute(() -> {
                                        net.minecraft.client.gui.components.toasts.SystemToast.add(
                                            ctx.getSource().getClient().getToastManager(),
                                            net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                            Component.literal(title.replace("&", "§")),
                                            Component.literal(message.replace("&", "§"))
                                        );
                                    });
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )

                    // ── test ──────────────────────────────────────────────────────
                    .then(ClientCommands.literal("test")
                        .executes(ctx -> {
                            ctx.getSource().getClient().execute(() -> {
                                net.minecraft.client.gui.components.toasts.SystemToast.add(
                                    ctx.getSource().getClient().getToastManager(),
                                    net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                    Component.translatable("text.notifcontrol.test_title"),
                                    Component.translatable("text.notifcontrol.test_msg")
                                );
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                    )

                    // ── profile ───────────────────────────────────────────────────
                    .then(ClientCommands.literal("profile")
                        .then(ClientCommands.literal("save")
                            .then(ClientCommands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    NotifControlConfig.saveProfile(name);
                                    ctx.getSource().sendFeedback(Component.literal(
                                        "§a[NotifControl] Profile saved: " + name + "§r"));
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                        .then(ClientCommands.literal("load")
                            .then(ClientCommands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    boolean ok = NotifControlConfig.loadProfile(name);
                                    if (ok) {
                                        ctx.getSource().sendFeedback(Component.literal(
                                            "§a[NotifControl] Profile loaded: " + name + "§r"));
                                    } else {
                                        ctx.getSource().sendFeedback(Component.literal(
                                            "§c[NotifControl] Profile not found: " + name + "§r"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                        .then(ClientCommands.literal("delete")
                            .then(ClientCommands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    boolean ok = NotifControlConfig.deleteProfile(name);
                                    if (ok) {
                                        ctx.getSource().sendFeedback(Component.literal(
                                            "§a[NotifControl] Profile deleted: " + name + "§r"));
                                    } else {
                                        ctx.getSource().sendFeedback(Component.literal(
                                            "§c[NotifControl] Profile not found: " + name + "§r"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                        .then(ClientCommands.literal("list")
                            .executes(ctx -> {
                                List<String> profiles = NotifControlConfig.listProfiles();
                                if (profiles.isEmpty()) {
                                    ctx.getSource().sendFeedback(Component.literal(
                                        "§e[NotifControl] No saved profiles.§r"));
                                } else {
                                    ctx.getSource().sendFeedback(Component.literal(
                                        "§6=== Profiles (" + profiles.size() + ") ===§r"));
                                    for (String p : profiles) {
                                        ctx.getSource().sendFeedback(Component.literal(
                                            "  §e" + p + "§r"));
                                    }
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )

                    // ── history ───────────────────────────────────────────────────
                    .then(ClientCommands.literal("history")
                        .executes(ctx -> {
                            List<NotifControlHistory.Entry> entries = NotifControlHistory.getRecent();
                            if (entries.isEmpty()) {
                                ctx.getSource().sendFeedback(Component.literal(
                                    "§e[NotifControl] No toast history yet.§r"));
                            } else {
                                ctx.getSource().sendFeedback(Component.literal(
                                    "§6=== NotifControl History (last " + entries.size() + ") ===§r"));
                                for (NotifControlHistory.Entry entry : entries) {
                                    ctx.getSource().sendFeedback(entry.toText());
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    );

            dispatcher.register(mainCmd);
        });
    }

    // ── Toggle handler ──────────────────────────────────────────────────────────

    private static int toggle(String type, FabricClientCommandSource source) {
        NotifControlConfig cfg = NotifControlConfig.get();

        switch (type) {
            case "recipes" -> {
                cfg.recipe.enabled = !cfg.recipe.enabled;
                source.sendFeedback(Component.literal(
                    "[NotifControl] Recipe toasts: " + stateText(cfg.recipe.enabled)));
            }
            case "advancements" -> {
                cfg.advancement.enabled = !cfg.advancement.enabled;
                source.sendFeedback(Component.literal(
                    "[NotifControl] Advancement toasts: " + stateText(cfg.advancement.enabled)));
            }
            case "tutorials" -> {
                cfg.tutorial.enabled = !cfg.tutorial.enabled;
                source.sendFeedback(Component.literal(
                    "[NotifControl] Tutorial toasts: " + stateText(cfg.tutorial.enabled)));
            }
            case "system" -> {
                cfg.system.enabled = !cfg.system.enabled;
                source.sendFeedback(Component.literal(
                    "[NotifControl] System toasts: " + stateText(cfg.system.enabled)));
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
                source.sendFeedback(Component.literal(
                    "[NotifControl] All toasts: " + stateText(newState)));
            }
            default -> {
                source.sendFeedback(Component.literal(
                    "§c[NotifControl] Unknown toggle type.§r"));
                return 0;
            }
        }

        NotifControlConfig.save();
        return Command.SINGLE_SUCCESS;
    }

    // ── Util ─────────────────────────────────────────────────────────────────────

    private static String stateText(boolean value) {
        return value ? "§aON§r" : "§cOFF§r";
    }
}
