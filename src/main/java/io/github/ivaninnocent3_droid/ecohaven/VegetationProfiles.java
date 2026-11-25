package io.github.ivaninnocent3_droid.ecohaven;

import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;

/** Initial biome vegetation catalogue. Kept code-driven until the rules settle. */
public final class VegetationProfiles {
    private VegetationProfiles() {}

    private static ResourceKey<Biome> biome(String id) {
        return ResourceKey.create(
                Registries.BIOME,
                Identifier.fromNamespaceAndPath("minecraft", id)
        );
    }

    private static VegetationSpecies species(
            String id,
            net.minecraft.world.level.block.Block block,
            int baseWeight,
            int patchWeight,
            float placementChance,
            float canopyMultiplier,
            float openMultiplier,
            TreeCanopy preferredCanopy
    ) {
        return new VegetationSpecies(
                id,
                block,
                baseWeight,
                patchWeight,
                placementChance,
                canopyMultiplier,
                openMultiplier,
                preferredCanopy
        );
    }

    private static final List<VegetationSpecies> FOREST_SPECIES = List.of(
            // Ferns favour shaded woodland and become strong competitors inside fern patches.
            species("fern", Blocks.FERN, 28, 100, 0.42F, 1.35F, 0.55F, TreeCanopy.OAK),
            species("large_fern", Blocks.LARGE_FERN, 8, 55, 0.20F, 1.20F, 0.45F, TreeCanopy.OAK),

            // Grass remains common, but short grass is intentionally less frequent than a normal vanilla carpet.
            species("short_grass", Blocks.SHORT_GRASS, 24, 45, 0.10F, 0.90F, 1.15F, null),
            species("tall_grass", Blocks.TALL_GRASS, 18, 60, 0.26F, 0.90F, 1.05F, null),

            // Flowers prefer more open light, but can still occur beneath sparse woodland.
            species("dandelion", Blocks.DANDELION, 7, 70, 0.22F, 0.60F, 1.35F, null),
            species("poppy", Blocks.POPPY, 6, 65, 0.22F, 0.60F, 1.30F, null),
            species("oxeye_daisy", Blocks.OXEYE_DAISY, 5, 60, 0.20F, 0.55F, 1.30F, null)
    );

    private static final VegetationProfile FOREST = new VegetationProfile(
            "forest",
            biome("forest"),
            FOREST_SPECIES,
            0.72F,
            3,
            9
    );

    private static final VegetationProfile FLOWER_FOREST = new VegetationProfile(
            "flower_forest",
            biome("flower_forest"),
            List.of(
                    species("dandelion", Blocks.DANDELION, 8, 80, 0.30F, 0.55F, 1.40F, null),
                    species("poppy", Blocks.POPPY, 8, 80, 0.30F, 0.55F, 1.40F, null),
                    species("oxeye_daisy", Blocks.OXEYE_DAISY, 8, 80, 0.30F, 0.55F, 1.40F, null),
                    species("azure_bluet", Blocks.AZURE_BLUET, 7, 75, 0.28F, 0.55F, 1.35F, null),
                    species("cornflower", Blocks.CORNFLOWER, 7, 75, 0.28F, 0.55F, 1.35F, null),
                    species("allium", Blocks.ALLIUM, 5, 70, 0.24F, 0.50F, 1.35F, null),
                    species("short_grass", Blocks.SHORT_GRASS, 18, 35, 0.08F, 0.85F, 1.10F, null)
            ),
            0.90F,
            2,
            7
    );

    private static final VegetationProfile BIRCH_FOREST = new VegetationProfile(
            "birch_forest",
            biome("birch_forest"),
            List.of(
                    species("fern", Blocks.FERN, 32, 105, 0.44F, 1.45F, 0.50F, TreeCanopy.BIRCH),
                    species("large_fern", Blocks.LARGE_FERN, 9, 55, 0.20F, 1.25F, 0.40F, TreeCanopy.BIRCH),
                    species("short_grass", Blocks.SHORT_GRASS, 24, 45, 0.10F, 0.90F, 1.15F, null),
                    species("tall_grass", Blocks.TALL_GRASS, 17, 60, 0.25F, 0.90F, 1.05F, null),
                    species("oxeye_daisy", Blocks.OXEYE_DAISY, 6, 65, 0.20F, 0.60F, 1.30F, null)
            ),
            0.72F,
            3,
            9
    );

    private static final VegetationProfile TAIGA = new VegetationProfile(
            "taiga",
            biome("taiga"),
            List.of(
                    species("fern", Blocks.FERN, 30, 100, 0.40F, 1.30F, 0.55F, TreeCanopy.SPRUCE),
                    species("large_fern", Blocks.LARGE_FERN, 7, 50, 0.18F, 1.15F, 0.45F, TreeCanopy.SPRUCE),
                    species("short_grass", Blocks.SHORT_GRASS, 18, 38, 0.08F, 0.85F, 1.00F, null),
                    species("tall_grass", Blocks.TALL_GRASS, 14, 50, 0.22F, 0.85F, 1.00F, null)
            ),
            0.68F,
            3,
            8
    );

    private static final Map<ResourceKey<Biome>, VegetationProfile> BY_BIOME = Map.of(
            FOREST.biome(), FOREST,
            FLOWER_FOREST.biome(), FLOWER_FOREST,
            BIRCH_FOREST.biome(), BIRCH_FOREST,
            TAIGA.biome(), TAIGA
    );

    public static VegetationProfile forBiome(ResourceKey<Biome> biome) {
        return BY_BIOME.get(biome);
    }

    public static List<VegetationProfile> all() {
        return List.copyOf(BY_BIOME.values());
    }
}
