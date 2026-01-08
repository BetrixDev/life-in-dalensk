package dev.betrix.lifeindalensk.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * A bleeding effect that periodically damages the affected entity.
 * Deals 0.5 hearts (half a heart) of damage every 5 seconds (100 ticks).
 */
public class BleedingEffect extends StatusEffect {

    /**
     * Damage interval in ticks: 5 seconds = 100 ticks
     */
    private static final int DAMAGE_INTERVAL = 100;

    /**
     * Damage amount: 0.5 hearts (half a heart)
     */
    private static final float DAMAGE_AMOUNT = 0.5f;

    public BleedingEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B0000); // Dark red color
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Deal damage every DAMAGE_INTERVAL ticks
        entity.damage(entity.getDamageSources().generic(), DAMAGE_AMOUNT);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Apply effect every DAMAGE_INTERVAL ticks
        return duration % DAMAGE_INTERVAL == 0;
    }
}
