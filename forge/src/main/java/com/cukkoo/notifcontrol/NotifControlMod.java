package com.cukkoo.notifcontrol;

import com.cukkoo.notifcontrol.modmenu.NotificationCenterScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NotifControlMod.MOD_ID)
public class NotifControlMod {

    public static final String MOD_ID = "notifcontrol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping toggleKey;
    public static KeyMapping historyKey;

    public NotifControlMod() {
        NotifControlConfig.load();

        toggleKey = new KeyMapping(
                "key.notifcontrol.toggle",
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.misc"
        );
        historyKey = new KeyMapping(
                "key.notifcontrol.history",
                GLFW.GLFW_KEY_H,
                "key.categories.misc"
        );

        LOGGER.info("[NotifControl] Initialized (Forge 26.1.x). Config loaded from config/notifcontrol.json");
    }

    // Note: Forge 26.1 @Mod.EventBusSubscriber: bus attribute removed
    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientEventHandler {

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(toggleKey);
            event.register(historyKey);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
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
