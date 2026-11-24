
package io.github.ivaninnocent3_droid.ecohaven;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class EcohavenWorldPlacedFeatures {

    public static final ResourceKey<PlacedFeature> TALL_OAK_TREE =
            ResourceKey.create(
                    Registries.PLACED_FEATURE,
                    Ecohaven.id("tall_oak_tree")
            );

    public static void configure(BootstrapContext<PlacedFeature> context) {

        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);

        PlacementUtils.register(
                context,
                TALL_OAK_TREE,
                features.getOrThrow(EcohavenWorldFeatures.TALL_OAK_TREE),
                RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome(),
                BlockPredicateFilter.forPredicate(
                        BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING)
                )
        );
    }
}
