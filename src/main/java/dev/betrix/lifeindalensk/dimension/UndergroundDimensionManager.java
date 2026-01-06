package dev.betrix.lifeindalensk.dimension;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UndergroundDimensionManager {
    public static final RegistryKey<World> UNDERGROUND_DIMENSION = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of(LifeInDalensk.MOD_ID, "underground"));

    public static final BlockPos SPAWN_POS = new BlockPos(28, 71, 51);

    public static void initializeDimension(ServerWorld world) {
        if (!world.getRegistryKey().equals(UNDERGROUND_DIMENSION))
            return;

        UndergroundDimensionState state = UndergroundDimensionState.getOrCreate(world);
        if (state.isInitialized())
            return;

        placeStructure(world);
        state.markInitialized();
    }

    public static void teleportToSpawn(ServerPlayerEntity player) {
        ServerWorld targetWorld = Objects.requireNonNull(player.getServer()).getWorld(UNDERGROUND_DIMENSION);
        if (targetWorld == null)
            return;

        initializeDimension(targetWorld);

        player.teleport(
                targetWorld,
                SPAWN_POS.getX() + 0.5,
                SPAWN_POS.getY(),
                SPAWN_POS.getZ() + 0.5,
                Set.of(),
                -180,
                0);
    }

    private static void placeStructure(ServerWorld world) {
        StructureTemplateManager manager = world.getStructureTemplateManager();
        Optional<StructureTemplate> template = manager.getTemplate(
                Identifier.of(LifeInDalensk.MOD_ID, "underground"));

        if (template.isPresent()) {
            BlockPos structurePos = new BlockPos(0, 64, 0);
            StructurePlacementData data = new StructurePlacementData()
                    .setMirror(BlockMirror.NONE)
                    .setRotation(BlockRotation.NONE)
                    .setIgnoreEntities(false);

            template.get().place(world, structurePos, structurePos, data, world.random, 2);
        } else {
            placeFallbackPlatform(world);
        }
    }

    private static void placeFallbackPlatform(ServerWorld world) {
        BlockState stone = net.minecraft.block.Blocks.STONE.getDefaultState();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                world.setBlockState(new BlockPos(x, 64, z), stone);
            }
        }
        System.out.println("[YourMod] Fallback platform placed.");
    }
}
