package dev.betrix.lifeindalensk.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to disable the default health regeneration from hunger.
 * Players must use medical items to heal instead.
 * Starvation damage is preserved.
 */
@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Shadow
    private int foodTickTimer;

    @Shadow
    private int foodLevel;

    @Shadow
    private float saturationLevel;

    /**
     * Replaces the update method to prevent natural health regeneration
     * while keeping starvation damage active.
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void disableHealthRegeneration(PlayerEntity player, CallbackInfo ci) {
        Difficulty difficulty = player.getWorld().getDifficulty();

        // Only handle starvation damage, skip all health regeneration
        if (this.foodLevel <= 0) {
            this.foodTickTimer++;
            if (this.foodTickTimer >= 80) {
                if (player.getHealth() > 10.0f || difficulty == Difficulty.HARD
                        || player.getHealth() > 1.0f && difficulty == Difficulty.NORMAL) {
                    player.damage(player.getDamageSources().starve(), 1.0f);
                }
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }

        ci.cancel();
    }
}
