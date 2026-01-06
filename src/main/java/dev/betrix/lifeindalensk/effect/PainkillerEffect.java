package dev.betrix.lifeindalensk.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * A painkiller effect that provides various pain-relief features.
 * Designed to be extensible - add new features to the PainkillerFeature enum
 * and implement their logic in the appropriate places.
 */
public class PainkillerEffect extends StatusEffect {

    /**
     * Features that the painkiller effect can provide.
     * Add new features here and implement their logic where needed.
     */
    public enum Feature {
        /**
         * Negates the movement speed slowdown from the broken leg effect.
         * The broken leg will still cause damage when sprinting or jumping.
         */
        NEGATE_BROKEN_LEG_SLOWDOWN,
        
        // Future features can be added here, for example:
        // REDUCE_DAMAGE_FLASH,
        // NEGATE_MINING_FATIGUE,
        // REDUCE_POISON_DAMAGE,
    }

    private final Set<Feature> activeFeatures;

    public PainkillerEffect() {
        this(EnumSet.allOf(Feature.class));
    }

    public PainkillerEffect(Set<Feature> features) {
        super(StatusEffectCategory.BENEFICIAL, 0xE8D5B7); // Light tan/beige color
        this.activeFeatures = EnumSet.copyOf(features);
    }

    /**
     * Check if this painkiller effect has a specific feature enabled.
     * 
     * @param feature The feature to check
     * @return true if the feature is active
     */
    public boolean hasFeature(Feature feature) {
        return activeFeatures.contains(feature);
    }

    /**
     * Get all active features of this painkiller effect.
     * 
     * @return An unmodifiable set of active features
     */
    public Set<Feature> getActiveFeatures() {
        return Collections.unmodifiableSet(activeFeatures);
    }

    /**
     * Static helper to check if an entity has the painkiller effect with a specific feature.
     * Useful for checking from mixins or other classes.
     * 
     * @param entity The entity to check
     * @param effect The painkiller status effect registry entry
     * @param feature The feature to check for
     * @return true if the entity has painkiller with the specified feature
     */
    public static boolean hasFeature(LivingEntity entity, net.minecraft.registry.entry.RegistryEntry<StatusEffect> effect, Feature feature) {
        if (!entity.hasStatusEffect(effect)) {
            return false;
        }
        
        StatusEffect statusEffect = effect.value();
        if (statusEffect instanceof PainkillerEffect painkillerEffect) {
            return painkillerEffect.hasFeature(feature);
        }
        return false;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        // The painkiller effect itself doesn't apply any direct effects.
        // Its benefits are checked by other systems (like the broken leg mixin).
        // 
        // Future tick-based features can be implemented here, for example:
        // if (hasFeature(Feature.REDUCE_POISON_DAMAGE)) {
        //     // Reduce poison damage logic
        // }
        
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Return true if we have any tick-based features that need processing
        // Currently no tick-based features, so return false
        return false;
    }
}
