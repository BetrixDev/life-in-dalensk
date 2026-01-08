package dev.betrix.lifeindalensk.mixin;

import dev.betrix.lifeindalensk.registry.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.monster.Zombie;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add a 10% chance of applying the Bleeding effect when a Zombie hits a player.
 * Bleeding deals 0.5 hearts every 5 seconds.
 */
@Mixin(Zombie.class)
public abstract class ZombieMixin extends LivingEntity {

    /**
     * Chance (0-1) of applying bleeding effect when hitting a player
     */
    private static final float BLEEDING_CHANCE = 0.1f;

    /**
     * Duration of bleeding effect in ticks (infinite duration)
     */
    private static final int BLEEDING_DURATION = StatusEffectInstance.INFINITE;

    protected ZombieMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    /**
     * After a zombie attacks, apply bleeding effect to the target with a 10% chance.
     */
    @Inject(method = "attack", at = @At("TAIL"))
    private void onAttack(LivingEntity target, CallbackInfo ci) {
        // Only apply bleeding to players
        if (!(target instanceof PlayerEntity)) {
            return;
        }

        // 10% chance to apply bleeding
        if (this.getRandom().nextFloat() < BLEEDING_CHANCE) {
            target.addStatusEffect(new StatusEffectInstance(
                    ModEffects.BLEEDING,
                    BLEEDING_DURATION,
                    0,
                    false,
                    true,
                    true));
        }
    }
}
