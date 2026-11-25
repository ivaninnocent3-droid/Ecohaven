package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Objects;

/**
 * Defines the vegetation community associated with a biome.
 *
 * A profile describes which plants are allowed to occur and
 * controls the general density and patch behaviour of vegetation.
 */
public final class VegetationProfile {

    private final String id;
    private final ResourceKey<Biome> biome;
    private final List<PlantSpecies> species;

    /**
     * Base number of vegetation generation attempts per chunk.
     */
    private final int attemptsPerChunk;

    /**
     * Chance that a generation attempt creates a dominant patch
     * instead of an individual plant.
     */
    private final float dominantPatchChance;

    /**
     * Chance for small grass/ground plants to appear naturally
     * on otherwise suitable grass blocks.
     */
    private final float smallPlantChance;

    public VegetationProfile(
            String id,
            ResourceKey<Biome> biome,
            List<PlantSpecies> species,
            int attemptsPerChunk,
            float dominantPatchChance,
            float smallPlantChance
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.biome = Objects.requireNonNull(biome, "biome");
        this.species = List.copyOf(
                Objects.requireNonNull(species, "species")
        );

        if (this.species.isEmpty()) {
            throw new IllegalArgumentException(
                    "A vegetation profile must contain at least one species"
            );
        }

        if (attemptsPerChunk < 1) {
            throw new IllegalArgumentException(
                    "attemptsPerChunk must be at least 1"
            );
        }

        if (dominantPatchChance < 0.0F || dominantPatchChance > 1.0F) {
            throw new IllegalArgumentException(
                    "dominantPatchChance must be between 0 and 1"
            );
        }

        if (smallPlantChance < 0.0F || smallPlantChance > 1.0F) {
            throw new IllegalArgumentException(
                    "smallPlantChance must be between 0 and 1"
            );
        }

        this.attemptsPerChunk = attemptsPerChunk;
        this.dominantPatchChance = dominantPatchChance;
        this.smallPlantChance = smallPlantChance;
    }

    public String id() {
        return id;
    }

    public ResourceKey<Biome> biome() {
        return biome;
    }

    public List<PlantSpecies> species() {
        return species;
    }

    public int attemptsPerChunk() {
        return attemptsPerChunk;
    }

    public float dominantPatchChance() {
        return dominantPatchChance;
    }

    public float smallPlantChance() {
        return smallPlantChance;
    }
}