package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import net.minecraft.world.level.block.Blocks;

public final class PlantSpeciesRegistry {

    private PlantSpeciesRegistry() {
    }

    // ------------------------------------------------------------
    // GROUND COVER
    // ------------------------------------------------------------

    public static final PlantSpecies SHORT_GRASS =
            new PlantSpecies(
                    "short_grass",
                    Blocks.SHORT_GRASS,
                    PlantRole.GROUND_COVER,
                    PlantGrowthForm.SINGLE_BLOCK,
                    35, 0.45F, 0.45F, 0.50F, 0.08F
            );

    public static final PlantSpecies TALL_GRASS =
            new PlantSpecies(
                    "tall_grass",
                    Blocks.TALL_GRASS,
                    PlantRole.GROUND_COVER,
                    PlantGrowthForm.SINGLE_BLOCK,
                    20, 0.35F, 0.40F, 0.50F, 0.06F
            );

    public static final PlantSpecies DANDELION =
            new PlantSpecies(
                    "dandelion",
                    Blocks.DANDELION,
                    PlantRole.GROUND_COVER,
                    PlantGrowthForm.SINGLE_BLOCK,
                    8, 0.30F, 0.35F, 0.65F, 0.025F
            );

    public static final PlantSpecies POPPY =
            new PlantSpecies(
                    "poppy",
                    Blocks.POPPY,
                    PlantRole.GROUND_COVER,
                    PlantGrowthForm.SINGLE_BLOCK,
                    7, 0.30F, 0.40F, 0.65F, 0.025F
            );

    public static final PlantSpecies OXEYE_DAISY =
            new PlantSpecies(
                    "oxeye_daisy",
                    Blocks.OXEYE_DAISY,
                    PlantRole.GROUND_COVER,
                    PlantGrowthForm.SINGLE_BLOCK,
                    5, 0.35F, 0.45F, 0.60F, 0.02F
            );

    // ------------------------------------------------------------
    // UNDERSTORY
    // ------------------------------------------------------------

    public static final PlantSpecies FERN =
            new PlantSpecies(
                    "fern",
                    Blocks.FERN,
                    PlantRole.UNDERSTORY,
                    PlantGrowthForm.SINGLE_BLOCK,
                    18, 0.80F, 0.65F, 0.30F, 0.07F
            );

    public static final PlantSpecies LARGE_FERN =
            new PlantSpecies(
                    "large_fern",
                    Blocks.LARGE_FERN,
                    PlantRole.UNDERSTORY,
                    PlantGrowthForm.DOUBLE_BLOCK,
                    6, 0.90F, 0.70F, 0.25F, 0.035F
            );

    // ------------------------------------------------------------
    // SPECIAL SINGLE-BLOCK FLOWERS
    // ------------------------------------------------------------

    public static final PlantSpecies BLUE_ORCHID =
            new PlantSpecies(
                    "blue_orchid",
                    Blocks.BLUE_ORCHID,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.SINGLE_BLOCK,
                    5, 0.65F, 0.90F, 0.25F, 0.015F
            );

    public static final PlantSpecies ALLIUM =
            new PlantSpecies(
                    "allium",
                    Blocks.ALLIUM,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.SINGLE_BLOCK,
                    5, 0.30F, 0.40F, 0.60F, 0.015F
            );

    public static final PlantSpecies CORNFLOWER =
            new PlantSpecies(
                    "cornflower",
                    Blocks.CORNFLOWER,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.SINGLE_BLOCK,
                    5, 0.30F, 0.40F, 0.70F, 0.015F
            );

    public static final PlantSpecies LILY_OF_THE_VALLEY =
            new PlantSpecies(
                    "lily_of_the_valley",
                    Blocks.LILY_OF_THE_VALLEY,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.SINGLE_BLOCK,
                    4, 0.55F, 0.55F, 0.40F, 0.012F
            );

    // ------------------------------------------------------------
    // SPECIAL DOUBLE-BLOCK PLANTS
    // ------------------------------------------------------------

    public static final PlantSpecies SUNFLOWER =
            new PlantSpecies(
                    "sunflower",
                    Blocks.SUNFLOWER,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.DOUBLE_BLOCK,
                    7, 0.20F, 0.40F, 0.90F, 0.015F
            );

    public static final PlantSpecies LILAC =
            new PlantSpecies(
                    "lilac",
                    Blocks.LILAC,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.DOUBLE_BLOCK,
                    5, 0.40F, 0.50F, 0.55F, 0.012F
            );

    public static final PlantSpecies ROSE_BUSH =
            new PlantSpecies(
                    "rose_bush",
                    Blocks.ROSE_BUSH,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.DOUBLE_BLOCK,
                    5, 0.35F, 0.45F, 0.60F, 0.012F
            );

    public static final PlantSpecies PEONY =
            new PlantSpecies(
                    "peony",
                    Blocks.PEONY,
                    PlantRole.SPECIAL,
                    PlantGrowthForm.DOUBLE_BLOCK,
                    5, 0.40F, 0.50F, 0.55F, 0.012F
            );
    public static final PlantSpecies WILDFLOWERS =
        new PlantSpecies(
                "wildflowers",
                Blocks.WILDFLOWERS,
                PlantRole.SPECIAL,
                PlantGrowthForm.SINGLE_BLOCK,
                4, 0.45F, 0.50F, 0.55F, 0.015F
        );
}