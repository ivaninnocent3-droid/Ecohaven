package io.github.ivaninnocent3_droid.ecohaven;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;

public final class EcohavenTreeProfiles {

    private EcohavenTreeProfiles() {}

    public enum Distribution {
        SCATTERED,
        CLUMPED
    }

    public record TreeSpawn(
            String id,
            String template,
            int chancePerChunk,
            Distribution distribution,
            int attemptsPerChunk,
            net.minecraft.world.level.block.Block groundCheck
    ) {
        public TreeSpawn {
            if (chancePerChunk < 1) {
                throw new IllegalArgumentException("chancePerChunk must be at least 1");
            }

            if (attemptsPerChunk < 1) {
                throw new IllegalArgumentException("attemptsPerChunk must be at least 1");
            }

            if (distribution == Distribution.SCATTERED && attemptsPerChunk != 1) {
                throw new IllegalArgumentException(
                        "SCATTERED trees must have exactly one attempt per selected chunk"
                );
            }

            if (distribution == Distribution.CLUMPED && attemptsPerChunk < 2) {
                throw new IllegalArgumentException(
                        "CLUMPED trees need at least two attempts per selected chunk"
                );
            }
        }
    }

    public record BiomeProfile(
            String id,
            ResourceKey<Biome> biome,
            List<TreeSpawn> trees
    ) {
        public BiomeProfile {
            trees = List.copyOf(trees);
        }
    }

    private static ResourceKey<Biome> biomeKey(String name) {
        return ResourceKey.create(
                Registries.BIOME,
                Ecohaven.id(name)
        );
    }

    /*
     * BIOME TREE CONFIGURATION
     *
     * chancePerChunk:
     *   5 means approximately one selected chunk in every five.
     *
     * SCATTERED:
     *   One tree placement attempt in a selected chunk.
     *
     * CLUMPED:
     *   Multiple tree placement attempts in a selected chunk.
     */
    private static final List<BiomeProfile> PROFILES = List.of(

            new BiomeProfile(
                    "forest",
                    biomeKey("forest"),
                    List.of(
                            new TreeSpawn(
                                    "forest_oak_scattered",
                                    "trees/oak_01",
                                    5,
                                    Distribution.SCATTERED,
                                    1,
                                    Blocks.OAK_SAPLING
                            ),

                            new TreeSpawn(
                                    "forest_oak_clumped",
                                    "trees/oak_01",
                                    12,
                                    Distribution.CLUMPED,
                                    4,
                                    Blocks.OAK_SAPLING
                            )
                    )
            )

            // Add more biome profiles here as you create their tree structures.
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