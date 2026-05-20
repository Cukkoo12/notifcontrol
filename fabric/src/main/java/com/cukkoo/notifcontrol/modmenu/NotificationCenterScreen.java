package com.cukkoo.notifcontrol.modmenu;

import com.cukkoo.notifcontrol.NotifControlHistory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class NotificationCenterScreen extends Screen {

    private final Screen parent;
    private int scrollY = 0;
    private long openTime;
    
    public NotificationCenterScreen(Screen parent) {
        super(Component.translatable("text.notifcontrol.center"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.openTime = System.currentTimeMillis();
        int centerX = this.width / 2;
        int bottomY = this.height - 30;
        
        Button closeBtn = Button.builder(Component.translatable("text.notifcontrol.close"), btn -> {
            this.minecraft.setScreen(parent);
        }).bounds(centerX - 60, bottomY, 120, 20).build();
        
        Button clearBtn = Button.builder(Component.translatable("text.notifcontrol.clear"), btn -> {
            NotifControlHistory.clear();
        }).bounds(centerX + 70, bottomY, 80, 20).build();

        addRenderableWidget(closeBtn);
        addRenderableWidget(clearBtn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        float progress = Math.min(1.0f, (System.currentTimeMillis() - this.openTime) / 300.0f);
        progress = (float)(1.0 - Math.pow(1.0 - progress, 3)); // ease-out cubic
        
        int bgAlpha = (int)(0xDD * progress);
        int bgCol = (bgAlpha << 24) | 0x000000;
        extractor.fill(0, 0, this.width, this.height, bgCol);
        
        int panelW = Math.min(400, this.width - 40);
        int panelX = this.width / 2 - panelW / 2;
        int panelYOffset = (int)((1.0f - progress) * 50); // slide up by 50px
        int panelTop = 20 + panelYOffset;
        int panelBottom = this.height - 40 + panelYOffset;
        
        int panelAlpha = (int)(0x22 * progress);
        int panelCol = (panelAlpha << 24) | 0xFFFFFF;
        extractor.fill(panelX, panelTop, panelX + panelW, panelBottom, panelCol);
        
        int textAlpha = (int)(0xFF * progress);
        int textCol = (textAlpha << 24) | 0xFFFFFF;
        extractor.centeredText(this.font, "§b§l" + Component.translatable("text.notifcontrol.center").getString(), this.width / 2, 28 + panelYOffset, textCol);
        
        int lineAlpha = (int)(0x44 * progress);
        int lineCol = (lineAlpha << 24) | 0xFFFFFF;
        extractor.fill(panelX + 10, 42 + panelYOffset, panelX + panelW - 10, 43 + panelYOffset, lineCol);
        
        List<NotifControlHistory.Entry> entries = NotifControlHistory.getRecent();
        int y = 50 - scrollY + panelYOffset;
        
        if (entries.isEmpty()) {
            extractor.centeredText(this.font, "§7" + Component.translatable("text.notifcontrol.empty").getString(), this.width / 2, 80 + panelYOffset, textCol);
        } else {
            for (NotifControlHistory.Entry entry : entries) {
                if (y > 45 + panelYOffset && y < panelBottom - 5) {
                    // Draw a mini card for each entry
                    int cardAlpha = (int)(0x44 * progress);
                    int cardCol = (cardAlpha << 24) | 0x000000;
                    extractor.fill(panelX + 10, y, panelX + panelW - 10, y + 24, cardCol);
                    
                    int seconds = net.minecraft.util.Mth.floor((System.currentTimeMillis() - entry.timestamp()) / 1000f);
                    String time = Component.translatable("text.notifcontrol.ago", seconds).getString();
                    String title = entry.title();
                    if (title.length() > 35) title = title.substring(0, 32) + "...";
                    
                    extractor.text(this.font, "§e" + entry.type(), panelX + 15, y + 4, textCol);
                    extractor.text(this.font, "§f" + title, panelX + 15, y + 14, textCol);
                    extractor.text(this.font, "§8" + time, panelX + panelW - 45, y + 8, textCol);
                    
                    if (entry.blocked()) {
                        extractor.text(this.font, "§c" + Component.translatable("text.notifcontrol.blocked").getString(), panelX + panelW - 110, y + 8, textCol);
                    }
                }
                y += 28;
            }
        }
        
        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollY -= (int) (scrollY * 20);
        this.scrollY = Math.max(0, this.scrollY);
        return true;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(event);
    }
}
