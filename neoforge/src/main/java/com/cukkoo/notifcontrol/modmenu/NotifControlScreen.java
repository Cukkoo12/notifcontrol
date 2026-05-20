package com.cukkoo.notifcontrol.modmenu;

import com.cukkoo.notifcontrol.NotifControlConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NotifControlScreen extends Screen {

    private static final int ROW_H = 18;
    private static final int ROW_GAP = 22;

    // Sleek modern theme colors
    private static final int C_BG         = 0xDD000000; // deep dark bg
    private static final int C_BORDER     = 0x33FFFFFF; // visible glassmorphic border
    private static final int C_FILL       = 0x22000000; // dark glassmorphic card fill
    private static final int C_ACTIVE_TAB = 0xFF00E5FF; // bright neon cyan
    private static final int C_LABEL      = 0xFFEEEEEE; // clean near-white
    private static final int C_VALUE      = 0xFF00FFCC; // accent teal/cyan

    // Color presets
    private static final String[] COLOR_NAMES = {"Def", "Red", "Blue", "Green", "Purple", "Orange", "White", "Dark"};
    private static final int[] COLOR_VALUES = {0, 0xFFFF4444, 0xFF4488FF, 0xFF44CC44, 0xFFBB44FF, 0xFFFF8844, 0xFFFFFFFF, 0xFF222222};

    // ── Fields ──
    private final Screen parent;
    private int activeTab = 0;
    private int tabScrollOffset = 0;
    private int previewType = 1; // 0 = Recipe, 1 = Advancement, 2 = Tutorial, 3 = System

    private int middle;
    private int totalW;
    private int leftW;
    private int gap;
    private int rightW;
    private int leftX;
    private int rightX;
    private int controlLx;
    private int controlRx;
    private final int controlW = 90;

    private final List<TabWidget> tabWidgets = new ArrayList<>();
    private final List<LabelLine> labels = new ArrayList<>();

    private boolean capturingKey = false;
    private EditBox blacklistBox;
    private EditBox profileBox;

    private record TabWidget(AbstractWidget widget, int tabIndex, int baseY) {}
    private record LabelLine(int tabIndex, int baseY, String label, java.util.function.Supplier<String> valueSupplier) {}

    public NotifControlScreen(Screen parent) {
        super(Component.translatable("text.notifcontrol.settings"));
        this.parent = parent;
    }

    // ── Color helpers ────────────────────────────────────────────────────────────

    private int colorIndex(int argb) {
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i] == argb) return i;
        }
        return 0;
    }

    private String getKeyName(int key) {
        if (key == -1) return Component.translatable("text.notifcontrol.none").getString();
        if (key == GLFW.GLFW_KEY_ESCAPE) return Component.translatable("text.notifcontrol.key.escape").getString();
        String n = GLFW.glfwGetKeyName(key, 0);
        if (n == null || n.isEmpty()) {
            return Component.translatable("text.notifcontrol.key.format", key).getString();
        }
        return n.toUpperCase();
    }

    // ── Init ─────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        tabWidgets.clear();
        labels.clear();
        clearWidgets();

        String savedBlacklist = blacklistBox != null ? blacklistBox.getValue() : "";
        String savedProfile = profileBox != null ? profileBox.getValue() : "";
        blacklistBox = null;
        profileBox = null;

        middle = this.width / 2;

        // Responsive dashboard sizing
        totalW = Math.min(500, this.width - 30);
        leftW = 150;
        gap = 20;
        rightW = totalW - leftW - gap;

        leftX = middle - totalW / 2;
        rightX = leftX + leftW + gap;

        controlRx = rightX + rightW - 5;
        controlLx = controlRx - controlW;

        NotifControlConfig cfg = NotifControlConfig.get();

        // ─────────────────────────────────────────────────────────────────────────────
        // ── LEFT COLUMN (Global Widgets, tabIndex = -1) ──────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────

        // Preview Switcher Button
        String[] previewTypes = {
            Component.translatable("text.notifcontrol.type.recipe").getString(),
            Component.translatable("text.notifcontrol.type.advancement").getString(),
            Component.translatable("text.notifcontrol.type.tutorial").getString(),
            Component.translatable("text.notifcontrol.type.system").getString()
        };
        AbstractWidget prevTypeBtn = Button.builder(Component.translatable("text.notifcontrol.preview", previewTypes[previewType]), btn -> {
            previewType = (previewType + 1) % 4;
            btn.setMessage(Component.translatable("text.notifcontrol.preview", previewTypes[previewType]));
        }).bounds(leftX + 5, 115, leftW - 10, 20).build();
        tabWidgets.add(new TabWidget(prevTypeBtn, -1, 115));
        addRenderableWidget(prevTypeBtn);

        // Global keybind button
        AbstractWidget bindBtn = Button.builder(Component.translatable(capturingKey ? "text.notifcontrol.press_key" : "text.notifcontrol.keybind", getKeyName(cfg.globalToggleKey)), btn -> {
            if (!capturingKey) {
                capturingKey = true;
                btn.setMessage(Component.translatable("text.notifcontrol.press_key"));
            }
        }).bounds(leftX + 5, 175, leftW - 10, 20).build();
        tabWidgets.add(new TabWidget(bindBtn, -1, 175));
        addRenderableWidget(bindBtn);

        // Notification Center button
        AbstractWidget centerBtn = Button.builder(Component.translatable("text.notifcontrol.center"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new NotificationCenterScreen(this));
            }
        }).bounds(leftX + 5, 205, leftW - 10, 20).build();
        tabWidgets.add(new TabWidget(centerBtn, -1, 205));
        addRenderableWidget(centerBtn);

        // Close button at bottom left
        AbstractWidget closeBtn = Button.builder(Component.translatable("text.notifcontrol.close"), btn -> onClose())
                .bounds(leftX + 5, this.height - 30, leftW - 10, 20)
                .build();
        tabWidgets.add(new TabWidget(closeBtn, -1, this.height - 28));
        addRenderableWidget(closeBtn);

        // ─────────────────────────────────────────────────────────────────────────────
        // ── TAB BUTTONS (Global Widgets, tabIndex = -1) ──────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────
        String[] tabKeys = {
            "text.notifcontrol.tab.general",
            "text.notifcontrol.tab.visuals",
            "text.notifcontrol.tab.filters",
            "text.notifcontrol.tab.chat",
            "text.notifcontrol.tab.profiles"
        };
        int tabW = (rightW - 4 * 3) / 5;
        for (int i = 0; i < 5; i++) {
            final int tIdx = i;
            AbstractWidget tabBtn = Button.builder(Component.translatable(tabKeys[i]), btn -> {
                activeTab = tIdx;
                tabScrollOffset = 0;
                updateWidgetVisibility();
            }).bounds(rightX + i * (tabW + 3), 20, tabW, 18).build();
            tabWidgets.add(new TabWidget(tabBtn, -1, 20));
            addRenderableWidget(tabBtn);
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // ── TAB CONTENT: TAB 0 (General Settings) ────────────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────
        int ry = 46;

        ry = addStepper(ry, 0, "text.notifcontrol.option.duration", "%.1fx", 0.1f, 5.0f, 0.1f,
                () -> (float) cfg.toastDurationMultiplier, v -> cfg.toastDurationMultiplier = v);

        ry = addStepper(ry, 0, "text.notifcontrol.option.scale", "%.1f", 0.5f, 2.0f, 0.1f,
                () -> (float) cfg.toastScale, v -> cfg.toastScale = v);

        ry = addStepper(ry, 0, "text.notifcontrol.option.opacity", "%.1f", 0.1f, 1.0f, 0.1f,
                () -> (float) cfg.toastOpacity, v -> cfg.toastOpacity = v);

        ry = addToggle(ry, 0, "text.notifcontrol.option.stacking", cfg.stackingEnabled, v -> cfg.stackingEnabled = v);

        ry = addCycle(ry, 0, "text.notifcontrol.option.position",
                new String[]{"TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT", "FREEFORM"},
                cfg.toastPosition, v -> cfg.toastPosition = v);

        AbstractWidget posBtn = Button.builder(Component.translatable("text.notifcontrol.edit_pos"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new NotifControlPositionScreen(this));
            }
        }).bounds(controlLx, ry, controlW, 18).build();
        tabWidgets.add(new TabWidget(posBtn, 0, ry));
        addRenderableWidget(posBtn);
        labels.add(new LabelLine(0, ry, Component.translatable("text.notifcontrol.drag_drop").getString(), () -> ""));
        ry += ROW_GAP;
        
        ry = addToggle(ry, 0, "text.notifcontrol.option.dnd", cfg.dndMode, v -> cfg.dndMode = v);
        ry = addToggle(ry, 0, "text.notifcontrol.option.smart_merging", cfg.smartMerging, v -> cfg.smartMerging = v);

        ry = addCycle(ry, 0, "text.notifcontrol.option.animation",
                new String[]{"SLIDE", "FADE", "POP", "BOUNCE", "NONE"},
                cfg.animationStyle, v -> cfg.animationStyle = v);

        ry = addStepper(ry, 0, "text.notifcontrol.option.anim_speed", "%.1fx", 0.1f, 5.0f, 0.1f,
                () -> (float) cfg.animationSpeed, v -> cfg.animationSpeed = v);

        ry = addToggle(ry, 0, "text.notifcontrol.option.custom_bg", cfg.customBackgroundsEnabled, v -> cfg.customBackgroundsEnabled = v);

        ry = addStepperInt(ry, 0, "text.notifcontrol.option.spam_delay", 0, 10000, 100,
                () -> cfg.toastSpamDelayMs, v -> cfg.toastSpamDelayMs = v);

        // ─────────────────────────────────────────────────────────────────────────────
        // ── TAB CONTENT: TAB 1 (Visuals) ─────────────────────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────
        ry = 46;
        ry = addCycle(ry, 1, "text.notifcontrol.option.theme", new String[]{"VANILLA", "RGB", "HOLOGRAPHIC"}, cfg.themeMode, v -> cfg.themeMode = v);
        ry = addToggle(ry, 1, "text.notifcontrol.option.progress_bar", cfg.progressBar, v -> cfg.progressBar = v);
        ry = addToggle(ry, 1, "text.notifcontrol.option.compact_mode", cfg.compactMode, v -> cfg.compactMode = v);
        ry = addToggle(ry, 1, "text.notifcontrol.option.toast_physics", cfg.toastPhysics, v -> cfg.toastPhysics = v);
        ry = addToggle(ry, 1, "text.notifcontrol.option.custom_sound", cfg.customSoundEnabled, v -> cfg.customSoundEnabled = v);
        ry += 10;
        ry = addTypeRow(ry, "text.notifcontrol.type.recipe", cfg.recipe);
        ry = addTypeRow(ry, "text.notifcontrol.type.advancement", cfg.advancement);
        ry = addTypeRow(ry, "text.notifcontrol.type.tutorial", cfg.tutorial);
        ry = addTypeRow(ry, "text.notifcontrol.type.system", cfg.system);

        // ─────────────────────────────────────────────────────────────────────────────
        // ── TAB CONTENT: TAB 2 (Filters) ─────────────────────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────
        ry = 46;
        ry = addCycle(ry, 2, "text.notifcontrol.option.mode", new String[]{"BLACKLIST", "WHITELIST"}, cfg.filterMode, v -> cfg.filterMode = v);
        ry = addToggle(ry, 2, "text.notifcontrol.option.regex", cfg.regexEnabled, v -> cfg.regexEnabled = v);
        ry = addToggle(ry, 2, "text.notifcontrol.option.log_blocked", cfg.logBlockedToasts, v -> cfg.logBlockedToasts = v);

        int blInputW = rightW - 45;
        blacklistBox = new EditBox(this.font, rightX + 5, ry, blInputW, ROW_H, Component.translatable("text.notifcontrol.filter_placeholder"));
        blacklistBox.setMaxLength(128);
        blacklistBox.setValue(savedBlacklist);
        tabWidgets.add(new TabWidget(blacklistBox, 2, ry));
        addRenderableWidget(blacklistBox);

        AbstractWidget addBtn = Button.builder(Component.translatable("text.notifcontrol.add"), btn -> {
            String t = blacklistBox.getValue().trim();
            if (!t.isEmpty()) {
                cfg.advancementBlacklist.add(t);
                NotifControlConfig.save();
                blacklistBox.setValue("");
                rebuild();
            }
        }).bounds(rightX + 5 + blInputW + 3, ry, 36, ROW_H).build();
        tabWidgets.add(new TabWidget(addBtn, 2, ry));
        addRenderableWidget(addBtn);

        ry += ROW_GAP;

        if (cfg.advancementBlacklist.isEmpty()) {
            labels.add(new LabelLine(2, ry + 4, "§8" + Component.translatable("text.notifcontrol.filter_empty").getString(), () -> ""));
        } else {
            for (int i = 0; i < cfg.advancementBlacklist.size(); i++) {
                final int idx = i;
                int iy = ry + i * ROW_GAP;
                String item = cfg.advancementBlacklist.get(i);
                
                String displayItem = item;
                if (displayItem.length() > 24) {
                    displayItem = displayItem.substring(0, 22) + "...";
                }
                labels.add(new LabelLine(2, iy, " §7" + displayItem, () -> ""));

                Button rb = Button.builder(Component.literal("×"), btn -> {
                    cfg.advancementBlacklist.remove(item);
                    NotifControlConfig.save();
                    rebuild();
                }).bounds(controlRx - 20, iy, 18, ROW_H).build();
                tabWidgets.add(new TabWidget(rb, 2, iy));
                addRenderableWidget(rb);
            }
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // ── TAB CONTENT: TAB 3 (Chat) ────────────────────────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────
        ry = 46;
        ry = addToggle(ry, 3, "text.notifcontrol.option.enable_chat", cfg.chatToToastEnabled, v -> cfg.chatToToastEnabled = v);
        ry = addToggle(ry, 3, "text.notifcontrol.option.block_chat", cfg.blockOriginalChat, v -> cfg.blockOriginalChat = v);
        
        labels.add(new LabelLine(3, ry, Component.translatable("text.notifcontrol.triggers_edit").getString(), () -> ""));
        ry += ROW_GAP;
        for (int i = 0; i < cfg.chatTriggerWords.size(); i++) {
            labels.add(new LabelLine(3, ry + i * ROW_GAP, " §f• §b" + cfg.chatTriggerWords.get(i), () -> ""));
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // ── TAB CONTENT: TAB 4 (Profiles Manager) ────────────────────────────────────
        // ─────────────────────────────────────────────────────────────────────────────
        ry = 46;
        int prInputW = rightW - 105;
        profileBox = new EditBox(this.font, rightX + 5, ry, prInputW, ROW_H, Component.translatable("text.notifcontrol.profile_placeholder"));
        profileBox.setMaxLength(32);
        profileBox.setValue(savedProfile);
        tabWidgets.add(new TabWidget(profileBox, 4, ry));
        addRenderableWidget(profileBox);

        AbstractWidget saveBtn = Button.builder(Component.translatable("text.notifcontrol.save"), btn -> {
            String n = profileBox.getValue().trim();
            if (!n.isEmpty()) { NotifControlConfig.saveProfile(n); rebuild(); }
        }).bounds(rightX + 5 + prInputW + 3, ry, 32, ROW_H).build();
        tabWidgets.add(new TabWidget(saveBtn, 4, ry));
        addRenderableWidget(saveBtn);

        AbstractWidget loadBtn = Button.builder(Component.translatable("text.notifcontrol.load"), btn -> {
            String n = profileBox.getValue().trim();
            if (!n.isEmpty()) { NotifControlConfig.loadProfile(n); rebuild(); }
        }).bounds(rightX + 5 + prInputW + 3 + 34, ry, 32, ROW_H).build();
        tabWidgets.add(new TabWidget(loadBtn, 4, ry));
        addRenderableWidget(loadBtn);

        AbstractWidget delBtn = Button.builder(Component.translatable("text.notifcontrol.del"), btn -> {
            String n = profileBox.getValue().trim();
            if (!n.isEmpty()) { NotifControlConfig.deleteProfile(n); rebuild(); }
        }).bounds(rightX + 5 + prInputW + 3 + 68, ry, 32, ROW_H).build();
        tabWidgets.add(new TabWidget(delBtn, 4, ry));
        addRenderableWidget(delBtn);

        ry += ROW_GAP;

        List<String> profilesList = NotifControlConfig.listProfiles();
        if (profilesList.isEmpty()) {
            labels.add(new LabelLine(4, ry + 4, "§8" + Component.translatable("text.notifcontrol.profiles_empty").getString(), () -> ""));
            ry += ROW_GAP;
        } else {
            for (int i = 0; i < profilesList.size(); i++) {
                final String pName = profilesList.get(i);
                int iy = ry + i * ROW_GAP;

                String displayPName = pName;
                if (displayPName.length() > 18) {
                    displayPName = displayPName.substring(0, 16) + "...";
                }

                labels.add(new LabelLine(4, iy, " §f• §b" + displayPName, () -> ""));

                Button plb = Button.builder(Component.translatable("text.notifcontrol.load"), btn -> {
                    NotifControlConfig.loadProfile(pName);
                    rebuild();
                }).bounds(controlRx - 56, iy, 26, ROW_H).build();
                tabWidgets.add(new TabWidget(plb, 4, iy));
                addRenderableWidget(plb);

                Button pdb = Button.builder(Component.translatable("text.notifcontrol.del"), btn -> {
                    NotifControlConfig.deleteProfile(pName);
                    rebuild();
                }).bounds(controlRx - 28, iy, 26, ROW_H).build();
                tabWidgets.add(new TabWidget(pdb, 4, iy));
                addRenderableWidget(pdb);
            }
            ry += profilesList.size() * ROW_GAP;
        }

        ry += 4;
        int toolW = (rightW - 15) / 2;
        AbstractWidget expBtn = Button.builder(Component.translatable("text.notifcontrol.export"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.keyboardHandler.setClipboard(NotifControlConfig.exportToJson());
                if (this.minecraft.player != null)
                    this.minecraft.player.sendSystemMessage(Component.translatable("text.notifcontrol.copied"));
            }
        }).bounds(rightX + 5, ry, toolW, ROW_H).build();
        tabWidgets.add(new TabWidget(expBtn, 4, ry));
        addRenderableWidget(expBtn);

        AbstractWidget impBtn = Button.builder(Component.translatable("text.notifcontrol.import"), btn -> {
            if (this.minecraft != null) {
                String j = this.minecraft.keyboardHandler.getClipboard();
                if (j != null && NotifControlConfig.importFromJson(j)) {
                    rebuild();
                    if (this.minecraft.player != null)
                        this.minecraft.player.sendSystemMessage(Component.translatable("text.notifcontrol.imported"));
                }
            }
        }).bounds(rightX + 5 + toolW + 5, ry, toolW, ROW_H).build();
        tabWidgets.add(new TabWidget(impBtn, 4, ry));
        addRenderableWidget(impBtn);

        updateWidgetVisibility();
    }

    // ── Element builders ─────────────────────────────────────────────────────────

    private int addToggle(int y, int tab, String label, boolean current,
                          java.util.function.Consumer<Boolean> setter) {
        AbstractWidget w = CycleButton.onOffBuilder(current)
                .create(controlLx, y, controlW, ROW_H, Component.empty(),
                        (btn, v) -> { setter.accept(v); NotifControlConfig.save(); });
        tabWidgets.add(new TabWidget(w, tab, y));
        addRenderableWidget(w);
        labels.add(new LabelLine(tab, y, Component.translatable(label).getString(), () -> ""));
        return y + ROW_GAP;
    }

    private int addCycle(int y, int tab, String label, String[] values,
                         String current, java.util.function.Consumer<String> setter) {
        AbstractWidget w = CycleButton.builder(s -> Component.literal(s), current)
                .withValues(values)
                .create(controlLx, y, controlW, ROW_H, Component.empty(),
                        (btn, v) -> { setter.accept(v); NotifControlConfig.save(); });
        tabWidgets.add(new TabWidget(w, tab, y));
        addRenderableWidget(w);
        labels.add(new LabelLine(tab, y, Component.translatable(label).getString(), () -> ""));
        return y + ROW_GAP;
    }

    private int addStepper(int y, int tab, String label, String format,
                           float min, float max, float step,
                           java.util.function.Supplier<Float> getter,
                           java.util.function.Consumer<Float> setter) {
        AbstractWidget dec = Button.builder(Component.literal("-"), btn -> {
            float v = Mth.clamp(getter.get() - step, min, max);
            setter.accept(v); NotifControlConfig.save();
        }).bounds(controlLx, y, 18, ROW_H).build();
        AbstractWidget inc = Button.builder(Component.literal("+"), btn -> {
            float v = Mth.clamp(getter.get() + step, min, max);
            setter.accept(v); NotifControlConfig.save();
        }).bounds(controlRx - 18, y, 18, ROW_H).build();

        tabWidgets.add(new TabWidget(dec, tab, y));
        tabWidgets.add(new TabWidget(inc, tab, y));
        addRenderableWidget(dec);
        addRenderableWidget(inc);

        labels.add(new LabelLine(tab, y, Component.translatable(label).getString(), () -> String.format(format, getter.get())));
        return y + ROW_GAP;
    }

    private int addStepperInt(int y, int tab, String label,
                              int min, int max, int step,
                              java.util.function.Supplier<Integer> getter,
                              java.util.function.Consumer<Integer> setter) {
        AbstractWidget dec = Button.builder(Component.literal("-"), btn -> {
            int v = Mth.clamp(getter.get() - step, min, max);
            setter.accept(v); NotifControlConfig.save();
        }).bounds(controlLx, y, 18, ROW_H).build();
        AbstractWidget inc = Button.builder(Component.literal("+"), btn -> {
            int v = Mth.clamp(getter.get() + step, min, max);
            setter.accept(v); NotifControlConfig.save();
        }).bounds(controlRx - 18, y, 18, ROW_H).build();

        tabWidgets.add(new TabWidget(dec, tab, y));
        tabWidgets.add(new TabWidget(inc, tab, y));
        addRenderableWidget(dec);
        addRenderableWidget(inc);

        labels.add(new LabelLine(tab, y, Component.translatable(label).getString(), () -> String.valueOf(getter.get())));
        return y + ROW_GAP;
    }

    private int addTypeRow(int y, String name, NotifControlConfig.TypeSettings ts) {
        AbstractWidget toggle = CycleButton.onOffBuilder(ts.enabled)
                .create(controlLx, y, 38, ROW_H, Component.empty(),
                        (btn, v) -> { ts.enabled = v; NotifControlConfig.save(); });
        tabWidgets.add(new TabWidget(toggle, 1, y));
        addRenderableWidget(toggle);

        labels.add(new LabelLine(1, y, Component.translatable(name).getString(), () -> ""));

        int ci = colorIndex(ts.bgColor);
        AbstractWidget colorBtn = CycleButton.builder(s -> Component.literal(s), ci > 0 ? COLOR_NAMES[ci] : COLOR_NAMES[0])
                .withValues(COLOR_NAMES)
                .create(controlLx + 40, y, 40, ROW_H, Component.empty(),
                        (btn, v) -> {
                            for (int i = 0; i < COLOR_NAMES.length; i++) {
                                if (COLOR_NAMES[i].equals(v)) { ts.bgColor = COLOR_VALUES[i]; break; }
                            }
                            NotifControlConfig.save();
                        });
        tabWidgets.add(new TabWidget(colorBtn, 1, y));
        addRenderableWidget(colorBtn);

        return y + ROW_GAP;
    }

    // ── Render ───────────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        // 1. Sleek backdrop
        extractor.fill(0, 0, this.width, this.height, C_BG);

        // 2. Left Column Dashboard (Glassmorphic)
        extractor.fill(leftX - 5, 20, leftX + leftW + 5, this.height - 35, C_FILL);
        drawRectBorder(extractor, leftX - 5, 20, leftX + leftW + 5, this.height - 35, C_BORDER);

        // Header Title in Left Column
        extractor.centeredText(this.font, "§e§l" + Component.translatable("text.notifcontrol.preview_status").getString(), leftX + leftW / 2, 24, 0xFFFFFFFF);

        // Active profile display
        extractor.text(this.font, "§7" + Component.translatable("text.notifcontrol.status_online").getString(), leftX + 8, 148, 0xFFFFFFFF);

        // 3. Right Column Dashboard (Glassmorphic)
        extractor.fill(rightX - 5, 42, rightX + rightW + 5, this.height - 10, C_FILL);
        drawRectBorder(extractor, rightX - 5, 42, rightX + rightW + 5, this.height - 10, C_BORDER);

        // Underline active tab button
        int tabW = (rightW - 4 * 3) / 5;
        int activeTabX1 = rightX + activeTab * (tabW + 3);
        int activeTabX2 = activeTabX1 + tabW;
        extractor.fill(activeTabX1, 38, activeTabX2, 40, C_ACTIVE_TAB);

        // 4. Draw Active Tab Labels and Values
        for (LabelLine l : labels) {
            if (l.tabIndex == activeTab) {
                int Y = l.baseY - tabScrollOffset;
                if (Y < 42 || Y > this.height - 15) continue;

                // Draw label string
                extractor.text(this.font, l.label, rightX + 5, Y + 4, C_LABEL);

                // Draw value if present
                String val = l.valueSupplier.get();
                if (!val.isEmpty()) {
                    int vw = this.font.width(val);
                    int centerX = controlLx + 18 + (controlW - 36) / 2;
                    extractor.text(this.font, val, centerX - vw / 2, Y + 4, C_VALUE);
                }
            }
        }

        // Draw super widgets
        super.extractRenderState(extractor, mouseX, mouseY, delta);

        // 5. Draw dynamic real-time preview toast
        drawLivePreviewToast(extractor);

        // 6. Draw Scrollbar track if overflows
        int contentBottom = 46;
        if (activeTab == 0) {
            contentBottom = 46 + 13 * ROW_GAP;
        } else if (activeTab == 1) {
            contentBottom = 46 + 10 * ROW_GAP;
        } else if (activeTab == 2) {
            NotifControlConfig cfg = NotifControlConfig.get();
            contentBottom = 46 + 3 * ROW_GAP + Math.max(1, cfg.advancementBlacklist.size()) * ROW_GAP;
        } else if (activeTab == 3) {
            NotifControlConfig cfg = NotifControlConfig.get();
            contentBottom = 46 + 3 * ROW_GAP + Math.max(1, cfg.chatTriggerWords.size()) * ROW_GAP;
        } else if (activeTab == 4) {
            List<String> pl = NotifControlConfig.listProfiles();
            contentBottom = 46 + ROW_GAP + Math.max(1, pl.size()) * ROW_GAP + ROW_GAP + 10;
        }
        int tabAreaHeight = this.height - 55;
        int maxS = Math.max(0, contentBottom - tabAreaHeight);
        if (maxS > 0) {
            int trackStart = 45;
            int trackH = this.height - trackStart - 15;
            int barH = Math.max(12, (int) ((float) tabAreaHeight / contentBottom * trackH));
            int barY = trackStart + (int) ((float) tabScrollOffset / maxS * (trackH - barH));
            extractor.fill(rightX + rightW + 1, barY, rightX + rightW + 3, barY + barH, 0x50FFFFFF);
        }
    }

    private void drawRectBorder(GuiGraphicsExtractor extractor, int x1, int y1, int x2, int y2, int color) {
        extractor.fill(x1, y1, x2, y1 + 1, color);
        extractor.fill(x1, y2 - 1, x2, y2, color);
        extractor.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        extractor.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private void drawLivePreviewToast(GuiGraphicsExtractor extractor) {
        NotifControlConfig cfg = NotifControlConfig.get();

        int panelLX = leftX;
        int panelRX = leftX + leftW;
        int panelY1 = 35;
        int panelY2 = 110;

        extractor.fill(panelLX, panelY1, panelRX, panelY2, 0x1AFFFFFF);
        drawRectBorder(extractor, panelLX, panelY1, panelRX, panelY2, 0x15FFFFFF);

        NotifControlConfig.TypeSettings ts;
        String titleStr;
        String descStr;
        int defColor;

        if (previewType == 0) {
            ts = cfg.recipe;
            titleStr = Component.translatable("text.notifcontrol.preview.recipe.title").getString();
            descStr = Component.translatable("text.notifcontrol.preview.recipe.desc").getString();
            defColor = 0xFFB06030;
        } else if (previewType == 2) {
            ts = cfg.tutorial;
            titleStr = Component.translatable("text.notifcontrol.preview.tutorial.title").getString();
            descStr = Component.translatable("text.notifcontrol.preview.tutorial.desc").getString();
            defColor = 0xFF50C878;
        } else if (previewType == 3) {
            ts = cfg.system;
            titleStr = Component.translatable("text.notifcontrol.preview.system.title").getString();
            descStr = Component.translatable("text.notifcontrol.preview.system.desc").getString();
            defColor = 0xFF808080;
        } else {
            ts = cfg.advancement;
            titleStr = Component.translatable("text.notifcontrol.preview.advancement.title").getString();
            descStr = Component.translatable("text.notifcontrol.preview.advancement.desc").getString();
            defColor = 0xFF4A90D9;
        }

        float scale = Mth.clamp(cfg.toastScale * (ts != null ? ts.scale : 1.0f), 0.5f, 2.0f);
        float opacity = cfg.toastOpacity;

        int toastW = (int) (120 * scale);
        int toastH = (int) (26 * scale);

        toastW = Math.min(toastW, leftW - 4);

        int tx1 = leftX + leftW / 2 - toastW / 2;
        int ty1 = panelY1 + (panelY2 - panelY1) / 2 - toastH / 2;
        int tx2 = tx1 + toastW;
        int ty2 = ty1 + toastH;

        int bgColor = (ts == null || ts.bgColor == 0) ? defColor : ts.bgColor;
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >> 8) & 0xFF;
        int b = bgColor & 0xFF;
        int a = (int) ((((bgColor >> 24) & 0xFF) / 255.0f) * opacity * 255);
        if ((bgColor >> 24) == 0) {
            a = (int) (opacity * 255);
        }
        int drawColor = (a << 24) | (r << 16) | (g << 8) | b;

        extractor.fill(tx1, ty1, tx2, ty2, drawColor);

        int borderColor = ((int) (a * 0.4f) << 24) | 0xFFFFFF;
        drawRectBorder(extractor, tx1, ty1, tx2, ty2, borderColor);

        if (scale >= 0.5f) {
            int titleColor = (a << 24) | 0xFFFFFF;
            int descColor = (a << 24) | 0xAAAAAA;

            String renderTitle = titleStr;
            String renderDesc = descStr;

            if (this.font.width(renderTitle) > toastW - 8) {
                renderTitle = renderTitle.substring(0, Math.max(3, (toastW - 8) / 6)) + "..";
            }
            if (this.font.width(renderDesc) > toastW - 8) {
                renderDesc = renderDesc.substring(0, Math.max(3, (toastW - 8) / 6)) + "..";
            }

            extractor.text(this.font, renderTitle, tx1 + 4, ty1 + 3, titleColor);
            extractor.text(this.font, renderDesc, tx1 + 4, ty1 + 13, descColor);
        }

        if (ts != null && !ts.enabled) {
            extractor.fill(tx1, ty1, tx2, ty2, 0x88CC0000);
            extractor.centeredText(this.font, "§c§l" + Component.translatable("text.notifcontrol.disabled").getString(), tx1 + toastW / 2, ty1 + toastH / 2 - 4, 0xFFFFFFFF);
        }
    }

    // ── Input handling ───────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (capturingKey) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                capturingKey = false;
            } else {
                NotifControlConfig cfg = NotifControlConfig.get();
                cfg.globalToggleKey = event.key();
                NotifControlConfig.save();
                capturingKey = false;
            }
            rebuild();
            return true;
        }

        if (blacklistBox != null && blacklistBox.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ENTER) {
                String t = blacklistBox.getValue().trim();
                if (!t.isEmpty()) {
                    NotifControlConfig cfg = NotifControlConfig.get();
                    cfg.advancementBlacklist.add(t);
                    NotifControlConfig.save();
                    blacklistBox.setValue("");
                    rebuild();
                }
                return true;
            }
            return blacklistBox.keyPressed(event);
        }

        if (profileBox != null && profileBox.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ENTER) {
                String n = profileBox.getValue().trim();
                if (!n.isEmpty()) {
                    NotifControlConfig.saveProfile(n);
                    profileBox.setValue("");
                    rebuild();
                }
                return true;
            }
            return profileBox.keyPressed(event);
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (blacklistBox != null && blacklistBox.isFocused()) {
            return blacklistBox.charTyped(event);
        }
        if (profileBox != null && profileBox.isFocused()) {
            return profileBox.charTyped(event);
        }
        return super.charTyped(event);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void rebuild() {
        int savedScroll = tabScrollOffset;
        int savedTab = activeTab;
        init();
        tabScrollOffset = savedScroll;
        activeTab = savedTab;
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        applyScroll();
    }

    private void applyScroll() {
        int contentBottom = 46;
        if (activeTab == 0) {
            contentBottom = 46 + 13 * ROW_GAP;
        } else if (activeTab == 1) {
            contentBottom = 46 + 10 * ROW_GAP;
        } else if (activeTab == 2) {
            NotifControlConfig cfg = NotifControlConfig.get();
            contentBottom = 46 + 3 * ROW_GAP + Math.max(1, cfg.advancementBlacklist.size()) * ROW_GAP;
        } else if (activeTab == 3) {
            NotifControlConfig cfg = NotifControlConfig.get();
            contentBottom = 46 + 3 * ROW_GAP + Math.max(1, cfg.chatTriggerWords.size()) * ROW_GAP;
        } else if (activeTab == 4) {
            List<String> pl = NotifControlConfig.listProfiles();
            contentBottom = 46 + ROW_GAP + Math.max(1, pl.size()) * ROW_GAP + ROW_GAP + 10;
        }

        int tabAreaHeight = this.height - 55;
        int max = Math.max(0, contentBottom - tabAreaHeight);
        tabScrollOffset = Mth.clamp(tabScrollOffset, 0, max);

        for (TabWidget tw : tabWidgets) {
            if (tw.tabIndex == -1) {
                tw.widget.visible = true;
                tw.widget.active = true;
            } else if (tw.tabIndex == activeTab) {
                int Y = tw.baseY - tabScrollOffset;
                tw.widget.setY(Y);

                if (Y < 42 || Y > this.height - 15) {
                    tw.widget.visible = false;
                    tw.widget.active = false;
                } else {
                    tw.widget.visible = true;
                    tw.widget.active = true;
                }
            } else {
                tw.widget.visible = false;
                tw.widget.active = false;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= rightX - 5) {
            tabScrollOffset -= (int) (scrollY * 14);
            applyScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}

