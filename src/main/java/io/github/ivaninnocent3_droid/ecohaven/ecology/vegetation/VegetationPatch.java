package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import java.util.List;
import java.util.Objects;

public final class VegetationPatch {

    private final List<PlantSpecies> palette;
    private final PlantSpecies dominantSpecies;
    private final int radius;
    private final float dominance;

    public VegetationPatch(
            List<PlantSpecies> palette,
            PlantSpecies dominantSpecies,
            int radius,
            float dominance
    ) {
        this.palette = List.copyOf(
                Objects.requireNonNull(palette, "palette")
        );

        this.dominantSpecies =
                Objects.requireNonNull(
                        dominantSpecies,
                        "dominantSpecies"
                );

        if (this.palette.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vegetation patch palette must not be empty"
            );
        }

        if (!this.palette.contains(dominantSpecies)) {
            throw new IllegalArgumentException(
                    "Dominant species must be part of the patch palette"
            );
        }

        if (this.palette.size() > 3) {
            throw new IllegalArgumentException(
                    "Vegetation patch palette may contain at most 3 species"
            );
        }

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

    public List<PlantSpecies> palette() {
        return palette;
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