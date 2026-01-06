package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.block.entity.SearchableContainerBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<SearchableContainerBlockEntity> SEARCHABLE_CONTAINER;

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod block entities for " + LifeInDalensk.MOD_ID);

        SEARCHABLE_CONTAINER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(LifeInDalensk.MOD_ID, "searchable_container"),
                BlockEntityType.Builder.create(
                        SearchableContainerBlockEntity::new,
                        ModBlocks.TOOLBOX,
                        ModBlocks.MED_CASE
                ).build());
    }
}
