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

    public enum Distribution {
        SCATTERED,
        CLUMPED
    }

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
                throw new IllegalArgumentException("Tree family cannot be blank");
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

    public record TreeSpawn(
            String id,
            List<FamilySpawn> families,
            int chancePerChunk,
            Distribution distribution,
            int attemptsPerChunk,
            net.minecraft.world.level.block.Block groundCheck
    ) {
        public TreeSpawn {
            if (id.isBlank()) {
                throw new IllegalArgumentException("Tree spawn id cannot be blank");
            }

            families = List.copyOf(families);

            if (families.isEmpty()) {
                throw new IllegalArgumentException(
                        "Tree spawn must contain at least one tree family: " + id
                );
            }

            if (chancePerChunk < 1) {
                throw new IllegalArgumentException(
                        "chancePerChunk must be at least 1"
                );
            }

            if (attemptsPerChunk < 1) {
                throw new IllegalArgumentException(
                        "attemptsPerChunk must be at least 1"
                );
            }

            if (distribution == Distribution.SCATTERED
                    && attemptsPerChunk != 1) {
                throw new IllegalArgumentException(
                        "SCATTERED trees must have exactly one attempt per selected chunk"
                );
            }

            if (distribution == Distribution.CLUMPED
                    && attemptsPerChunk < 2) {
                throw new IllegalArgumentException(
                        "CLUMPED trees need at least two attempts per selected chunk"
                );
            }
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

        private static BiomeProfile oakProfile(
                String id,
                String biome,
                String vanillaTreeFeature,
                int scatteredChance,
                int clumpedChance,
                int clumpedAttempts
        ) {
        ResourceKey<Biome> biomeKey = ResourceKey.create(
                Registries.BIOME,
                Identifier.fromNamespaceAndPath("minecraft", biome)
        );

        return new BiomeProfile(
                id,
                biomeKey,
                List.of(vanillaTreeFeature),
                List.of(
                        new TreeSpawn(
                                id + "_oak_scattered",
                                List.of(FamilySpawn.family("oak")),
                                scatteredChance,
                                Distribution.SCATTERED,
                                1,
                                Blocks.OAK_SAPLING
                        ),
                        new TreeSpawn(
                                id + "_oak_clumped",
                                List.of(FamilySpawn.family("oak")),
                                clumpedChance,
                                Distribution.CLUMPED,
                                clumpedAttempts,
                                Blocks.OAK_SAPLING
                        )
                )
        );
        }

        private static final List<BiomeProfile> PROFILES = List.of(

                // Temperate forests
                oakProfile("forest", "forest",
                        "trees_birch_and_oak_leaf_litter", 5, 12, 4),

                oakProfile("birch_forest", "birch_forest",
                        "trees_birch", 5, 12, 3),

                oakProfile("flower_forest", "flower_forest",
                        "trees_flower_forest", 7, 14, 3),

                oakProfile("dappled_forest", "dappled_forest",
                        "trees_dappled_forest", 5, 10, 4),

                oakProfile("windswept_forest", "windswept_forest",
                        "trees_windswept_forest", 5, 10, 4),

                // Mountain and highland biomes
                oakProfile("grove", "grove",
                        "trees_grove", 6, 14, 3),

                oakProfile("meadow", "meadow",
                        "trees_meadow", 12, 20, 2),

                oakProfile("windswept_hills", "windswept_hills",
                        "trees_windswept_hills", 10, 18, 2),

                oakProfile("windswept_gravelly_hills", "windswept_gravelly_hills",
                        "trees_windswept_hills", 10, 18, 2),

                // Cherry biome
                oakProfile("cherry_grove", "cherry_grove",
                        "trees_cherry", 6, 14, 3),

                // Jungle biomes
                oakProfile("jungle", "jungle",
                        "trees_jungle", 3, 8, 5),

                oakProfile("sparse_jungle", "sparse_jungle",
                        "trees_sparse_jungle", 7, 14, 3),

                // Taiga biomes
                oakProfile("taiga", "taiga",
                        "trees_taiga", 6, 12, 3),

                oakProfile("snowy_taiga", "snowy_taiga",
                        "trees_taiga", 6, 12, 3),

                oakProfile("old_growth_pine_taiga", "old_growth_pine_taiga",
                        "trees_old_growth_pine_taiga", 4, 10, 4),

                oakProfile("old_growth_spruce_taiga", "old_growth_spruce_taiga",
                        "trees_old_growth_spruce_taiga", 4, 10, 4),

                // Plains
                oakProfile("plains", "plains",
                        "trees_plains", 16, 24, 2),

                oakProfile("sunflower_plains", "sunflower_plains",
                        "trees_plains", 16, 24, 2),

                // Savanna
                oakProfile("savanna", "savanna",
                        "trees_savanna", 10, 18, 2),

                oakProfile("savanna_plateau", "savanna_plateau",
                        "trees_savanna", 10, 18, 2),

                oakProfile("windswept_savanna", "windswept_savanna",
                        "trees_windswept_savanna", 10, 18, 2),

                // Swamp
                oakProfile("swamp", "swamp",
                        "trees_swamp", 6, 12, 3),

                oakProfile("mangrove_swamp", "mangrove_swamp",
                        "trees_mangrove", 4, 10, 4),

                // Snowy biomes
                oakProfile("snowy_plains", "snowy_plains",
                        "trees_snowy", 20, 28, 2),

                oakProfile("ice_spikes", "ice_spikes",
                        "trees_snowy", 24, 32, 2),

                // Badlands
                oakProfile("wooded_badlands", "wooded_badlands",
                        "trees_badlands", 12, 20, 2)
        );

    public static List<BiomeProfile> profiles() {
        return PROFILES;
    }

    public static List<TreeSpawn> allTreeSpawns() {
        return PROFILES.stream()
                .flatMap(profile -> profile.trees().stream())
                .toList();
    }

    public static ResourceKey<net.minecraft.world.level.levelgen.feature.Feature>
    featureKey(TreeSpawn tree) {
        return ResourceKey.create(
                Registries.FEATURE,
                Ecohaven.id("tree_features/" + tree.id())
        );
    }

    public static ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature>
    placedFeatureKey(TreeSpawn tree) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                Ecohaven.id("tree_placements/" + tree.id())
        );
    }
}
