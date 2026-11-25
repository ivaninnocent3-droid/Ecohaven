package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import java.util.Objects;

public final class VegetationPatch {

    private final PlantSpecies dominantSpecies;
    private final int radius;
    private final float dominance;

    public VegetationPatch(
            PlantSpecies dominantSpecies,
            int radius,
            float dominance
    ) {
        this.dominantSpecies =
                Objects.requireNonNull(dominantSpecies, "dominantSpecies");

        if (radius < 2) {
            throw new IllegalArgumentException(
                    "Vegetation patch radius must be at least 2"
            );
        }

        if (dominance < 0.0F || dominance > 1.0F) {
            throw new IllegalArgumentException(
                    "Vegetation patch dominance must be between 0 and 1"
            );
        }

        this.radius = radius;
        this.dominance = dominance;
    }

    public PlantSpecies dominantSpecies() {
        return dominantSpecies;
    }

    public int radius() {
        return radius;
    }

    public float dominance() {
        return dominance;
    }
}