package com.cukkoo.notifcontrol;

import com.cukkoo.notifcontrol.modmenu.NotificationCenterScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NotifControlMod.MOD_ID)
public class NotifControlMod {

    public static final String MOD_ID = "notifcontrol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping toggleKey;
    public static KeyMapping historyKey;

    public NotifControlMod(IEventBus modEventBus, ModContainer container) {
        NotifControlConfig.load();

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

        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.register(ClientEventHandler.class);

        LOGGER.info("[NotifControl] Initialized (NeoForge 26.1.x). Config loaded from config/notifcontrol.json");
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(toggleKey);
        event.register(historyKey);
    }

    // Note: @EventBusSubscriber on NeoForge 26.1.x: bus attribute removed, only modid needed
    @EventBusSubscriber(modid = MOD_ID)
    public static class ClientEventHandler {

        @SubscribeEvent
        public static void onClientTickPost(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null) return;
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
                            Component.literal("\u00a79[NotifControl] Toasts: " + (newState ? "\u00a7aON" : "\u00a7cOFF") + "\u00a7r"));
                }
                NotifControlConfig.save();
            }

            if (historyKey.consumeClick()) {
                client.setScreen(new NotificationCenterScreen(client.screen));
            }
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            NotifControlCommand.register(event.getDispatcher());
        }
    }
}
