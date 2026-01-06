package dev.betrix.lifeindalensk.block;

import com.mojang.serialization.MapCodec;
import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ToolboxBlock extends SearchableContainerBlock {

    public static final MapCodec<ToolboxBlock> CODEC = createCodec(ToolboxBlock::new);
    
    public static final RegistryKey<LootTable> LOOT_TABLE_KEY = RegistryKey.of(RegistryKeys.LOOT_TABLE,
            Identifier.of(LifeInDalensk.MOD_ID, "searchable/toolbox"));

    public ToolboxBlock(Settings settings) {
        super(settings, LOOT_TABLE_KEY, Text.translatable("container.lifeindalensk.toolbox"));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    public static AbstractBlock.Settings createSettings() {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.RED)
                .strength(2.5f)
                .sounds(BlockSoundGroup.METAL)
                .requiresTool();
    }
}
