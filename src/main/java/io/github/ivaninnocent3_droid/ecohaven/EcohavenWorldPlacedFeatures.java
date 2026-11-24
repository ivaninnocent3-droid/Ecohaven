package io.github.ivaninnocent3_droid.ecohaven;

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
import net.minecraft.world.level.levelgen.placement.RarityFilter;

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

                    // Chance is checked once per chunk before the attempts.
                    RarityFilter.onAverageOnceEvery(tree.chancePerChunk()),

                    // One attempt for scattered trees; multiple for clumped trees.
                    CountPlacement.of(tree.attemptsPerChunk()),

                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                    BiomeFilter.biome(),

                    // Keep the ground-survival safeguard.
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

                // One feature execution per chunk.
                CountPlacement.of(1),

                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome()
        );
    }
}