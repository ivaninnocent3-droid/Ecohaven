package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import net.minecraft.world.level.block.Blocks;

public final class PlantSpeciesRegistry {

    private PlantSpeciesRegistry() {
    }

    public static final PlantSpecies SHORT_GRASS =
            new PlantSpecies(
                    "short_grass",
                    Blocks.SHORT_GRASS,
                    35,
                    0.45F,
                    0.45F,
                    0.08F
            );

    public static final PlantSpecies TALL_GRASS =
            new PlantSpecies(
                    "tall_grass",
                    Blocks.TALL_GRASS,
                    20,
                    0.35F,
                    0.40F,
                    0.06F
            );

    public static final PlantSpecies FERN =
            new PlantSpecies(
                    "fern",
                    Blocks.FERN,
                    18,
                    0.80F,
                    0.65F,
                    0.07F
            );

    public static final PlantSpecies LARGE_FERN =
            new PlantSpecies(
                    "large_fern",
                    Blocks.LARGE_FERN,
                    6,
                    0.90F,
                    0.70F,
                    0.035F
            );

    public static final PlantSpecies DANDELION =
            new PlantSpecies(
                    "dandelion",
                    Blocks.DANDELION,
                    8,
                    0.30F,
                    0.35F,
                    0.025F
            );

    public static final PlantSpecies POPPY =
            new PlantSpecies(
                    "poppy",
                    Blocks.POPPY,
                    7,
                    0.30F,
                    0.40F,
                    0.025F
            );

    public static final PlantSpecies OXEYE_DAISY =
            new PlantSpecies(
                    "oxeye_daisy",
                    Blocks.OXEYE_DAISY,
                    5,
                    0.35F,
                    0.45F,
                    0.02F
            );
}