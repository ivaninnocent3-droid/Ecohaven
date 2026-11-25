package io.github.ivaninnocent3_droid.ecohaven;

import net.minecraft.world.level.block.Block;

/** A plant species that can participate in Ecohaven's vegetation simulation. */
public record VegetationSpecies(
        String id,
        Block block,
        int baseWeight,
        int patchWeight,
        float placementChance,
        float canopyMultiplier,
        float openMultiplier,
        TreeCanopy preferredCanopy
) {
    public VegetationSpecies {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Vegetation species id cannot be blank");
        }
        if (baseWeight < 1 || patchWeight < 1) {
            throw new IllegalArgumentException("Vegetation weights must be at least 1: " + id);
        }
        if (placementChance <= 0.0F || placementChance > 1.0F) {
            throw new IllegalArgumentException("placementChance must be in (0, 1]: " + id);
        }
        if (canopyMultiplier <= 0.0F || openMultiplier <= 0.0F) {
            throw new IllegalArgumentException("Vegetation multipliers must be positive: " + id);
        }
    }

    public double weight(boolean covered, TreeCanopy canopy, boolean dominantPatch) {
        double weight = dominantPatch ? patchWeight : baseWeight;

        if (covered) {
            weight *= canopyMultiplier;
            if (preferredCanopy != null && preferredCanopy == canopy) {
                weight *= 1.8D;
            } else if (preferredCanopy != null && canopy != TreeCanopy.NONE) {
                weight *= 0.65D;
            }
        } else {
            weight *= openMultiplier;
        }

        return weight;
    }
}
