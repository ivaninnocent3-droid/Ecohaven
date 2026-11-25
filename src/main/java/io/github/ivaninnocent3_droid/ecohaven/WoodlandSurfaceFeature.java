package io.github.ivaninnocent3_droid.ecohaven;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

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

    private static final float PATCHED_GRASS_CHANCE = 0.85F;
    private static final int COVER_SCAN_HEIGHT = 16;

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
        Block patchedGrass =
                BuiltInRegistries.BLOCK.getValue(PATCHED_GRASS_ID);

        if (patchedGrass == Blocks.AIR) {
            Ecohaven.LOGGER.error(
                    "Could not find ecology:patched_grass. " +
                    "Check the ecology dependency."
            );
            return false;
        }

        int startX = origin.getX() & ~15;
        int startZ = origin.getZ() & ~15;

        boolean changed = false;

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {

                int surfaceY = level.getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG,
                        x,
                        z
                ) - 1;

                BlockPos groundPos =
                        new BlockPos(x, surfaceY, z);

                BlockState groundState =
                        level.getBlockState(groundPos);

                if (!groundState.is(Blocks.GRASS_BLOCK)
                        && !groundState.is(Blocks.DIRT)) {
                    continue;
                }

                boolean covered =
                        hasOverheadCover(level, groundPos);

                BlockState replacement;

                if (covered) {
                    if (random.nextFloat() < PATCHED_GRASS_CHANCE) {
                        replacement =
                                patchedGrass.defaultBlockState();
                    } else {
                        replacement =
                                Blocks.DIRT.defaultBlockState();
                    }
                } else {
                    replacement =
                            Blocks.GRASS_BLOCK.defaultBlockState();
                }

                if (!groundState.equals(replacement)) {
                    level.setBlock(
                            groundPos,
                            replacement,
                            2
                    );

                    changed = true;
                }
            }
        }

        return changed;
    }

    private static boolean hasOverheadCover(
            WorldGenLevel level,
            BlockPos groundPos
    ) {
        BlockPos.MutableBlockPos checkPos =
                groundPos.mutable();

        for (int y = 1; y <= COVER_SCAN_HEIGHT; y++) {

            checkPos.set(
                    groundPos.getX(),
                    groundPos.getY() + y,
                    groundPos.getZ()
            );

            BlockState state =
                    level.getBlockState(checkPos);

            if (state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.getLightDampening() >= 15) {
            return true;
        }
        }

        return false;
    }
}