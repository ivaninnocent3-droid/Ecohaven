
package io.github.ivaninnocent3_droid.ecohaven;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class WoodlandSurfaceFeature implements Feature {

    private static final Identifier PATCHED_GRASS_ID =
            Identifier.fromNamespaceAndPath("ecology", "patched_grass");

    public static final MapCodec<WoodlandSurfaceFeature> CODEC =
            MapCodec.unit(new WoodlandSurfaceFeature());

    public static final ResourceKey<Feature> FEATURE_KEY =
            ResourceKey.create(
                    Registries.FEATURE,
                    Ecohaven.id("woodland_surface")
            );

    public static final ResourceKey<PlacedFeature> PLACED_FEATURE_KEY =
            ResourceKey.create(
                    Registries.PLACED_FEATURE,
                    Ecohaven.id("woodland_surface")
            );

    private static final float MAX_PATCHED_GRASS_CHANCE = 0.85F;

    private static final int COVER_SCAN_HEIGHT = 16;
    private static final int COVER_INFLUENCE_RADIUS = 6;

    public static void register() {
        Registry.register(
                BuiltInRegistries.FEATURE_TYPE,
                Ecohaven.id("woodland_surface"),
                CODEC
        );
    }

    @Override
    public MapCodec<WoodlandSurfaceFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
            WorldGenLevel level,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BlockPos origin
    ) {
        Block patchedGrass = BuiltInRegistries.BLOCK.getValue(PATCHED_GRASS_ID);

        if (patchedGrass == Blocks.AIR) {
            Ecohaven.LOGGER.error(
                    "Could not find ecology:patched_grass. Check the ecology dependency."
            );
            return false;
        }

        int startX = origin.getX() & ~15;
        int startZ = origin.getZ() & ~15;

        int[][] surfaceHeights = new int[16][16];
        boolean[][] coveredColumns = new boolean[16][16];

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        // PASS 1: Find the ground height and overhead cover.
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = startX + x;
                int worldZ = startZ + z;

                int surfaceY = level.getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG,
                        worldX,
                        worldZ
                ) - 1;

                surfaceHeights[x][z] = surfaceY;

                BlockPos groundPos = new BlockPos(
                        worldX,
                        surfaceY,
                        worldZ
                );

                BlockState groundState = level.getBlockState(groundPos);

                if (!groundState.is(Blocks.GRASS_BLOCK)
                        && !groundState.is(Blocks.DIRT)) {
                    continue;
                }

                coveredColumns[x][z] =
                        hasOverheadCover(level, groundPos, checkPos);
            }
        }

        boolean changed = false;

        // PASS 2: Generate patched grass based on nearby cover.
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = startX + x;
                int worldZ = startZ + z;
                int surfaceY = surfaceHeights[x][z];

                BlockPos groundPos = new BlockPos(
                        worldX,
                        surfaceY,
                        worldZ
                );

                BlockState groundState = level.getBlockState(groundPos);

                if (!groundState.is(Blocks.GRASS_BLOCK)
                        && !groundState.is(Blocks.DIRT)) {
                    continue;
                }

                float chance = getPatchedGrassChance(
                        coveredColumns,
                        x,
                        z
                );

                BlockState replacement;

                if (coveredColumns[x][z]) {
                    // Directly beneath cover: high patched grass density.
                    if (random.nextFloat() < MAX_PATCHED_GRASS_CHANCE) {
                        replacement = patchedGrass.defaultBlockState();
                    } else {
                        replacement = Blocks.DIRT.defaultBlockState();
                    }
                } else if (random.nextFloat() < chance) {
                    // Near cover: progressively lower patched grass density.
                    replacement = patchedGrass.defaultBlockState();
                } else {
                    replacement = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                if (!groundState.equals(replacement)) {
                    level.setBlock(groundPos, replacement, 2);
                    changed = true;
                }
            }
        }

        return changed;
    }

    private static float getPatchedGrassChance(
            boolean[][] covered,
            int x,
            int z
    ) {
        int radius = COVER_INFLUENCE_RADIUS;
        int radiusSquared = radius * radius;

        int nearestDistanceSquared = Integer.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                int distanceSquared = dx * dx + dz * dz;

                if (distanceSquared > radiusSquared
                        || distanceSquared >= nearestDistanceSquared) {
                    continue;
                }

                int checkX = x + dx;
                int checkZ = z + dz;

                if (checkX < 0 || checkX >= 16
                        || checkZ < 0 || checkZ >= 16) {
                    continue;
                }

                if (covered[checkX][checkZ]) {
                    nearestDistanceSquared = distanceSquared;
                }
            }
        }

        if (nearestDistanceSquared == Integer.MAX_VALUE) {
            return 0.0F;
        }

        float distance = (float) Math.sqrt(nearestDistanceSquared);

        float falloff = 1.0F
                - distance / (COVER_INFLUENCE_RADIUS + 1.0F);

        return MAX_PATCHED_GRASS_CHANCE * falloff;
    }

    private static boolean hasOverheadCover(
            WorldGenLevel level,
            BlockPos groundPos,
            BlockPos.MutableBlockPos checkPos
    ) {
        for (int y = 1; y <= COVER_SCAN_HEIGHT; y++) {

            checkPos.set(
                    groundPos.getX(),
                    groundPos.getY() + y,
                    groundPos.getZ()
            );

            BlockState state = level.getBlockState(checkPos);

            if (state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.LOGS)
                    || state.isSolid()) {
                return true;
            }
        }

        return false;
    }
}
