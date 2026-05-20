package com.cukkoo.notifcontrol.modmenu;

import com.cukkoo.notifcontrol.NotifControlConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class NotifControlPositionScreen extends Screen {

    private final Screen parent;
    private final NotifControlConfig cfg;

    private boolean isDragging = false;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private int startOffsetX = 0;
    private int startOffsetY = 0;

    private int toastW = 160;
    private int toastH = 32;

    public NotifControlPositionScreen(Screen parent) {
        super(Component.translatable("text.notifcontrol.edit_pos_title"));
        this.parent = parent;
        this.cfg = NotifControlConfig.get();
    }

    @Override
    protected void init() {
        this.toastW = (int)(160 * cfg.toastScale);
        this.toastH = (int)(32 * cfg.toastScale);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        extractor.fill(0, 0, this.width, this.height, 0x88000000);

        // Instructions
        extractor.centeredText(this.font, "§e" + Component.translatable("text.notifcontrol.drag_instructions").getString(), this.width / 2, 20, 0xFFFFFFFF);
        extractor.centeredText(this.font, "§7" + Component.translatable("text.notifcontrol.save_instructions").getString(), this.width / 2, 35, 0xFFFFFFFF);

        // Draw the dummy toast based on current position and offsets
        int tx = getToastX();
        int ty = getToastY();

        // Draw toast background
        extractor.fill(tx, ty, tx + toastW, ty + toastH, 0xCC222222);
        
        // Draw toast border
        extractor.fill(tx, ty, tx + toastW, ty + 1, 0xFFFFFFFF);
        extractor.fill(tx, ty + toastH - 1, tx + toastW, ty + toastH, 0xFFFFFFFF);
        extractor.fill(tx, ty + 1, tx + 1, ty + toastH - 1, 0xFFFFFFFF);
        extractor.fill(tx + toastW - 1, ty + 1, tx + toastW, ty + toastH - 1, 0xFFFFFFFF);

        // Draw text
        extractor.text(this.font, "§e" + Component.translatable("text.notifcontrol.sample_title").getString(), tx + 8, ty + 6, 0xFFFFFFFF);
        extractor.text(this.font, "§7" + Component.translatable("text.notifcontrol.drag_me").getString(), tx + 8, ty + 18, 0xFFFFFFFF);

        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }

    private int getToastX() {
        int x = 0;
        if (cfg.toastPosition.equals("TOP_RIGHT") || cfg.toastPosition.equals("BOTTOM_RIGHT")) {
            x = this.width - toastW;
        }
        return x + cfg.toastOffsetX;
    }

    private int getToastY() {
        int y = 0;
        if (cfg.toastPosition.equals("BOTTOM_RIGHT") || cfg.toastPosition.equals("BOTTOM_LEFT")) {
            y = this.height - toastH;
        }
        return y + cfg.toastOffsetY;
    }

    // 1.21.4 MouseButtonEvent signatures
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isAction) {
        int tx = getToastX();
        int ty = getToastY();
        // MouseButtonEvent doesn't have double mouseX, mouseY. It might be passed via global state, but let's see.
        // Wait, if MouseButtonEvent doesn't have coordinates, we can get them from minecraft instance.
        double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

        if (mouseX >= tx && mouseX <= tx + toastW && mouseY >= ty && mouseY <= ty + toastH) {
            isDragging = true;
            dragStartX = (int) mouseX;
            dragStartY = (int) mouseY;
            startOffsetX = cfg.toastOffsetX;
            startOffsetY = cfg.toastOffsetY;
            return true;
        }

        return super.mouseClicked(event, isAction);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isDragging) {
            double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
            double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
            
            int dx = (int) mouseX - dragStartX;
            int dy = (int) mouseY - dragStartY;

            cfg.toastOffsetX = startOffsetX + dx;
            cfg.toastOffsetY = startOffsetY + dy;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDragging) {
            isDragging = false;
            NotifControlConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        NotifControlConfig.save();
        this.minecraft.setScreen(parent);
    }
}
