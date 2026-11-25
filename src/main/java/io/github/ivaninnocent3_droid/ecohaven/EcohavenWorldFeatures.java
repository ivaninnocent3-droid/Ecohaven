package io.github.ivaninnocent3_droid.ecohaven;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation.VegetationFeature;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TemplateFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class EcohavenWorldFeatures {

    public static void configure(BootstrapContext<Feature> context) {
        StructureProcessorList processors = new StructureProcessorList(
                List.of(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
        );

        Map<String, List<EcohavenTreeTemplateCatalog.TreeTemplate>> catalog =
                EcohavenTreeTemplateCatalog.discover();

        for (EcohavenTreeProfiles.TreeSpawn tree :
                EcohavenTreeProfiles.allTreeSpawns()) {

            WeightedList.Builder<TemplateFeature.TemplateEntry> templates =
                    new WeightedList.Builder<>();

            for (EcohavenTreeProfiles.FamilySpawn family :
                    tree.families()) {

                List<EcohavenTreeTemplateCatalog.TreeTemplate> variants =
                        catalog.get(family.family());

                if (variants == null || variants.isEmpty()) {
                    throw new IllegalStateException(
                            "Tree family '" + family.family()
                                    + "' is configured for spawn '"
                                    + tree.id()
                                    + "' but no matching NBT templates were discovered."
                    );
                }

                Set<String> discoveredVariants = new HashSet<>();

                for (EcohavenTreeTemplateCatalog.TreeTemplate variant :
                        variants) {

                    discoveredVariants.add(variant.variant());

                    int variantWeight = family.variantWeights()
                            .getOrDefault(variant.variant(), 1);

                    int finalWeight = Math.multiplyExact(
                            family.weight(),
                            variantWeight
                    );

                    templates.add(
                            TemplateFeature.TemplateEntry.of(
                                    variant.template()
                            ),
                            finalWeight
                    );
                }

                for (String configuredVariant :
                        family.variantWeights().keySet()) {

                    if (!discoveredVariants.contains(configuredVariant)) {
                        throw new IllegalStateException(
                                "Tree variant '" + family.family()
                                        + "_" + configuredVariant
                                        + "' is configured for spawn '"
                                        + tree.id()
                                        + "' but no matching NBT template exists."
                        );
                    }
                }
            }

            context.register(
                    EcohavenTreeProfiles.featureKey(tree),
                    new TemplateFeature(
                            templates.build(),
                            java.util.Optional.of(Holder.direct(processors))
                    )
            );
        }
        context.register(
                WoodlandSurfaceFeature.FEATURE_KEY,
                new WoodlandSurfaceFeature()
        );

        context.register(
                VegetationFeature.FEATURE_KEY,
                new VegetationFeature()
        );
    }
}
