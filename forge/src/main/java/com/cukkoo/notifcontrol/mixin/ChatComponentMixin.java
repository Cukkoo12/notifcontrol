package com.cukkoo.notifcontrol.mixin;

import com.cukkoo.notifcontrol.NotifControlConfig;
import com.cukkoo.notifcontrol.ChatToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddMessage1(Component message, CallbackInfo ci) {
        handleChat(message, ci);
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/gui/components/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddMessage2(Component message, Object signature, Object tag, CallbackInfo ci) {
        handleChat(message, ci);
    }

    private void handleChat(Component message, CallbackInfo ci) {
        NotifControlConfig cfg = NotifControlConfig.get();
        if (!cfg.chatToToastEnabled) return;

        String text = message.getString();
        boolean triggered = false;

        for (String trigger : cfg.chatTriggerWords) {
            if (!trigger.isEmpty() && text.toLowerCase().contains(trigger.toLowerCase())) {
                triggered = true;
                break;
            }
        }

        if (triggered) {
            Minecraft.getInstance().getToastManager().addToast(new ChatToast(message));
            if (cfg.blockOriginalChat) {
                ci.cancel();
            }
        }
    }
}
