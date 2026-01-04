package dev.betrix.exclusioncraft.blocks;

import dev.betrix.exclusioncraft.blocks.entity.SearchableContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SearchableContainerBlock extends BaseEntityBlock {

    private final ResourceLocation lootTableId;
    private final Component containerName;

    public SearchableContainerBlock(Properties properties, ResourceLocation lootTableId, Component containerName) {
        super(properties);
        this.lootTableId = lootTableId;
        this.containerName = containerName;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new SearchableContainerBlockEntity(pos, state);
    }

    @Override
    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Nonnull
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
            @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SearchableContainerBlockEntity container)) {
            return InteractionResult.PASS;
        }

        if (!container.isSearched()) {
            generateLoot(level, pos, container);
            container.setSearched(true);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                @Nonnull
                public Component getDisplayName() {
                    return containerName;
                }

                @Override
                @Nonnull
                public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory playerInventory,
                        @Nonnull Player player) {
                    return new dev.betrix.exclusioncraft.inventory.SearchableContainerMenu(containerId, playerInventory,
                            container);
                }
            }, pos);
        }

        return InteractionResult.CONSUME;
    }

    private void generateLoot(Level level, BlockPos pos, SearchableContainerBlockEntity container) {
        if (level.getServer() == null)
            return;

        LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);

        LootParams.Builder paramsBuilder = new LootParams.Builder(level.getServer().overworld())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));

        LootParams params = paramsBuilder.create(LootContextParamSets.CHEST);
        lootTable.fill(container, params, level.random.nextLong());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
            @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SearchableContainerBlockEntity container) {
                Containers.dropContents(level, pos, container);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    public ResourceLocation getLootTableId() {
        return lootTableId;
    }
}
