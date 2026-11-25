package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import net.minecraft.world.level.block.Block;

import java.util.Objects;

/**
 * Describes a plant species that can occur as natural vegetation.
 *
 * This class contains ecological properties only.
 * Actual world generation is handled by the vegetation generation system.
 */
public final class PlantSpecies {

    private final String id;
    private final Block block;

    /**
     * Relative chance of this species being selected when several
     * plants are competing for the same location.
     */
    private final int weight;

    /**
     * How well the plant tolerates shade.
     *
     * 0.0 = strongly prefers open ground
     * 1.0 = highly shade tolerant
     */
    private final float shadeTolerance;

    /**
     * Preference for moist environments.
     *
     * 0.0 = prefers dry conditions
     * 1.0 = prefers wet conditions
     */
    private final float moisturePreference;

    /**
     * Chance that an established plant can participate in
     * future natural spreading.
     */
    private final float spreadChance;

    public PlantSpecies(
            String id,
            Block block,
            int weight,
            float shadeTolerance,
            float moisturePreference,
            float spreadChance
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.block = Objects.requireNonNull(block, "block");

        if (weight < 1) {
            throw new IllegalArgumentException("weight must be at least 1");
        }

        if (shadeTolerance < 0.0F || shadeTolerance > 1.0F) {
            throw new IllegalArgumentException(
                    "shadeTolerance must be between 0 and 1"
            );
        }

        if (moisturePreference < 0.0F || moisturePreference > 1.0F) {
            throw new IllegalArgumentException(
                    "moisturePreference must be between 0 and 1"
            );
        }

        if (spreadChance < 0.0F || spreadChance > 1.0F) {
            throw new IllegalArgumentException(
                    "spreadChance must be between 0 and 1"
            );
        }

        this.weight = weight;
        this.shadeTolerance = shadeTolerance;
        this.moisturePreference = moisturePreference;
        this.spreadChance = spreadChance;
    }

    public String id() {
        return id;
    }

    public Block block() {
        return block;
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

    public float spreadChance() {
        return spreadChance;
    }
}