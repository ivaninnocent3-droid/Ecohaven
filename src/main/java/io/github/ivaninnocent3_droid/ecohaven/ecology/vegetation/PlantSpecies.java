package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import net.minecraft.world.level.block.Block;

import java.util.Objects;

public final class PlantSpecies {

    private final String id;
    private final Block block;
    private final PlantRole role;
    private final PlantGrowthForm growthForm;

    private final int weight;
    private final float shadeTolerance;
    private final float moisturePreference;
    private final float exposurePreference;
    private final float spreadChance;

    public PlantSpecies(
            String id,
            Block block,
            PlantRole role,
            PlantGrowthForm growthForm,
            int weight,
            float shadeTolerance,
            float moisturePreference,
            float exposurePreference,
            float spreadChance
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.block = Objects.requireNonNull(block, "block");
        this.role = Objects.requireNonNull(role, "role");
        this.growthForm = Objects.requireNonNull(
                growthForm,
                "growthForm"
        );

        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }

        if (weight < 1) {
            throw new IllegalArgumentException(
                    "weight must be at least 1"
            );
        }

        validateRange(shadeTolerance, "shadeTolerance");
        validateRange(moisturePreference, "moisturePreference");
        validateRange(exposurePreference, "exposurePreference");
        validateRange(spreadChance, "spreadChance");

        this.weight = weight;
        this.shadeTolerance = shadeTolerance;
        this.moisturePreference = moisturePreference;
        this.exposurePreference = exposurePreference;
        this.spreadChance = spreadChance;
    }

    private static void validateRange(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
            throw new IllegalArgumentException(
                    name + " must be between 0 and 1"
            );
        }
    }

    public String id() {
        return id;
    }

    public Block block() {
        return block;
    }

    public PlantRole role() {
        return role;
    }

    public PlantGrowthForm growthForm() {
        return growthForm;
    }

    public int weight() {
        return weight;
    }

    public float shadeTolerance() {
        return shadeTolerance;
    }

    public float moisturePreference() {
        return moisturePreference;
    }

    public float exposurePreference() {
        return exposurePreference;
    }

    public float spreadChance() {
        return spreadChance;
    }
}