package com.cukkoo.notifcontrol;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public class ChatToast implements Toast {

    private final Component message;

    public ChatToast(Component message) {
        this.message = message;
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor extractor, net.minecraft.client.gui.Font font, long timeSinceLastVisible) {
        int tx = 0;
        int ty = 0;
        int toastW = 160;
        int toastH = 32;

        extractor.fill(tx, ty, tx + toastW, ty + toastH, 0xCC222222);
        extractor.fill(tx, ty, tx + toastW, ty + 1, 0xFFFFFFFF);
        extractor.fill(tx, ty + toastH - 1, tx + toastW, ty + toastH, 0xFFFFFFFF);
        extractor.fill(tx, ty + 1, tx + 1, ty + toastH - 1, 0xFFFFFFFF);
        extractor.fill(tx + toastW - 1, ty + 1, tx + toastW, ty + toastH - 1, 0xFFFFFFFF);
        
        extractor.text(font, "§bChat Message", 30, 7, 0xFFFFFFFF);
        
        String msgStr = message.getString();
        if (font.width(msgStr) > 120) {
            msgStr = font.plainSubstrByWidth(msgStr, 115) + "...";
        }
        extractor.text(font, msgStr, 30, 18, 0xFFFFFFFF);
    }

    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    @Override
    public void update(ToastManager manager, long timeSinceLastVisible) {
        NotifControlConfig cfg = NotifControlConfig.get();
        double displayTime = 5000.0 * cfg.toastDurationMultiplier;
        if (timeSinceLastVisible >= displayTime) {
            this.visibility = Toast.Visibility.HIDE;
        }
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.visibility;
    }
}
