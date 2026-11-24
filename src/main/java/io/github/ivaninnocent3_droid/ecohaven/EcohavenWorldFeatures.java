package io.github.ivaninnocent3_droid.ecohaven;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TemplateFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class EcohavenWorldFeatures {

    public static void configure(BootstrapContext<Feature> context) {
        StructureProcessorList processors = new StructureProcessorList(
                List.of(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
        );

        for (EcohavenTreeProfiles.TreeSpawn tree :
                EcohavenTreeProfiles.allTreeSpawns()) {

            TemplateFeature feature = TemplateFeature.simple(
                    Ecohaven.id(tree.template()),
                    Holder.direct(processors)
            );

            context.register(
                    EcohavenTreeProfiles.featureKey(tree),
                    feature
            );
        }
        context.register(
                WoodlandSurfaceFeature.FEATURE_KEY,
                new WoodlandSurfaceFeature()
        );
    }
}