package io.github.ivaninnocent3_droid.ecohaven;

import io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation.VegetationGrowth;

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

import io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation.VegetationFeature;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.biome.Biome;


public class Ecohaven implements ModInitializer {
    public static final String MOD_ID = "ecohaven";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



    @Override
    public void onInitialize() {
        LOGGER.info("Ecohaven initialised.");

        ServerLifecycleEvents.SERVER_STARTED.register(
                Ecohaven::logBiomeFeatures
        );

        WoodlandSurfaceFeature.register();
        VegetationFeature.register();
        VegetationGrowth.register();

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

                        // Remove only the vanilla tree features assigned to this biome.
                        for (String vanillaTreeFeature :
                                profile.vanillaTreeFeaturesToRemove()) {

                            generation.removeFeature(
                                    ResourceKey.create(
                                            Registries.PLACED_FEATURE,
                                            Identifier.fromNamespaceAndPath(
                                                    "minecraft",
                                                    vanillaTreeFeature
                                            )
                                    )
                            );
                        }

                        // Add this biome's custom Ecohaven trees.
                        for (EcohavenTreeProfiles.TreeSpawn tree :
                                profile.trees()) {

                            generation.addFeature(
                                    GenerationStep.Decoration.VEGETAL_DECORATION,
                                    EcohavenTreeProfiles.placedFeatureKey(tree)
                            );
                        }

                        // Add woodland surface generation.
                        generation.addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                WoodlandSurfaceFeature.PLACED_FEATURE_KEY
                        );
                        generation.addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                VegetationFeature.PLACED_FEATURE_KEY
                        );
                        if (profile.id().equals("forest")) {
                            generation.addFeature(
                                    GenerationStep.Decoration.VEGETAL_DECORATION,
                                    EcohavenWorldPlacedFeatures
                                            .REDUCED_LEAF_LITTER_PLACED_FEATURE_KEY
                            );
                        }
                    }
            );
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    
    private static void logBiomeFeatures(
            net.minecraft.server.MinecraftServer server
    ) {
        var biomeRegistry = server.registryAccess()
                .lookupOrThrow(Registries.BIOME);

        LOGGER.info("========== ECohaven BIOME FEATURE REPORT ==========");

        for (var biomeId : biomeRegistry.keySet()) {
            var biomeEntry = biomeRegistry.get(biomeId);

            if (biomeEntry.isEmpty()) {
                continue;
            }

            Biome biome = biomeEntry.get().value();

            if (biome == null) {
                continue;
            }

            LOGGER.info("BIOME: {}", biomeId);

            for (var featureStep : biome.getGenerationSettings().features()) {
                for (var featureHolder : featureStep) {
                    featureHolder.unwrapKey().ifPresent(key ->
                            LOGGER.info("  FEATURE: {}", key)
                    );
                }
            }
        }

        LOGGER.info("========== END BIOME FEATURE REPORT ==========");
    }
}