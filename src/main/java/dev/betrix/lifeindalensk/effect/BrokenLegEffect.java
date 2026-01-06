package dev.betrix.lifeindalensk.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

public class BrokenLegEffect extends StatusEffect {

    public BrokenLegEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B4513);
    }

    public static BrokenLegEffect create() {
        BrokenLegEffect effect = new BrokenLegEffect();
        effect.addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Identifier.of("lifeindalensk", "broken_leg_slowdown"),
                -0.5,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        return effect;
    }
}
