package dev.betrix.exclusioncraft.blocks;

import dev.betrix.exclusioncraft.ExclusionCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ToolboxBlock extends SearchableContainerBlock {

    public static final ResourceLocation LOOT_TABLE_ID =
            new ResourceLocation(ExclusionCraft.MODID, "searchable/toolbox");

    public ToolboxBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(2.5f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(),
                LOOT_TABLE_ID,
                Component.translatable("container.exclusioncraft.toolbox"));
    }
}
