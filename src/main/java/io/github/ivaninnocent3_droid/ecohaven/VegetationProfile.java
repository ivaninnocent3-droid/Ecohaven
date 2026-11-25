package io.github.ivaninnocent3_droid.ecohaven;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/** Biome-specific rules for ground vegetation. */
public record VegetationProfile(
        String id,
        ResourceKey<Biome> biome,
        List<VegetationSpecies> species,
        float patchChancePerCell,
        int minPatchRadius,
        int maxPatchRadius
) {
    public VegetationProfile {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Vegetation profile id cannot be blank");
        }
        species = List.copyOf(species);
        if (species.isEmpty()) {
            throw new IllegalArgumentException("Vegetation profile must contain species: " + id);
        }
        if (patchChancePerCell <= 0.0F || patchChancePerCell > 1.0F) {
            throw new IllegalArgumentException("patchChancePerCell must be in (0, 1]: " + id);
        }
        if (minPatchRadius < 1 || maxPatchRadius < minPatchRadius) {
            throw new IllegalArgumentException("Invalid vegetation patch radius range: " + id);
        }
    }

    public boolean matches(Holder<Biome> biomeHolder) {
        return biomeHolder.is(biome);
    }
}
