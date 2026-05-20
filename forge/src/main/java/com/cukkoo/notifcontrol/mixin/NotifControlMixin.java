package com.cukkoo.notifcontrol.mixin;

import com.cukkoo.notifcontrol.NotifControlConfig;
import com.cukkoo.notifcontrol.NotifControlHistory;
import net.minecraft.client.gui.components.toasts.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Intercepts ToastManager#addToast before any toast is queued.
 * Handles all toast type filtering, blacklists, stacking, and history logging.
 */
@Mixin(ToastManager.class)
public abstract class NotifControlMixin {

    private static final Map<String, SystemToast.SystemToastId> TOAST_ID_MAP = buildToastIdMap();
    private static final Map<String, Long> LAST_TOAST_TIME = new ConcurrentHashMap<>();
    private static final Map<String, Integer> STACK_COUNTS = new ConcurrentHashMap<>();

    private static Class<?> lastToastClass = null;
    private static long lastToastTime = 0;

    private static Map<String, SystemToast.SystemToastId> buildToastIdMap() {
        Map<String, SystemToast.SystemToastId> map = new HashMap<>();
        for (Field field : SystemToast.SystemToastId.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.getType() == SystemToast.SystemToastId.class) {
                try {
                    map.put(field.getName(), (SystemToast.SystemToastId) field.get(null));
                } catch (IllegalAccessException ignored) {}
            }
        }
        return map;
    }

    @Inject(
        method = "addToast(Lnet/minecraft/client/gui/components/toasts/Toast;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void notifcontrol$onAdd(Toast toast, CallbackInfo ci) {
        NotifControlConfig cfg = NotifControlConfig.get();

        // 1. DND Mode Check
        if (cfg.dndMode) {
            if (cfg.logBlockedToasts) {
                NotifControlHistory.add(toast.getClass().getSimpleName(), "DND Active - Hidden", true);
            }
            ci.cancel();
            return;
        }

        // 2. Smart Merging Check (consecutive identical classes within 2 seconds)
        if (cfg.smartMerging) {
            long now = System.currentTimeMillis();
            if (lastToastClass == toast.getClass() && (now - lastToastTime) < 2000) {
                if (cfg.logBlockedToasts) {
                    NotifControlHistory.add(toast.getClass().getSimpleName(), "Merged (Smart Merging)", true);
                }
                ci.cancel();
                return;
            }
            lastToastTime = now;
            lastToastClass = toast.getClass();
        }

        boolean blocked = false;
        String toastType = "UNKNOWN";
        String toastTitle = "";

        // 3. Type-based filtering using per-type settings
        if (toast instanceof RecipeToast) {
            toastType = "Recipe";
            if (!cfg.recipe.enabled) blocked = true;
        } else if (toast instanceof AdvancementToast) {
            toastType = "Advancement";
            if (!cfg.advancement.enabled) blocked = true;
        } else if (toast instanceof TutorialToast) {
            toastType = "Tutorial";
            if (!cfg.tutorial.enabled) blocked = true;
        } else if (toast instanceof SystemToast) {
            toastType = "System";
            if (!cfg.system.enabled) blocked = true;
        }

        // 4. Extract Toast ID/Title for filtering
        if (toast instanceof AdvancementToast) {
            AdvancementToastAccessor accessor = (AdvancementToastAccessor) toast;
            toastTitle = accessor.getAdvancement().id().toString();
        } else if (toast instanceof SystemToast) {
            SystemToast st = (SystemToast) toast;
            Object token = st.getToken();
            if (token instanceof SystemToast.SystemToastId) {
                for (Map.Entry<String, SystemToast.SystemToastId> entry : TOAST_ID_MAP.entrySet()) {
                    if (entry.getValue() == token) {
                        toastTitle = entry.getKey();
                        break;
                    }
                }
            }
        }

        // 5. Advanced Filtering (Regex & Whitelist)
        if (!blocked && !toastTitle.isEmpty() && !cfg.advancementBlacklist.isEmpty()) {
            boolean matched = false;
            for (String filter : cfg.advancementBlacklist) {
                if (cfg.regexEnabled) {
                    try {
                        if (toastTitle.matches(filter)) { matched = true; break; }
                    } catch (Exception ignored) {}
                } else {
                    if (toastTitle.contains(filter)) { matched = true; break; }
                }
            }
            
            if (cfg.filterMode.equals("WHITELIST")) {
                if (!matched) blocked = true;
            } else { // BLACKLIST
                if (matched) blocked = true;
            }
        }

        // 6. Stacking Check
        boolean stacked = false;
        if (!blocked && cfg.stackingEnabled) {
            String stackKey = toastType + "|" + toastTitle;
            int count = STACK_COUNTS.merge(stackKey, 1, Integer::sum);
            if (count > 1) {
                blocked = true;
                stacked = true;
            }
        }

        // 7. Spam delay cooldown
        if (!blocked && cfg.toastSpamDelayMs > 0) {
            long now = System.currentTimeMillis();
            long last = LAST_TOAST_TIME.getOrDefault(toastType, 0L);
            if (now - last < cfg.toastSpamDelayMs) {
                blocked = true;
            } else {
                LAST_TOAST_TIME.put(toastType, now);
            }
        }

        // 8. Log and Cancel or Allow
        if (blocked) {
            if (cfg.logBlockedToasts) {
                String logTitle = toastTitle.isEmpty() ? toastType : toastTitle;
                if (stacked) logTitle += " (STACKED)";
                NotifControlHistory.add(toastType, logTitle, true);
            }
            ci.cancel();
        } else {
            // Log as displayed
            NotifControlHistory.add(toastType, toastTitle.isEmpty() ? toastType : toastTitle, false);
            
            // 9. Custom Sound (only play if not blocked)
            if (cfg.customSoundEnabled) {
                try {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    net.minecraft.sounds.SoundEvent soundEvent = null;
                    for (net.minecraft.sounds.SoundEvent event : net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT) {
                        if (String.valueOf(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(event)).equals(cfg.customSoundEvent)) {
                            soundEvent = event;
                            break;
                        }
                    }
                    if (soundEvent != null) {
                        mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(soundEvent, 1.0F));
                    }
                } catch (Exception e) {
                    com.cukkoo.notifcontrol.NotifControlMod.LOGGER.error("[NotifControl] Failed to play custom toast sound", e);
                }
            }
        }
    }
}
