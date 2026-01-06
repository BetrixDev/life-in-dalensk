package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.entity.TraderNPC;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntityTypes {

    public static EntityType<TraderNPC> TRADER_NPC;

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod entity types for " + LifeInDalensk.MOD_ID);

        TRADER_NPC = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(LifeInDalensk.MOD_ID, "trader_npc"),
                EntityType.Builder.create(TraderNPC::new, SpawnGroup.MISC)
                        .dimensions(0.6F, 1.8F)
                        .build());

        // Register entity attributes
        FabricDefaultAttributeRegistry.register(TRADER_NPC, TraderNPC.createTraderAttributes());
    }
}
