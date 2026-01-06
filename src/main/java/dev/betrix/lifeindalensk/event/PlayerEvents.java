package dev.betrix.lifeindalensk.event;

import dev.betrix.lifeindalensk.registry.ModEffects;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEvents {

    private static final float MIN_FALL_DISTANCE = 5.0f;
    private static final float MAX_FALL_DISTANCE = 10.0f;
    private static final float MIN_BREAK_CHANCE = 0.1f;
    private static final int MIN_SPRINT_DAMAGE_TICKS = 20;
    private static final int MAX_SPRINT_DAMAGE_TICKS = 60;

    private static final Map<UUID, Integer> sprintDamageTimers = new HashMap<>();
    private static final Map<UUID, Float> lastFallDistance = new HashMap<>();

    public static void register() {
        // Handle fall damage -> broken leg chance
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof PlayerEntity player))
                return;

            if (source.isOf(net.minecraft.entity.damage.DamageTypes.FALL)) {
                float distance = lastFallDistance.getOrDefault(player.getUuid(), 0f);
                lastFallDistance.remove(player.getUuid());

                if (distance < MIN_FALL_DISTANCE) {
                    return;
                }

                float chance;
                if (distance >= MAX_FALL_DISTANCE) {
                    chance = 1.0f;
                } else {
                    float progress = (distance - MIN_FALL_DISTANCE) / (MAX_FALL_DISTANCE - MIN_FALL_DISTANCE);
                    chance = MIN_BREAK_CHANCE + progress * (1.0f - MIN_BREAK_CHANCE);
                }

                if (player.getRandom().nextFloat() < chance) {
                    player.addStatusEffect(new StatusEffectInstance(
                            ModEffects.BROKEN_LEG,
                            StatusEffectInstance.INFINITE,
                            0,
                            false,
                            true,
                            true));
                }
            }
        });

        // Track fall distance
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof PlayerEntity player && source.isOf(net.minecraft.entity.damage.DamageTypes.FALL)) {
                lastFallDistance.put(player.getUuid(), player.fallDistance);
            }
            return true;
        });
    }

    public static void tickPlayer(PlayerEntity player) {
        UUID playerId = player.getUuid();

        if (player.hasStatusEffect(ModEffects.BROKEN_LEG) && player.isSprinting()) {
            int timer = sprintDamageTimers.getOrDefault(playerId, 0);

            if (timer <= 0) {
                player.damage(player.getDamageSources().generic(), 1.0f);
                int nextDamageIn = MIN_SPRINT_DAMAGE_TICKS +
                        player.getRandom().nextInt(MAX_SPRINT_DAMAGE_TICKS - MIN_SPRINT_DAMAGE_TICKS + 1);
                sprintDamageTimers.put(playerId, nextDamageIn);
            } else {
                sprintDamageTimers.put(playerId, timer - 1);
            }
        } else {
            sprintDamageTimers.remove(playerId);
        }
    }

    public static void onPlayerJump(PlayerEntity player) {
        if (player.hasStatusEffect(ModEffects.BROKEN_LEG)) {
            player.damage(player.getDamageSources().generic(), 1.0f);
        }
    }
}
