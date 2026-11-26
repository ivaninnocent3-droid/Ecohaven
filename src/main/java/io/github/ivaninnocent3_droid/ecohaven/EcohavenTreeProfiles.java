package io.github.ivaninnocent3_droid.ecohaven;

import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;

public final class EcohavenTreeProfiles {

    private EcohavenTreeProfiles() {}

    /**
     * A weighted tree family inside a spawn.
     *
     * If variantWeights is empty, every discovered variant in the family
     * automatically receives weight 1.
     */
    public record FamilySpawn(
            String family,
            int weight,
            Map<String, Integer> variantWeights
    ) {
        public FamilySpawn {
            if (family.isBlank()) {
                throw new IllegalArgumentException(
                        "Tree family cannot be blank"
                );
            }

            if (weight < 1) {
                throw new IllegalArgumentException(
                        "Tree family weight must be at least 1"
                );
            }

            variantWeights = Map.copyOf(variantWeights);

            variantWeights.forEach((variant, variantWeight) -> {
                if (variant.isBlank()) {
                    throw new IllegalArgumentException(
                            "Tree variant cannot be blank"
                    );
                }

                if (variantWeight < 1) {
                    throw new IllegalArgumentException(
                            "Tree variant weight must be at least 1: "
                                    + family + "_" + variant
                    );
                }
            });
        }

        public static FamilySpawn family(String family) {
            return new FamilySpawn(family, 1, Map.of());
        }

        public static FamilySpawn family(
                String family,
                int weight
        ) {
            return new FamilySpawn(family, weight, Map.of());
        }

        public static FamilySpawn family(
                String family,
                int weight,
                Map<String, Integer> variantWeights
        ) {
            return new FamilySpawn(family, weight, variantWeights);
        }
    }

    /**
     * Describes how many tree-placement attempts a biome gets.
     *
     * baseCount:
     *     Normal number of attempts.
     *
     * extraChance:
     *     Chance of receiving extraCount additional attempts.
     *     Set to 0 for a constant count.
     *
     * extraCount:
     *     Number of additional attempts when the extra roll succeeds.
     *
     * Examples:
     *
     *     constant(6)
     *         -> exactly 6 attempts
     *
     *     extra(10, 0.10F, 1)
     *         -> 10 attempts normally, 11 attempts 10% of the time
     *
     *     extra(0, 0.05F, 1)
     *         -> 0 attempts normally, 1 attempt 5% of the time
     */
    public record TreeSpawn(
            String id,
            List<FamilySpawn> families,
            int baseCount,
            float extraChance,
            int extraCount,
            net.minecraft.world.level.block.Block groundCheck
    ) {
        public TreeSpawn {
            if (id.isBlank()) {
                throw new IllegalArgumentException(
                        "Tree spawn id cannot be blank"
                );
            }

            families = List.copyOf(families);

            if (families.isEmpty()) {
                throw new IllegalArgumentException(
                        "Tree spawn must contain at least one tree family: "
                                + id
                );
            }

            if (baseCount < 0) {
                throw new IllegalArgumentException(
                        "baseCount cannot be negative: " + id
                );
            }

            if (extraChance < 0.0F || extraChance > 1.0F) {
                throw new IllegalArgumentException(
                        "extraChance must be between 0 and 1: " + id
                );
            }

            if (extraCount < 0) {
                throw new IllegalArgumentException(
                        "extraCount cannot be negative: " + id
                );
            }

            if (extraChance == 0.0F && extraCount != 0) {
                throw new IllegalArgumentException(
                        "extraCount must be 0 when extraChance is 0: " + id
                );
            }

            if (extraChance > 0.0F && extraCount < 1) {
                throw new IllegalArgumentException(
                        "extraCount must be at least 1 when extraChance is "
                                + "greater than 0: " + id
                );
            }
        }

        public static TreeSpawn constant(
                String id,
                List<FamilySpawn> families,
                int count,
                net.minecraft.world.level.block.Block groundCheck
        ) {
            return new TreeSpawn(
                    id,
                    families,
                    count,
                    0.0F,
                    0,
                    groundCheck
            );
        }

        public static TreeSpawn extra(
                String id,
                List<FamilySpawn> families,
                int baseCount,
                float extraChance,
                int extraCount,
                net.minecraft.world.level.block.Block groundCheck
        ) {
            return new TreeSpawn(
                    id,
                    families,
                    baseCount,
                    extraChance,
                    extraCount,
                    groundCheck
            );
        }
    }

    public record BiomeProfile(
            String id,
            ResourceKey<Biome> biome,
            List<String> vanillaTreeFeaturesToRemove,
            List<TreeSpawn> trees
    ) {
        public BiomeProfile {
            vanillaTreeFeaturesToRemove =
                    List.copyOf(vanillaTreeFeaturesToRemove);

            trees = List.copyOf(trees);
        }
    }

    /**
     * Creates a profile for a biome whose vanilla tree feature is replaced
     * by one Ecohaven tree family using vanilla-style placement density.
     */
    private static BiomeProfile oakProfile(
            String id,
            String biome,
            String vanillaTreeFeature,
            int baseCount,
            float extraChance,
            int extraCount
    ) {
        ResourceKey<Biome> biomeKey = ResourceKey.create(
                Registries.BIOME,
                Identifier.fromNamespaceAndPath("minecraft", biome)
        );

        TreeSpawn tree;

        if (extraChance == 0.0F) {
            tree = TreeSpawn.constant(
                    id + "_trees",
                    List.of(FamilySpawn.family("oak")),
                    baseCount,
                    Blocks.OAK_SAPLING
            );
        } else {
            tree = TreeSpawn.extra(
                    id + "_trees",
                    List.of(FamilySpawn.family("oak")),
                    baseCount,
                    extraChance,
                    extraCount,
                    Blocks.OAK_SAPLING
            );
        }

        return new BiomeProfile(
                id,
                biomeKey,
                List.of(vanillaTreeFeature),
                List.of(tree)
        );
    }

    private static final List<BiomeProfile> PROFILES = List.of(

            // ------------------------------------------------------------
            // Temperate forests
            // ------------------------------------------------------------

            oakProfile(
                    "forest",
                    "forest",
                    "trees_birch_and_oak_leaf_litter",
                    10, 0.10F, 1
            ),

            oakProfile(
                    "birch_forest",
                    "birch_forest",
                    "trees_birch",
                    10, 0.10F, 1
            ),

            oakProfile(
                    "flower_forest",
                    "flower_forest",
                    "trees_flower_forest",
                    6, 0.10F, 1
            ),

            oakProfile(
                    "dappled_forest",
                    "dappled_forest",
                    "trees_dappled_forest",
                    6, 0.0F, 0
            ),

            oakProfile(
                    "windswept_forest",
                    "windswept_forest",
                    "trees_windswept_forest",
                    3, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Mountain / highland
            // ------------------------------------------------------------

            oakProfile(
                    "grove",
                    "grove",
                    "trees_grove",
                    10, 0.10F, 1
            ),

            oakProfile(
                    "meadow",
                    "meadow",
                    "trees_meadow",
                    0, 0.01F, 1
            ),

            oakProfile(
                    "windswept_hills",
                    "windswept_hills",
                    "trees_windswept_hills",
                    0, 0.10F, 1
            ),

            oakProfile(
                    "windswept_gravelly_hills",
                    "windswept_gravelly_hills",
                    "trees_windswept_hills",
                    0, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Cherry
            // ------------------------------------------------------------

            oakProfile(
                    "cherry_grove",
                    "cherry_grove",
                    "trees_cherry",
                    10, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Jungle
            // ------------------------------------------------------------

            oakProfile(
                    "jungle",
                    "jungle",
                    "trees_jungle",
                    50, 0.10F, 1
            ),

            oakProfile(
                    "sparse_jungle",
                    "sparse_jungle",
                    "trees_sparse_jungle",
                    2, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Taiga
            // ------------------------------------------------------------

            oakProfile(
                    "taiga",
                    "taiga",
                    "trees_taiga",
                    10, 0.10F, 1
            ),

            oakProfile(
                    "snowy_taiga",
                    "snowy_taiga",
                    "trees_taiga",
                    10, 0.10F, 1
            ),

            oakProfile(
                    "old_growth_pine_taiga",
                    "old_growth_pine_taiga",
                    "trees_old_growth_pine_taiga",
                    10, 0.10F, 1
            ),

            oakProfile(
                    "old_growth_spruce_taiga",
                    "old_growth_spruce_taiga",
                    "trees_old_growth_spruce_taiga",
                    10, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Plains
            // ------------------------------------------------------------

            oakProfile(
                    "plains",
                    "plains",
                    "trees_plains",
                    0, 0.05F, 1
            ),

            oakProfile(
                    "sunflower_plains",
                    "sunflower_plains",
                    "trees_plains",
                    0, 0.05F, 1
            ),

            // ------------------------------------------------------------
            // Savanna
            // ------------------------------------------------------------

            oakProfile(
                    "savanna",
                    "savanna",
                    "trees_savanna",
                    1, 0.10F, 1
            ),

            oakProfile(
                    "savanna_plateau",
                    "savanna_plateau",
                    "trees_savanna",
                    1, 0.10F, 1
            ),

            oakProfile(
                    "windswept_savanna",
                    "windswept_savanna",
                    "trees_windswept_savanna",
                    2, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Swamp
            // ------------------------------------------------------------

            oakProfile(
                    "swamp",
                    "swamp",
                    "trees_swamp",
                    2, 0.10F, 1
            ),

            oakProfile(
                    "mangrove_swamp",
                    "mangrove_swamp",
                    "trees_mangrove",
                    25, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Snowy
            // ------------------------------------------------------------

            oakProfile(
                    "snowy_plains",
                    "snowy_plains",
                    "trees_snowy",
                    0, 0.10F, 1
            ),

            oakProfile(
                    "ice_spikes",
                    "ice_spikes",
                    "trees_snowy",
                    0, 0.10F, 1
            ),

            // ------------------------------------------------------------
            // Badlands
            // ------------------------------------------------------------

            oakProfile(
                    "wooded_badlands",
                    "wooded_badlands",
                    "trees_badlands",
                    5, 0.10F, 1
            )
    );

    public static List<BiomeProfile> profiles() {
        return PROFILES;
    }

    public static List<TreeSpawn> allTreeSpawns() {
        return PROFILES.stream()
                .flatMap(profile -> profile.trees().stream())
                .toList();
    }

    public static ResourceKey<
            net.minecraft.world.level.levelgen.feature.Feature
            > featureKey(TreeSpawn tree) {

        return ResourceKey.create(
                Registries.FEATURE,
                Ecohaven.id("tree_features/" + tree.id())
        );
    }

    public static ResourceKey<
            net.minecraft.world.level.levelgen.placement.PlacedFeature
            > placedFeatureKey(TreeSpawn tree) {

        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                Ecohaven.id("tree_placements/" + tree.id())
        );
    }
}