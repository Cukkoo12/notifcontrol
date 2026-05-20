package com.cukkoo.notifcontrol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifControlMod implements ClientModInitializer {

    public static final String MOD_ID = "notifcontrol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping toggleKey;
    public static KeyMapping historyKey;

    @Override
    public void onInitializeClient() {
        NotifControlConfig.load();

        // Register keybindings
        toggleKey = new KeyMapping(
                "key.notifcontrol.toggle",
                GLFW.GLFW_KEY_UNKNOWN,
                KeyMapping.Category.MISC
        );
        historyKey = new KeyMapping(
                "key.notifcontrol.history",
                GLFW.GLFW_KEY_H,
                KeyMapping.Category.MISC
        );

        // Tick handler for keybind — polls the key state
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            NotifControlConfig cfg = NotifControlConfig.get();
            if (cfg.globalToggleKey >= 0 && toggleKey.consumeClick()) {
                boolean allOff = !cfg.recipe.enabled
                        && !cfg.advancement.enabled
                        && !cfg.tutorial.enabled
                        && !cfg.system.enabled;
                boolean newState = allOff;
                cfg.recipe.enabled = newState;
                cfg.advancement.enabled = newState;
                cfg.tutorial.enabled = newState;
                cfg.system.enabled = newState;
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("§9[NotifControl] Toasts: " + (newState ? "§aON" : "§cOFF") + "§r"));
                }
                NotifControlConfig.save();
            }

            if (historyKey.consumeClick()) {
                client.setScreen(new com.cukkoo.notifcontrol.modmenu.NotificationCenterScreen(client.screen));
            }
        });

        NotifControlCommand.register();
        LOGGER.info("[NotifControl] Initialized. Config loaded from config/notifcontrol.json");
    }
}
