package com.cukkoo.notifcontrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NotifControlConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("notifcontrol.json");
    private static final Path PROFILES_DIR = FMLPaths.CONFIGDIR.get().resolve("notifcontrol").resolve("profiles");

    private static NotifControlConfig INSTANCE = new NotifControlConfig();

    // ── Per-type settings ────────────────────────────────────────────────────────
    public TypeSettings recipe = new TypeSettings(true, 1.0f, 1.0f, 0xFFB06030);
    public TypeSettings advancement = new TypeSettings(true, 1.0f, 1.0f, 0xFF4A90D9);
    public TypeSettings tutorial = new TypeSettings(true, 1.0f, 1.0f, 0xFF50C878);
    public TypeSettings system = new TypeSettings(true, 1.0f, 1.0f, 0xFF808080);

    // ── Global toast behaviour ───────────────────────────────────────────────────
    public float toastDurationMultiplier = 1.0f;
    public int maxVisibleToasts = 5;
    public int toastSpamDelayMs = 0;
    public float toastScale = 1.0f;
    public float toastOpacity = 1.0f;
    public String toastPosition = "TOP_RIGHT";
    public int toastOffsetX = 0;
    public int toastOffsetY = 0;
    public String animationStyle = "SLIDE";
    public float animationSpeed = 1.0f;
    public boolean stackingEnabled = false;
    public boolean customBackgroundsEnabled = false;
    public int toastCornerRadius = 8;
    
    // ── V3 Ultimate Features ─────────────────────────────────────────────────────
    public boolean dndMode = false;
    public boolean smartMerging = true;
    public boolean compactMode = false;
    public boolean toastPhysics = true;
    public boolean progressBar = true;
    public String themeMode = "VANILLA"; // VANILLA, RGB, HOLOGRAPHIC
    public boolean customSoundEnabled = false;
    public String customSoundEvent = "minecraft:entity.experience_orb.pickup";
    public boolean freeformAnchor = false;

    // ── Filters ─────────────────────────────────────────────────────────────────
    public List<String> systemToastFilter = new ArrayList<>();
    public List<String> advancementBlacklist = new ArrayList<>();
    public String filterMode = "BLACKLIST";
    public boolean regexEnabled = false;
    public boolean logBlockedToasts = false;

    // ── Chat To Toast ───────────────────────────────────────────────────────────
    public boolean chatToToastEnabled = false;
    public List<String> chatTriggerWords = new ArrayList<>(List.of("@", "whispers to you"));
    public boolean blockOriginalChat = false;

    // ── Keybind ─────────────────────────────────────────────────────────────────
    public int globalToggleKey = -1; // -1 = unbound, GLFW key code otherwise

    // ── TypeSettings ─────────────────────────────────────────────────────────────

    public static class TypeSettings {
        public boolean enabled = true;
        public float durationMultiplier = 1.0f;
        public float scale = 1.0f;
        public int bgColor = 0; // 0 = use default vanilla, otherwise ARGB tint
        public boolean hideIcon = false;
        public int titleColor = 0xFFFFFFFF;
        public int descColor = 0xFFAAAAAA;
        public boolean muteSound = false;

        public TypeSettings() {}

        public TypeSettings(boolean enabled, float durationMultiplier, float scale, int bgColor) {
            this.enabled = enabled;
            this.durationMultiplier = durationMultiplier;
            this.scale = scale;
            this.bgColor = bgColor;
            this.hideIcon = false;
            this.titleColor = 0xFFFFFFFF;
            this.descColor = 0xFFAAAAAA;
            this.muteSound = false;
        }

        public static TypeSettings defaults() {
            return new TypeSettings(true, 1.0f, 1.0f, 0);
        }
    }

    // ── Accessors ───────────────────────────────────────────────────────────────

    public static NotifControlConfig get() {
        return INSTANCE;
    }

    // ── IO ──────────────────────────────────────────────────────────────────────

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                NotifControlConfig loaded = GSON.fromJson(reader, NotifControlConfig.class);
                if (loaded != null) {
                    if (loaded.systemToastFilter == null) loaded.systemToastFilter = new ArrayList<>();
                    if (loaded.advancementBlacklist == null) loaded.advancementBlacklist = new ArrayList<>();
                    if (loaded.chatTriggerWords == null) loaded.chatTriggerWords = new ArrayList<>(List.of("@", "whispers to you"));
                    if (loaded.filterMode == null) loaded.filterMode = "BLACKLIST";
                    if (loaded.animationStyle == null) loaded.animationStyle = "SLIDE";
                    if (loaded.themeMode == null) loaded.themeMode = "VANILLA";
                    if (loaded.customSoundEvent == null) loaded.customSoundEvent = "minecraft:entity.experience_orb.pickup";
                    loaded.fillMissingTypeSettings();
                    INSTANCE = loaded;
                }
            } catch (IOException e) {
                NotifControlMod.LOGGER.error("[NotifControl] Could not read config, using defaults.", e);
            }
        }
        ensureTypeSettings();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            NotifControlMod.LOGGER.error("[NotifControl] Could not save config.", e);
        }
    }

    // ── Migration helpers ───────────────────────────────────────────────────────

    private static void ensureTypeSettings() {
        if (INSTANCE.recipe == null) INSTANCE.recipe = new TypeSettings(true, 1.0f, 1.0f, 0xFFB06030);
        if (INSTANCE.advancement == null) INSTANCE.advancement = new TypeSettings(true, 1.0f, 1.0f, 0xFF4A90D9);
        if (INSTANCE.tutorial == null) INSTANCE.tutorial = new TypeSettings(true, 1.0f, 1.0f, 0xFF50C878);
        if (INSTANCE.system == null) INSTANCE.system = new TypeSettings(true, 1.0f, 1.0f, 0xFF808080);
    }

    private void fillMissingTypeSettings() {
        if (recipe == null) recipe = new TypeSettings(true, 1.0f, 1.0f, 0xFFB06030);
        if (advancement == null) advancement = new TypeSettings(true, 1.0f, 1.0f, 0xFF4A90D9);
        if (tutorial == null) tutorial = new TypeSettings(true, 1.0f, 1.0f, 0xFF50C878);
        if (system == null) system = new TypeSettings(true, 1.0f, 1.0f, 0xFF808080);
    }

    // ── Profile system ──────────────────────────────────────────────────────────

    public static void saveProfile(String name) {
        try {
            Files.createDirectories(PROFILES_DIR);
            Path profilePath = PROFILES_DIR.resolve(name + ".json");
            try (Writer writer = Files.newBufferedWriter(profilePath)) {
                GSON.toJson(INSTANCE, writer);
            }
            NotifControlMod.LOGGER.info("[NotifControl] Saved profile: {}", name);
        } catch (IOException e) {
            NotifControlMod.LOGGER.error("[NotifControl] Could not save profile '{}'", name, e);
        }
    }

    public static boolean loadProfile(String name) {
        Path profilePath = PROFILES_DIR.resolve(name + ".json");
        if (!Files.exists(profilePath)) return false;
        try (Reader reader = Files.newBufferedReader(profilePath)) {
            NotifControlConfig loaded = GSON.fromJson(reader, NotifControlConfig.class);
            if (loaded != null) {
                if (loaded.systemToastFilter == null) loaded.systemToastFilter = new ArrayList<>();
                if (loaded.advancementBlacklist == null) loaded.advancementBlacklist = new ArrayList<>();
                if (loaded.chatTriggerWords == null) loaded.chatTriggerWords = new ArrayList<>(List.of("@", "whispers to you"));
                if (loaded.filterMode == null) loaded.filterMode = "BLACKLIST";
                if (loaded.animationStyle == null) loaded.animationStyle = "SLIDE";
                if (loaded.themeMode == null) loaded.themeMode = "VANILLA";
                if (loaded.customSoundEvent == null) loaded.customSoundEvent = "minecraft:entity.experience_orb.pickup";
                loaded.fillMissingTypeSettings();
                INSTANCE = loaded;
                save();
                return true;
            }
        } catch (IOException e) {
            NotifControlMod.LOGGER.error("[NotifControl] Could not load profile '{}'", name, e);
        }
        return false;
    }

    public static List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        if (Files.exists(PROFILES_DIR)) {
            try (var paths = Files.list(PROFILES_DIR)) {
                paths.filter(p -> p.toString().endsWith(".json"))
                     .map(p -> p.getFileName().toString().replace(".json", ""))
                     .sorted()
                     .forEach(names::add);
            } catch (IOException ignored) {}
        }
        return names;
    }

    public static boolean deleteProfile(String name) {
        try {
            return Files.deleteIfExists(PROFILES_DIR.resolve(name + ".json"));
        } catch (IOException e) {
            return false;
        }
    }

    // ── Export / Import ─────────────────────────────────────────────────────────

    public static String exportToJson() {
        return GSON.toJson(INSTANCE);
    }

    public static boolean importFromJson(String json) {
        try {
            NotifControlConfig loaded = GSON.fromJson(json, NotifControlConfig.class);
            if (loaded == null) return false;
            loaded.fillMissingTypeSettings();
            INSTANCE = loaded;
            save();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
