package dev.betrix.lifeindalensk.mixin;

import dev.betrix.lifeindalensk.effect.PainkillerEffect;
import dev.betrix.lifeindalensk.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle painkiller effect interactions with attribute modifiers.
 * When a player has the painkiller effect, certain negative attribute modifiers
 * (like the broken leg slowdown) are removed.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static final float BROKEN_LEG_JUMP_DAMAGE = 1.0f;

    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect);

    @Shadow
    public abstract StatusEffectInstance getStatusEffect(RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect);

    @Shadow
    public abstract EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute);

    /**
     * After status effect attributes are updated, check if painkiller should negate
     * the broken leg slowdown modifier.
     */
    @Inject(method = "updateAttributes", at = @At("TAIL"))
    private void onUpdateAttributes(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        
        // Check if entity has painkiller with the broken leg slowdown negation feature
        if (PainkillerEffect.hasFeature(self, ModEffects.PAINKILLER, PainkillerEffect.Feature.NEGATE_BROKEN_LEG_SLOWDOWN)) {
            // Remove the broken leg slowdown modifier if present
            EntityAttributeInstance speedAttribute = self.getAttributeInstance(
                    net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
            
            if (speedAttribute != null) {
                EntityAttributeModifier modifier = speedAttribute.getModifier(ModEffects.BROKEN_LEG_SLOWDOWN_ID);
                if (modifier != null) {
                    speedAttribute.removeModifier(modifier);
                }
            }
        }
    }

    /**
     * When a status effect is added, check if we need to handle painkiller interactions.
     * This handles the case where painkiller is added while already having broken leg.
     */
    @Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
    private void onStatusEffectApplied(StatusEffectInstance effect, net.minecraft.entity.Entity source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        
        // If painkiller was just applied and we have broken leg, remove the slowdown
        if (effect.getEffectType() == ModEffects.PAINKILLER) {
            if (PainkillerEffect.hasFeature(self, ModEffects.PAINKILLER, PainkillerEffect.Feature.NEGATE_BROKEN_LEG_SLOWDOWN)
                    && hasStatusEffect(ModEffects.BROKEN_LEG)) {
                
                EntityAttributeInstance speedAttribute = self.getAttributeInstance(
                        net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
                
                if (speedAttribute != null) {
                    EntityAttributeModifier modifier = speedAttribute.getModifier(ModEffects.BROKEN_LEG_SLOWDOWN_ID);
                    if (modifier != null) {
                        speedAttribute.removeModifier(modifier);
                    }
                }
            }
        }
    }

    /**
     * When painkiller wears off, reapply the broken leg slowdown if the player still has broken leg.
     */
    @Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        
        // If painkiller was removed and we still have broken leg, reapply the slowdown
        if (effect.getEffectType() == ModEffects.PAINKILLER && hasStatusEffect(ModEffects.BROKEN_LEG)) {
            EntityAttributeInstance speedAttribute = self.getAttributeInstance(
                    net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
            
            if (speedAttribute != null && speedAttribute.getModifier(ModEffects.BROKEN_LEG_SLOWDOWN_ID) == null) {
                // Reapply the broken leg slowdown modifier
                speedAttribute.addTemporaryModifier(new EntityAttributeModifier(
                        ModEffects.BROKEN_LEG_SLOWDOWN_ID,
                        -0.5,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.hasStatusEffect(ModEffects.BROKEN_LEG)) {
            entity.damage(entity.getDamageSources().generic(), BROKEN_LEG_JUMP_DAMAGE);
        }
    }
}
