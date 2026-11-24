
package io.github.ivaninnocent3_droid.ecohaven;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.util.valueproviders.ConstantInt;

public class EcohavenWorldFeatures {

    public static final ResourceKey<Feature> TALL_OAK_TREE =
            ResourceKey.create(
                    Registries.FEATURE,
                    Ecohaven.id("tall_oak_tree")
            );

    public static void configure(BootstrapContext<Feature> context) {

        TreeFeature tallOak = new TreeFeature.Builder(
                Holder.direct(BlockStateProvider.of(Blocks.OAK_LOG)),
                new StraightTrunkPlacer(7, 2, 1),

                Holder.direct(BlockStateProvider.of(Blocks.OAK_LEAVES)),
                new BlobFoliagePlacer(
                        ConstantInt.of(2),
                        ConstantInt.of(0),
                        3
                ),

                Optional.empty(),

                new TwoLayersFeatureSize(1, 0, 1),

                Holder.direct(BlockStateProvider.of(Blocks.DIRT))
        ).build();

        context.register(TALL_OAK_TREE, tallOak);
    }
}
