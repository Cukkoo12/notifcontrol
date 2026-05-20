package com.cukkoo.notifcontrol.mixin;

import com.cukkoo.notifcontrol.NotifControlConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.cukkoo.notifcontrol.NotifControlHistory;
import java.util.List;

/**
 * Overrides ToastManager behaviour:
 * - getNotificationDisplayTimeMultiplier → applies toastDurationMultiplier
 * - freeSlotCount → applies maxVisibleToasts cap
 */
@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private List<?> visibleToasts;

    @Shadow protected abstract int freeSlotCount();

    /**
     * Multiply the base notification time by the config value.
     * Injects at RETURN and multiplies the original result.
     */
    @Inject(method = "getNotificationDisplayTimeMultiplier", at = @At("RETURN"), cancellable = true)
    private void onGetNotificationDisplayTimeMultiplier(CallbackInfoReturnable<Double> cir) {
        NotifControlConfig cfg = NotifControlConfig.get();
        double base = cir.getReturnValue();
        double multiplier = Mth.clamp(cfg.toastDurationMultiplier, 0.1f, 5.0f);
        cir.setReturnValue(base * multiplier);
    }

    /**
     * Cap the number of free slots based on maxVisibleToasts.
     * We cap it so at most maxVisibleToasts slots are used.
     */
    @Inject(method = "freeSlotCount", at = @At("RETURN"), cancellable = true)
    private void onFreeSlotCount(CallbackInfoReturnable<Integer> cir) {
        NotifControlConfig cfg = NotifControlConfig.get();
        int normalFree = cir.getReturnValue();
        int occupied = this.visibleToasts.size();
        int maxAllowed = Mth.clamp(cfg.maxVisibleToasts, 1, 10);

        if (occupied >= maxAllowed) {
            cir.setReturnValue(0);
        } else {
            cir.setReturnValue(Math.min(normalFree, maxAllowed - occupied));
        }
    }
}
