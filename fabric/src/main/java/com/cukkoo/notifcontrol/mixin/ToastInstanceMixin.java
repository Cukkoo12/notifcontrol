package com.cukkoo.notifcontrol.mixin;

import com.cukkoo.notifcontrol.NotifControlConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.*;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Injects into ToastInstance.extractRenderState to apply:
 * - toastScale (scale transform)
 * - toastOffsetX / toastOffsetY (position offset)
 * - toastPosition (TOP_LEFT / BOTTOM_RIGHT / BOTTOM_LEFT)
 * - animationStyle NONE (skip slide-in)
 * - Per-type settings (scale, bgColor)
 * - Stacking and Opacity
 * <p>
 * Target: net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance
 * Method: extractRenderState(GuiGraphicsExtractor, int)
 */
@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public abstract class ToastInstanceMixin {

    @Shadow(remap = false)
    private boolean hasFinishedRendering;

    @Shadow(remap = false)
    private float visiblePortion;

    @Shadow(remap = false)
    private Toast toast;

    @Unique
    private int notifcontrol$startStratumCount;
    @Unique
    private int notifcontrol$startElementCount;
    @Unique
    private int notifcontrol$startTextCount;
    @Unique
    private long notifcontrol$spawnTime = 0;
    
    @Unique
    private float notifcontrol$currentX = 0;
    @Unique
    private float notifcontrol$currentY = 0;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
        at = @At("HEAD")
    )
    private void beforeToastRender(GuiGraphicsExtractor extractor, int screenWidth, CallbackInfo ci) {
        if (this.notifcontrol$spawnTime == 0) this.notifcontrol$spawnTime = System.currentTimeMillis();
        
        if (hasFinishedRendering) return;

        NotifControlConfig cfg = NotifControlConfig.get();

        // Capture current strata, element and text sizes to post-process newly added elements
        try {
            Field strataField = extractor.guiRenderState.getClass().getDeclaredField("strata");
            strataField.setAccessible(true);
            List<?> strata = (List<?>) strataField.get(extractor.guiRenderState);
            this.notifcontrol$startStratumCount = strata.size();
            if (this.notifcontrol$startStratumCount > 0) {
                Object lastStratum = strata.get(this.notifcontrol$startStratumCount - 1);
                this.notifcontrol$startElementCount = getElementStatesSize(lastStratum);
                this.notifcontrol$startTextCount = getTextStatesSize(lastStratum);
            } else {
                this.notifcontrol$startElementCount = 0;
                this.notifcontrol$startTextCount = 0;
            }
        } catch (Exception e) {
            this.notifcontrol$startStratumCount = 0;
            this.notifcontrol$startElementCount = 0;
            this.notifcontrol$startTextCount = 0;
        }

        // animationStyle NONE: skip slide-in animation by making toast fully visible
        if (cfg.animationStyle.equals("NONE") && this.visiblePortion < 1.0f) {
            this.visiblePortion = 1.0f;
        }
    }

    @ModifyArg(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
        at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"),
        index = 0,
        require = 0
    )
    private float modifyTranslateX(float x) {
        NotifControlConfig cfg = NotifControlConfig.get();
        String pos = cfg.toastPosition;
        float p = this.visiblePortion;
        
        // Handle custom animation curve
        if (cfg.animationStyle.equals("BOUNCE")) {
            p = 1.0f - (float)Math.cos(p * Math.PI * 2.5f) * (float)Math.exp(-p * 3.0f);
        } else if (!cfg.animationStyle.equals("SLIDE")) {
            p = 1.0f; // No slide for FADE, POP, NONE
        }

        // Vanilla original X calculation uses visiblePortion. We reverse engineer it to get the final X.
        float finalX = x - 160.0f * (1.0f - this.visiblePortion); 
        float base = finalX + 160.0f * (1.0f - p);

        if (pos.equals("TOP_LEFT") || pos.equals("BOTTOM_LEFT")) {
            base = -160.0f * (1.0f - p);
        } else if (pos.equals("FREEFORM")) {
            base = 160.0f * p; // Freeform uses base zero + offsets
        }
        
        base += cfg.toastOffsetX;
        
        if (cfg.toastPhysics) {
            double mx = Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
            double my = Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();
            float dx = (float)mx - base;
            float dy = (float)my - this.notifcontrol$currentY;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < 60 && dist > 0.001f) {
                float force = (60 - dist) / 60.0f;
                base -= (dx / dist) * force * 15.0f;
            }
        }
        
        this.notifcontrol$currentX = base;
        return base;
    }

    @ModifyArg(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
        at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"),
        index = 1,
        require = 0
    )
    private float modifyTranslateY(float y) {
        NotifControlConfig cfg = NotifControlConfig.get();
        String pos = cfg.toastPosition;
        float base = y;
        if (pos.equals("BOTTOM_RIGHT") || pos.equals("BOTTOM_LEFT")) {
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            base = Math.max(0.0f, screenHeight - 160.0f + y);
        } else if (pos.equals("FREEFORM")) {
            base = y;
        }
        base += cfg.toastOffsetY;
        
        if (cfg.toastPhysics) {
            double mx = Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
            double my = Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();
            float dx = (float)mx - this.notifcontrol$currentX;
            float dy = (float)my - base;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < 60 && dist > 0.001f) {
                float force = (60 - dist) / 60.0f;
                base -= (dy / dist) * force * 15.0f;
            }
        }
        
        this.notifcontrol$currentY = base;
        return base;
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
        at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", shift = At.Shift.AFTER),
        require = 0
    )
    private void afterVanillaTranslate(GuiGraphicsExtractor extractor, int screenWidth, CallbackInfo ci) {
        if (hasFinishedRendering) return;

        NotifControlConfig cfg = NotifControlConfig.get();
        float scale = Mth.clamp(cfg.toastScale, 0.5f, 2.0f);
        NotifControlConfig.TypeSettings ts = getTypeSettings(cfg);
        if (ts != null) {
            scale *= Mth.clamp(ts.scale, 0.5f, 2.0f);
        }

        if (cfg.animationStyle.equals("POP")) {
            float p = this.visiblePortion;
            float popScale = p < 0.5f ? 2.0f * p * p : 1.0f - (float)Math.pow(-2.0f * p + 2.0f, 3) / 2.0f;
            scale *= popScale;
        }

        if (scale != 1.0f) {
            // Translate to center of toast before scaling to keep it centered
            extractor.pose().translate(80.0f, 16.0f);
            extractor.pose().scale(scale, scale);
            extractor.pose().translate(-80.0f, -16.0f);
        }
    }

    @Inject(
        method = "update()V",
        at = @At("HEAD"),
        require = 0
    )
    private void onUpdate(CallbackInfo ci) {
        NotifControlConfig cfg = NotifControlConfig.get();
        if (cfg.animationStyle.equals("NONE") && this.visiblePortion < 1.0f) {
            this.visiblePortion = 1.0f;
        }
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
        at = @At("RETURN")
    )
    private void afterToastRender(GuiGraphicsExtractor extractor, int screenWidth, CallbackInfo ci) {
        if (hasFinishedRendering) return;

        NotifControlConfig cfg = NotifControlConfig.get();
        NotifControlConfig.TypeSettings ts = getTypeSettings(cfg);

        // Post-process elements to apply global opacity and background tint
        float opacity = cfg.toastOpacity;
        if (cfg.animationStyle.equals("FADE")) {
            opacity *= this.visiblePortion;
        }

        float red = 1.0f, green = 1.0f, blue = 1.0f, alpha = opacity;
        
        if (cfg.themeMode.equals("HOLOGRAPHIC")) {
            alpha = 0.3f * opacity;
        } else if (ts != null && ts.bgColor != 0) {
            red = ((ts.bgColor >> 16) & 0xFF) / 255.0f;
            green = ((ts.bgColor >> 8) & 0xFF) / 255.0f;
            blue = (ts.bgColor & 0xFF) / 255.0f;
            alpha = (((ts.bgColor >> 24) & 0xFF) / 255.0f) * opacity;
        } else if (cfg.themeMode.equals("RGB")) {
            long time = System.currentTimeMillis();
            red = (float)((Math.sin(time * 0.002) * 127 + 128) / 255.0);
            green = (float)((Math.sin(time * 0.002 + 2) * 127 + 128) / 255.0);
            blue = (float)((Math.sin(time * 0.002 + 4) * 127 + 128) / 255.0);
        }

        tintNewlyAddedElements(extractor, this.notifcontrol$startStratumCount, this.notifcontrol$startElementCount, this.notifcontrol$startTextCount, red, green, blue, alpha, opacity, ts, cfg);
        
        // Progress Bar
        if (cfg.progressBar) {
            float maxDuration = 5000 * cfg.toastDurationMultiplier; 
            long elapsed = System.currentTimeMillis() - this.notifcontrol$spawnTime;
            float progress = Math.max(0, 1.0f - (elapsed / maxDuration));
            int barWidth = (int)(160 * progress);
            if (cfg.compactMode) barWidth = (int)(60 * progress);
            
            int pbColor = 0xFF00E5FF;
            if (cfg.themeMode.equals("RGB")) {
                long time = System.currentTimeMillis();
                int r = (int)(Math.sin(time * 0.002) * 127 + 128);
                int g = (int)(Math.sin(time * 0.002 + 2) * 127 + 128);
                int b = (int)(Math.sin(time * 0.002 + 4) * 127 + 128);
                pbColor = (255 << 24) | (r << 16) | (g << 8) | b;
            }
            
            extractor.fill(0, 29, barWidth, 31, pbColor);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private NotifControlConfig.TypeSettings getTypeSettings(NotifControlConfig cfg) {
        if (toast instanceof RecipeToast) return cfg.recipe;
        if (toast instanceof AdvancementToast) return cfg.advancement;
        if (toast instanceof TutorialToast) return cfg.tutorial;
        if (toast instanceof SystemToast) return cfg.system;
        return null;
    }

    @Unique
    private int getStrataSize(GuiGraphicsExtractor extractor) {
        try {
            Field strataField = extractor.guiRenderState.getClass().getDeclaredField("strata");
            strataField.setAccessible(true);
            List<?> strata = (List<?>) strataField.get(extractor.guiRenderState);
            return strata.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Unique
    private int getElementStatesSize(Object stratum) {
        try {
            Field elementStatesField = stratum.getClass().getDeclaredField("elementStates");
            elementStatesField.setAccessible(true);
            List<?> list = (List<?>) elementStatesField.get(stratum);
            return list.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Unique
    private int getTextStatesSize(Object stratum) {
        try {
            Field textStatesField = stratum.getClass().getDeclaredField("textStates");
            textStatesField.setAccessible(true);
            List<?> list = (List<?>) textStatesField.get(stratum);
            return list.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void tintNewlyAddedElements(GuiGraphicsExtractor extractor, int startStratumCount, int startElementCount, int startTextCount, float red, float green, float blue, float alpha, float opacity, NotifControlConfig.TypeSettings ts, NotifControlConfig cfg) {
        try {
            Field strataField = extractor.guiRenderState.getClass().getDeclaredField("strata");
            strataField.setAccessible(true);
            List<?> strata = (List<?>) strataField.get(extractor.guiRenderState);
            if (strata == null) return;

            for (int i = Math.max(0, startStratumCount - 1); i < strata.size(); i++) {
                Object node = strata.get(i);
                if (node == null) continue;

                int elemStart = (i == startStratumCount - 1) ? startElementCount : 0;
                int textStart = (i == startStratumCount - 1) ? startTextCount : 0;

                // Tint elementStates (BlitRenderState, etc.)
                Field elementStatesField = node.getClass().getDeclaredField("elementStates");
                elementStatesField.setAccessible(true);
                Object rawElementStates = elementStatesField.get(node);
                if (rawElementStates instanceof List) {
                    List<Object> elementStates = (List<Object>) rawElementStates;
                    int elemSize = elementStates.size();
                    for (int j = elemStart; j < elemSize; j++) {
                        Object state = elementStates.get(j);
                        if (state instanceof BlitRenderState) {
                            BlitRenderState old = (BlitRenderState) state;
                            
                            // If hideIcon is true, hide non-background blits (usually j > elemStart)
                            float elemAlpha = alpha;
                            if (ts != null && ts.hideIcon && j > elemStart) {
                                elemAlpha = 0.0f; // hide it
                            }
                            
                            int newColor = applyTint(old.color(), red, green, blue, elemAlpha);
                            
                            int newX1 = old.x1();
                            if (cfg.compactMode && (old.x1() - old.x0()) >= 150) {
                                newX1 = old.x0() + 65; // Compact size
                            }

                            BlitRenderState tinted = new BlitRenderState(
                                old.pipeline(), old.textureSetup(), old.pose(),
                                old.x0(), old.y0(), newX1, old.y1(),
                                old.u0(), old.u1(), old.v0(), old.v1(),
                                newColor,
                                old.scissorArea(), old.bounds()
                            );
                            elementStates.set(j, tinted);
                        }
                    }
                }

                // Tint textStates (GuiTextRenderState)
                Field textStatesField = node.getClass().getDeclaredField("textStates");
                textStatesField.setAccessible(true);
                Object rawTextStates = textStatesField.get(node);
                if (rawTextStates instanceof List) {
                    List<Object> textStates = (List<Object>) rawTextStates;
                    int textSize = textStates.size();
                    for (int j = textStart; j < textSize; j++) {
                        Object state = textStates.get(j);
                        if (state instanceof GuiTextRenderState) {
                            GuiTextRenderState old = (GuiTextRenderState) state;
                            
                            int baseColor = old.color;
                            if (cfg.themeMode.equals("HOLOGRAPHIC")) {
                                baseColor = 0xFF00FFFF;
                            } else if (cfg.themeMode.equals("RGB")) {
                                long time = System.currentTimeMillis();
                                int r = (int)(Math.sin(time * 0.002) * 127 + 128);
                                int g = (int)(Math.sin(time * 0.002 + 2) * 127 + 128);
                                int b = (int)(Math.sin(time * 0.002 + 4) * 127 + 128);
                                baseColor = (255 << 24) | (r << 16) | (g << 8) | b;
                            } else if (ts != null) {
                                // Usually the first text is the title, second is description
                                if (j == textStart) {
                                    if (ts.titleColor != 0xFFFFFFFF) baseColor = ts.titleColor;
                                } else {
                                    if (ts.descColor != 0xFFAAAAAA) baseColor = ts.descColor;
                                }
                            }
                            
                            if (cfg.compactMode && j > textStart) {
                                baseColor = 0x00000000; // Hide description in compact mode
                            }
                            
                            int newColor = applyTint(baseColor, 1.0f, 1.0f, 1.0f, opacity);

                            Field includeEmptyField = GuiTextRenderState.class.getDeclaredField("includeEmpty");
                            includeEmptyField.setAccessible(true);
                            boolean includeEmpty = (boolean) includeEmptyField.get(old);

                            GuiTextRenderState tinted = new GuiTextRenderState(
                                old.font, old.text, old.pose,
                                old.x, old.y, newColor, old.backgroundColor,
                                old.dropShadow, includeEmpty, old.scissor
                            );
                            textStates.set(j, tinted);
                        }
                    }
                }
            }
        } catch (Exception e) {
            com.cukkoo.notifcontrol.NotifControlMod.LOGGER.error("[NotifControlDebug] tintNewlyAddedElements failed", e);
        }
    }

    @Unique
    private int applyTint(int originalARGB, float r, float g, float b, float a) {
        int originalA = (originalARGB >> 24) & 0xFF;
        int originalR = (originalARGB >> 16) & 0xFF;
        int originalG = (originalARGB >> 8) & 0xFF;
        int originalB = originalARGB & 0xFF;

        int tintedA = Math.min(255, Math.max(0, (int) (originalA * a)));
        int tintedR = Math.min(255, Math.max(0, (int) (originalR * r)));
        int tintedG = Math.min(255, Math.max(0, (int) (originalG * g)));
        int tintedB = Math.min(255, Math.max(0, (int) (originalB * b)));

        return (tintedA << 24) | (tintedR << 16) | (tintedG << 8) | tintedB;
    }
}
