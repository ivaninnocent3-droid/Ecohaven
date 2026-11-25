package io.github.ivaninnocent3_droid.ecohaven;

import io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation.VegetationFeature;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;

public class EcohavenWorldPlacedFeatures {

    public static void configure(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);

        for (EcohavenTreeProfiles.TreeSpawn tree :
                EcohavenTreeProfiles.allTreeSpawns()) {

            PlacementUtils.register(
                    context,
                    EcohavenTreeProfiles.placedFeatureKey(tree),
                    features.getOrThrow(
                            EcohavenTreeProfiles.featureKey(tree)
                    ),

                    net.minecraft.world.level.levelgen.placement.RarityFilter
                            .onAverageOnceEvery(tree.chancePerChunk()),

                    CountPlacement.of(tree.attemptsPerChunk()),

                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                    BiomeFilter.biome(),

                    BlockPredicateFilter.forPredicate(
                            BlockPredicate.wouldSurvive(tree.groundCheck())
                    )
            );
        }

        PlacementUtils.register(
                context,
                WoodlandSurfaceFeature.PLACED_FEATURE_KEY,
                features.getOrThrow(
                        WoodlandSurfaceFeature.FEATURE_KEY
                ),
                CountPlacement.of(1),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome()
        );
                PlacementUtils.register(
                context,
                VegetationFeature.PLACED_FEATURE_KEY,
                features.getOrThrow(
                        VegetationFeature.FEATURE_KEY
                ),
                CountPlacement.of(1),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome()
        );
        PlacementUtils.register(
        context,
        REDUCED_LEAF_LITTER_PLACED_FEATURE_KEY,
        features.getOrThrow(VANILLA_LEAF_LITTER_FEATURE_KEY),

        // Half the vanilla initial placement attempts.
        CountPlacement.of(1),

        InSquarePlacement.spread(),
        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,

        BiomeFilter.biome(),

        CountPlacement.of(32),
        OffsetPlacement.ofTriangle(7, 3),

        BlockPredicateFilter.forPredicate(
                BlockPredicate.allOf(
                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                        BlockPredicate.matchesBlocks(
                                Direction.DOWN,
                                Blocks.GRASS_BLOCK
                        )
                )
        )
);
    }
        public static final ResourceKey<PlacedFeature>
                REDUCED_LEAF_LITTER_PLACED_FEATURE_KEY =
                ResourceKey.create(
                        Registries.PLACED_FEATURE,
                        Ecohaven.id("reduced_leaf_litter")
                );

        private static final ResourceKey<Feature>
                VANILLA_LEAF_LITTER_FEATURE_KEY =
                ResourceKey.create(
                        Registries.FEATURE,
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "leaf_litter"
                        )
                );
}
