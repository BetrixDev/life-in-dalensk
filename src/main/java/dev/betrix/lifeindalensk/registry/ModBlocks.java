package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.block.MedCaseBlock;
import dev.betrix.lifeindalensk.block.ToolboxBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block TOOLBOX = register("toolbox", new ToolboxBlock(ToolboxBlock.createSettings()));
    public static final Block MED_CASE = register("med_case", new MedCaseBlock(MedCaseBlock.createSettings()));

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(LifeInDalensk.MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod blocks for " + LifeInDalensk.MOD_ID);

        // Add blocks to creative tabs
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(TOOLBOX);
            content.add(MED_CASE);
        });
    }
}
