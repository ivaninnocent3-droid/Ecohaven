package io.github.ivaninnocent3_droid.ecohaven;

public record VegetationPatch(
        VegetationSpecies species,
        int centerX,
        int centerZ,
        int radius
) {
    public boolean contains(int x, int z) {
        int dx = x - centerX;
        int dz = z - centerZ;
        return dx * dx + dz * dz <= radius * radius;
    }

    public int distanceSquared(int x, int z) {
        int dx = x - centerX;
        int dz = z - centerZ;
        return dx * dx + dz * dz;
    }
}
