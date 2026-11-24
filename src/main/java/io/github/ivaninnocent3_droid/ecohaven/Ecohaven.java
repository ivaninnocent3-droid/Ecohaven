package io.github.ivaninnocent3_droid.ecohaven;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ecohaven implements ModInitializer {
    public static final String MOD_ID = "ecohaven";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String[] VANILLA_TREE_FEATURES = {
            "trees_plains",
            "trees_birch",
            "trees_birch_and_oak",
            "trees_water",
            "trees_taiga",
            "trees_grove",
            "trees_windswept_savanna",
            "trees_savanna",
            "trees_badlands",
            "trees_old_growth_taiga",
            "trees_dark_forest",
            "trees_jungle",
            "trees_mangrove",
            "trees_cherry",
            "trees_pale_garden",
            "oak",
            "fancy_oak",
            "oak_bees_0002",
            "fancy_oak_bees_0002",
            "birch",
            "birch_bees_0002",
            "super_birch_bees_0002",
            "pine",
            "spruce",
            "mega_spruce",
            "mega_pine",
            "jungle_tree",
            "mega_jungle_tree",
            "acacia",
            "dark_oak",
            "swamp_oak",
            "mangrove",
            "cherry",
            "pale_oak"
    };

    @Override
    public void onInitialize() {
        LOGGER.info("Ecohaven initialised.");

        WoodlandSurfaceFeature.register();

        for (EcohavenTreeProfiles.BiomeProfile profile :
                EcohavenTreeProfiles.profiles()) {

            var selector = BiomeSelectors.includeByKey(profile.biome());

            BiomeModifications.create(
                    id("tree_profile/" + profile.id())
            ).add(
                    ModificationPhase.REPLACEMENTS,
                    selector,
                    context -> {
                        var generation = context.getGenerationSettings();

                        // Add this biome's configured Ecohaven trees.
                        for (EcohavenTreeProfiles.TreeSpawn tree :
                                profile.trees()) {

                            generation.addFeature(
                                    GenerationStep.Decoration.VEGETAL_DECORATION,
                                    EcohavenTreeProfiles.placedFeatureKey(tree)
                            );
                        }
                        // Add woodland surface generation after the custom trees.
                        generation.addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                WoodlandSurfaceFeature.PLACED_FEATURE_KEY
                        );
                    }
            );
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}