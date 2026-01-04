package dev.betrix.exclusioncraft.registry;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.blocks.MedCaseBlock;
import dev.betrix.exclusioncraft.blocks.ToolboxBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ExclusionCraft.MODID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExclusionCraft.MODID);

    public static final RegistryObject<Block> TOOLBOX =
            registerBlock("toolbox", ToolboxBlock::new);

    public static final RegistryObject<Block> MED_CASE =
            registerBlock("med_case", MedCaseBlock::new);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        BLOCK_ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
