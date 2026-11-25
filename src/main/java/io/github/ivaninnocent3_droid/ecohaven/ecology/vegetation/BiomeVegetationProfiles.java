package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines the vegetation communities used by vanilla biomes.
 *
 * This class describes vegetation only.
 * Actual placement is handled by the vegetation generation system.
 */
public final class BiomeVegetationProfiles {

    private BiomeVegetationProfiles() {
    }

    private static ResourceKey<Biome> biome(String id) {
        return ResourceKey.create(
                Registries.BIOME,
                Identifier.fromNamespaceAndPath("minecraft", id)
        );
    }

    private static VegetationProfile profile(
            String id,
            String biome,
            List<PlantSpecies> species,
            int attemptsPerChunk,
            float dominantPatchChance,
            float smallPlantChance
    ) {
        return new VegetationProfile(
                id,
                biome(biome),
                species,
                attemptsPerChunk,
                dominantPatchChance,
                smallPlantChance
        );
    }

    private static final List<VegetationProfile> PROFILES = List.of(

            /*
             * ============================================================
             * TEMPERATE WOODLAND
             * ============================================================
             */

            profile(
                    "forest",
                    "forest",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY,
                            PlantSpeciesRegistry.LILY_OF_THE_VALLEY
                    ),
                    20,
                    0.18F,
                    0.45F
            ),

            profile(
                    "birch_forest",
                    "birch_forest",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.OXEYE_DAISY,
                            PlantSpeciesRegistry.LILY_OF_THE_VALLEY
                    ),
                    20,
                    0.20F,
                    0.42F
            ),

            profile(
                    "flower_forest",
                    "flower_forest",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.ALLIUM,
                            PlantSpeciesRegistry.LILY_OF_THE_VALLEY
                    ),
                    24,
                    0.35F,
                    0.55F
            ),

            profile(
                    "dappled_forest",
                    "dappled_forest",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY
                    ),
                    22,
                    0.22F,
                    0.50F
            ),

            profile(
                    "windswept_forest",
                    "windswept_forest",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY
                    ),
                    18,
                    0.12F,
                    0.40F
            ),

            /*
             * ============================================================
             * OPEN TEMPERATE LAND
             * ============================================================
             */

            profile(
                    "plains",
                    "plains",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY
                    ),
                    26,
                    0.28F,
                    0.65F
            ),

            profile(
                    "sunflower_plains",
                    "sunflower_plains",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY
                    ),
                    28,
                    0.30F,
                    0.68F
            ),

            profile(
                    "meadow",
                    "meadow",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY,
                            PlantSpeciesRegistry.CORNFLOWER
                    ),
                    30,
                    0.38F,
                    0.70F
            ),

            /*
             * ============================================================
             * JUNGLE / HUMID VEGETATION
             * ============================================================
             */

            profile(
                    "jungle",
                    "jungle",
                    List.of(
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS
                    ),
                    30,
                    0.30F,
                    0.55F
            ),

            profile(
                    "sparse_jungle",
                    "sparse_jungle",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    24,
                    0.22F,
                    0.55F
            ),

            profile(
                    "swamp",
                    "swamp",
                    List.of(
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.BLUE_ORCHID
                    ),
                    28,
                    0.30F,
                    0.58F
            ),

            /*
             * ============================================================
             * TAIGA
             * ============================================================
             */

            profile(
                    "taiga",
                    "taiga",
                    List.of(
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    20,
                    0.24F,
                    0.40F
            ),

            profile(
                    "old_growth_pine_taiga",
                    "old_growth_pine_taiga",
                    List.of(
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    18,
                    0.28F,
                    0.36F
            ),

            profile(
                    "old_growth_spruce_taiga",
                    "old_growth_spruce_taiga",
                    List.of(
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    18,
                    0.28F,
                    0.36F
            ),

            /*
             * ============================================================
             * SAVANNA
             * ============================================================
             */

            profile(
                    "savanna",
                    "savanna",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    22,
                    0.18F,
                    0.52F
            ),

            profile(
                    "savanna_plateau",
                    "savanna_plateau",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    20,
                    0.15F,
                    0.48F
            ),

            profile(
                    "windswept_savanna",
                    "windswept_savanna",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS
                    ),
                    18,
                    0.10F,
                    0.42F
            ),

            /*
             * ============================================================
             * CHERRY GROVE
             * ============================================================
             */

            profile(
                    "cherry_grove",
                    "cherry_grove",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.POPPY,
                            PlantSpeciesRegistry.OXEYE_DAISY
                    ),
                    22,
                    0.25F,
                    0.50F
            ),

            /*
             * ============================================================
             * MOUNTAIN GROVE
             * ============================================================
             */

            profile(
                    "grove",
                    "grove",
                    List.of(
                            PlantSpeciesRegistry.FERN,
                            PlantSpeciesRegistry.LARGE_FERN,
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.DANDELION,
                            PlantSpeciesRegistry.OXEYE_DAISY
                    ),
                    20,
                    0.24F,
                    0.42F
            ),

            /*
             * ============================================================
             * WOODED BADLANDS
             * ============================================================
             */

            profile(
                    "wooded_badlands",
                    "wooded_badlands",
                    List.of(
                            PlantSpeciesRegistry.SHORT_GRASS,
                            PlantSpeciesRegistry.TALL_GRASS,
                            PlantSpeciesRegistry.DANDELION
                    ),
                    16,
                    0.10F,
                    0.32F
            )
    );

    /**
     * Fast lookup used by the future generation system.
     *
     * ResourceKey is used rather than biome instances so this remains
     * compatible with Minecraft's registry-based biome system.
     */
    private static final Map<ResourceKey<Biome>, VegetationProfile> BY_BIOME =
            PROFILES.stream().collect(
                    Collectors.toUnmodifiableMap(
                            VegetationProfile::biome,
                            Function.identity()
                    )
            );

    public static List<VegetationProfile> profiles() {
        return PROFILES;
    }

    public static VegetationProfile profileFor(ResourceKey<Biome> biome) {
        return BY_BIOME.get(biome);
    }
}