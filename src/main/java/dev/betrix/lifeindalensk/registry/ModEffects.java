package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.effect.PainkillerEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {

    /**
     * Identifier for the broken leg slowdown attribute modifier.
     * Used by the mixin to identify and conditionally remove this modifier.
     */
    public static final Identifier BROKEN_LEG_SLOWDOWN_ID = Identifier.of(LifeInDalensk.MOD_ID, "broken_leg_slowdown");

    public static final RegistryEntry<StatusEffect> BROKEN_LEG = register("broken_leg", 
            createBrokenLegEffect());

    public static final RegistryEntry<StatusEffect> PAINKILLER = register("painkiller",
            new PainkillerEffect());

    private static StatusEffect createBrokenLegEffect() {
        return new StatusEffect(StatusEffectCategory.HARMFUL, 0x8B4513) {
        }.addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                BROKEN_LEG_SLOWDOWN_ID,
                -0.5,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(LifeInDalensk.MOD_ID, name), effect);
    }

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod effects for " + LifeInDalensk.MOD_ID);
    }
}
