package dev.betrix.exclusioncraft.registry;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.blocks.entity.SearchableContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ExclusionCraft.MODID);

    public static final RegistryObject<BlockEntityType<SearchableContainerBlockEntity>> SEARCHABLE_CONTAINER =
            BLOCK_ENTITIES.register("searchable_container",
                    () -> BlockEntityType.Builder.of(
                            SearchableContainerBlockEntity::new,
                            ModBlocks.TOOLBOX.get(),
                            ModBlocks.MED_CASE.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
