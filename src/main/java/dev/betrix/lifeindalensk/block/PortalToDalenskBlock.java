package dev.betrix.lifeindalensk.block;

import com.mojang.serialization.MapCodec;
import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Set;

/**
 * A portal block that teleports players to a random safe location in the Overworld
 * within 100 blocks of spawn (0, 0).
 */
public class PortalToDalenskBlock extends Block {

    public static final MapCodec<PortalToDalenskBlock> CODEC = createCodec(PortalToDalenskBlock::new);

    private static final int SEARCH_RADIUS = 100;
    private static final int MAX_ATTEMPTS = 50;

    public PortalToDalenskBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    public static AbstractBlock.Settings createSettings() {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.PURPLE)
                .strength(-1.0f, 3600000.0f) // Unbreakable like bedrock
                .sounds(BlockSoundGroup.GLASS)
                .luminance(state -> 11) // Emits light
                .noCollision(); // Players can walk into it
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient) {
            return;
        }

        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        // Prevent repeated teleportation by checking if player just teleported
        if (player.hasPortalCooldown()) {
            return;
        }

        ServerWorld overworld = player.getServer().getWorld(
                RegistryKey.of(RegistryKeys.WORLD, World.OVERWORLD.getValue()));

        if (overworld == null) {
            LifeInDalensk.LOGGER.warn("Could not find Overworld dimension for portal teleport");
            return;
        }

        BlockPos safePos = findSafeSpawnLocation(overworld);
        if (safePos == null) {
            LifeInDalensk.LOGGER.warn("Could not find safe spawn location in Overworld");
            return;
        }

        // Set portal cooldown to prevent immediate re-teleportation
        player.resetPortalCooldown();

        // Teleport player to the safe location
        player.teleport(
                overworld,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                Set.of(),
                player.getYaw(),
                player.getPitch()
        );

        LifeInDalensk.LOGGER.info("Teleported player {} to Overworld at {}",
                player.getName().getString(), safePos);
    }

    /**
     * Finds a random safe spawn location within SEARCH_RADIUS blocks of (0, 0).
     * A safe location is a solid block with at least 2 air blocks above it.
     *
     * @param world The server world to search in
     * @return A safe BlockPos to spawn the player, or null if none found
     */
    private BlockPos findSafeSpawnLocation(ServerWorld world) {
        Random random = world.getRandom();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Generate random X and Z within the search radius
            int x = random.nextBetween(-SEARCH_RADIUS, SEARCH_RADIUS);
            int z = random.nextBetween(-SEARCH_RADIUS, SEARCH_RADIUS);

            // Find the top solid block at this position
            BlockPos safePos = findSafeYPosition(world, x, z);
            if (safePos != null) {
                return safePos;
            }
        }

        // Fallback: try spawn point (0, 0) with safe Y
        BlockPos fallback = findSafeYPosition(world, 0, 0);
        if (fallback != null) {
            return fallback;
        }

        // Last resort: return world spawn
        return world.getSpawnPos();
    }

    /**
     * Finds a safe Y position at the given X, Z coordinates.
     * Scans from top to bottom to find a solid block with 2 air blocks above.
     *
     * @param world The server world
     * @param x     X coordinate
     * @param z     Z coordinate
     * @return A safe BlockPos or null if none found
     */
    private BlockPos findSafeYPosition(ServerWorld world, int x, int z) {
        int minY = world.getBottomY();
        int maxY = world.getTopY();

        // Scan from top to bottom
        for (int y = maxY - 2; y >= minY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockPos feetPos = groundPos.up();
            BlockPos headPos = feetPos.up();

            BlockState groundState = world.getBlockState(groundPos);
            BlockState feetState = world.getBlockState(feetPos);
            BlockState headState = world.getBlockState(headPos);

            // Check if ground is solid and feet/head positions are air
            if (isSolidGround(groundState) && isPassable(feetState) && isPassable(headState)) {
                // Return the position where the player's feet will be
                return feetPos;
            }
        }

        return null;
    }

    /**
     * Checks if a block state represents solid ground suitable for standing on.
     */
    private boolean isSolidGround(BlockState state) {
        // Check if the block is solid and not a fluid
        return state.isSolid() && !state.isLiquid();
    }

    /**
     * Checks if a block state is air or can be passed through safely.
     */
    private boolean isPassable(BlockState state) {
        return state.isAir() || state.isReplaceable();
    }
}
