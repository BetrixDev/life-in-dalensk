package dev.betrix.lifeindalensk.block;

import com.mojang.serialization.MapCodec;
import dev.betrix.lifeindalensk.block.entity.SearchableContainerBlockEntity;
import dev.betrix.lifeindalensk.inventory.SearchableContainerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class SearchableContainerBlock extends BlockWithEntity {

    private final RegistryKey<LootTable> lootTableKey;
    private final Text containerName;

    protected SearchableContainerBlock(Settings settings, RegistryKey<LootTable> lootTableKey, Text containerName) {
        super(settings);
        this.lootTableKey = lootTableKey;
        this.containerName = containerName;
    }

    @Override
    protected abstract MapCodec<? extends BlockWithEntity> getCodec();

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SearchableContainerBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof SearchableContainerBlockEntity container)) {
            return ActionResult.PASS;
        }

        if (!container.isSearched()) {
            generateLoot(world, pos, container);
            container.setSearched(true);
        }

        player.openHandledScreen(createScreenHandlerFactory(state, world, pos, container));
        return ActionResult.CONSUME;
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos,
            SearchableContainerBlockEntity container) {
        return new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, player) -> new SearchableContainerScreenHandler(syncId, playerInventory,
                        container, ScreenHandlerContext.create(world, pos)),
                containerName);
    }

    private void generateLoot(World world, BlockPos pos, SearchableContainerBlockEntity container) {
        if (!(world instanceof ServerWorld serverWorld))
            return;

        LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(lootTableKey);

        LootContextParameterSet.Builder paramsBuilder = new LootContextParameterSet.Builder(serverWorld)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos));

        lootTable.supplyInventory(container, paramsBuilder.build(lootTable.getType()), world.random.nextLong());
    }

    /**
     * Called when the block is about to be replaced.
     * In Fabric 1.21+, we handle item dropping via BlockEntity's onBroken or tick logic,
     * or through BlockWithEntity's built-in mechanisms.
     * Note: onStateReplaced is final in 1.21, use alternative approach.
     */
    public void dropContainerContents(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SearchableContainerBlockEntity container) {
            ItemScatterer.spawn(world, pos, container);
        }
    }

    /**
     * Get the custom loot table key for this searchable container.
     * Named differently from AbstractBlock's getLootTableKey() which is final.
     */
    public RegistryKey<LootTable> getSearchableLootTableKey() {
        return lootTableKey;
    }
}
