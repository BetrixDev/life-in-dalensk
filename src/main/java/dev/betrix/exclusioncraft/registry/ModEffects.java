package dev.betrix.exclusioncraft.registry;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.effects.BrokenLegEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ExclusionCraft.MODID);

    public static final RegistryObject<MobEffect> BROKEN_LEG =
            MOB_EFFECTS.register("broken_leg", BrokenLegEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
