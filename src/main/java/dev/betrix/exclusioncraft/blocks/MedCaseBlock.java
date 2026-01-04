package dev.betrix.exclusioncraft.blocks;

import dev.betrix.exclusioncraft.ExclusionCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MedCaseBlock extends SearchableContainerBlock {

    public static final ResourceLocation LOOT_TABLE_ID = new ResourceLocation(ExclusionCraft.MODID,
            "searchable/med_case");

    public MedCaseBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(2.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(),
                LOOT_TABLE_ID,
                Component.translatable("container.exclusioncraft.med_case"));
    }
}
