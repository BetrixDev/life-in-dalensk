package dev.betrix.exclusioncraft.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BrokenLegEffect extends MobEffect {

    private static final String SLOWDOWN_UUID = "7107DE5E-7CE8-4030-940E-514C1F160890";

    public BrokenLegEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
        addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SLOWDOWN_UUID,
                -0.5,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}
