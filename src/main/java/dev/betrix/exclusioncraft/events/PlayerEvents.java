package dev.betrix.exclusioncraft.events;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {

    private static final float MIN_FALL_DISTANCE = 5.0f;
    private static final float MAX_FALL_DISTANCE = 10.0f;
    private static final float MIN_BREAK_CHANCE = 0.1f;
    private static final int MIN_SPRINT_DAMAGE_TICKS = 20;
    private static final int MAX_SPRINT_DAMAGE_TICKS = 60;

    private static final Map<UUID, Integer> sprintDamageTimers = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        float distance = event.getDistance();
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
            player.addEffect(new MobEffectInstance(
                    ModEffects.BROKEN_LEG.get(),
                    MobEffectInstance.INFINITE_DURATION,
                    0,
                    false,
                    true,
                    true));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();

        if (player.hasEffect(ModEffects.BROKEN_LEG.get()) && player.isSprinting()) {
            int timer = sprintDamageTimers.getOrDefault(playerId, 0);

            if (timer <= 0) {
                player.hurt(player.damageSources().generic(), 1.0f);
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

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.hasEffect(ModEffects.BROKEN_LEG.get())) {
            player.hurt(player.damageSources().generic(), 1.0f);
        }
    }
}
