package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import com.mojang.serialization.MapCodec;
import io.github.ivaninnocent3_droid.ecohaven.Ecohaven;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

/**
 * Generates natural ground vegetation according to the vegetation
 * profile of the biome being generated.
 *
 * This feature deliberately performs only local environmental checks.
 * More complex behaviour such as dominant patches and spreading is
 * implemented by later vegetation systems.
 */
public final class VegetationFeature implements Feature {

    public static final MapCodec<VegetationFeature> CODEC =
            MapCodec.unit(new VegetationFeature());

    public static final ResourceKey<Feature> FEATURE_KEY =
            ResourceKey.create(
                    Registries.FEATURE,
                    Ecohaven.id("vegetation")
            );

    public static final ResourceKey<PlacedFeature> PLACED_FEATURE_KEY =
            ResourceKey.create(
                    Registries.PLACED_FEATURE,
                    Ecohaven.id("vegetation")
            );

    /*
     * Keep environmental scans deliberately small.
     *
     * Vegetation is generated potentially millions of times in a world,
     * so large-radius searches would become expensive very quickly.
     */
    private static final int CANOPY_SCAN_HEIGHT = 12;
    private static final int MOISTURE_SCAN_RADIUS = 4;
    private static final int MIN_PATCH_RADIUS = 3;
    private static final int MAX_PATCH_RADIUS = 8;
    private static final int MIN_PATCH_ATTEMPTS = 5;
    private static final int MAX_PATCH_ATTEMPTS = 14;
    private static final float PATCH_DOMINANCE = 0.80F;
    private static final int SPREAD_ATTEMPTS = 6;
    private static final int SPREAD_SEARCH_RADIUS = 4;
    private static final int TERRAIN_SCAN_RADIUS = 2;

    public VegetationFeature() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.FEATURE_TYPE,
                Ecohaven.id("vegetation"),
                CODEC
        );
    }

    @Override
    public MapCodec<VegetationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
            WorldGenLevel level,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BlockPos origin
    ) {
        var biomeHolder = level.getBiome(origin);

        var biomeKey = biomeHolder.unwrapKey();

        if (biomeKey.isEmpty()) {
            return false;
        }

        VegetationProfile profile =
                BiomeVegetationProfiles.profileFor(biomeKey.get());

        if (profile == null) {
            return false;
        }

        int startX = origin.getX() & ~15;
        int startZ = origin.getZ() & ~15;

        boolean changed = false;

        // Normal vegetation.
        for (int attempt = 0;
        attempt < profile.attemptsPerChunk();
        attempt++) {

        int x = startX + random.nextInt(16);
        int z = startZ + random.nextInt(16);

        changed |= placeVegetation(
                level,
                profile,
                random,
                new BlockPos(x, 0, z),
                null
        );
        }

        // Occasional dominant plant community.
        if (random.nextFloat() < profile.dominantPatchChance()) {

        VegetationPatch patch = createPatch(
                profile,
                random
        );

        changed |= placePatch(
                level,
                profile,
                patch,
                random,
                startX,
                startZ
        );
        }
        changed |= placeSpreadingVegetation(
                level,
                profile,
                random,
                startX,
                startZ
        );
        
        return changed;
    }

    /**
     * Determines whether the ground is suitable for natural
     * vegetation.
     */
    private static boolean isSuitableGround(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.MOSS_BLOCK);
    }

    /**
     * Estimates local shade from blocks above the ground.
     *
     * This intentionally does not attempt to calculate exact sunlight.
     * A small local scan is considerably cheaper and is sufficient
     * for the ecological preferences of our initial plant species.
     */
    private static float calculateShade(
            WorldGenLevel level,
            BlockPos groundPos
    ) {
        BlockPos.MutableBlockPos checkPos =
                groundPos.mutable();

        int shadeBlocks = 0;

        for (int y = 1; y <= CANOPY_SCAN_HEIGHT; y++) {

            checkPos.set(
                    groundPos.getX(),
                    groundPos.getY() + y,
                    groundPos.getZ()
            );

            BlockState state =
                    level.getBlockState(checkPos);

            if (state.is(BlockTags.LEAVES)) {
                shadeBlocks += 2;
            } else if (state.is(BlockTags.LOGS)) {
                shadeBlocks++;
            } else if (state.isSolid()) {
                shadeBlocks += 2;
            }
        }

        /*
         * Four effective shade points is treated as roughly moderate
         * canopy cover. Clamp so the result always remains [0, 1].
         */
        return Math.min(1.0F, shadeBlocks / 8.0F);
    }

    /**
     * Estimates moisture from nearby water.
     *
     * Water directly underneath the plant location is treated as
     * highly moist. Otherwise nearby water progressively increases
     * the moisture value.
     */
    private static float calculateMoisture(
            WorldGenLevel level,
            BlockPos groundPos
    ) {
        if (level.getFluidState(groundPos).is(net.minecraft.tags.FluidTags.WATER)) {
            return 1.0F;
        }

        int radius = MOISTURE_SCAN_RADIUS;
        int radiusSquared = radius * radius;

        int waterBlocks = 0;
        int possiblePositions = 0;

        BlockPos.MutableBlockPos checkPos =
                groundPos.mutable();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                if (dx * dx + dz * dz > radiusSquared) {
                    continue;
                }

                possiblePositions++;

                checkPos.set(
                        groundPos.getX() + dx,
                        groundPos.getY(),
                        groundPos.getZ() + dz
                );

                if (level.getFluidState(checkPos)
                        .is(net.minecraft.tags.FluidTags.WATER)) {
                    waterBlocks++;
                }
            }
        }

        if (possiblePositions == 0) {
            return 0.0F;
        }

        return Math.min(
                1.0F,
                waterBlocks / 8.0F
        );
    }

    /**
     * Selects a plant using its ecological preferences.
     *
     * The species' base weight is modified by how closely the
     * current environment matches its shade and moisture preferences.
     */
private static PlantSpecies selectSpecies(
        List<PlantSpecies> species,
        float shade,
        float moisture,
        float exposure,
        RandomSource random
) {
    int totalWeight = 0;

    // First pass: calculate the total ecological selection weight.
    for (PlantSpecies plant : species) {

        float shadeMatch = preferenceMatch(
                shade,
                plant.shadeTolerance()
        );

        float moistureMatch = preferenceMatch(
                moisture,
                plant.moisturePreference()
        );

        float exposureMatch = preferenceMatch(
                exposure,
                plant.exposurePreference()
        );

        float roleMatch = roleSuitability(
                plant.role(),
                shade
        );

        float suitability =
                shadeMatch
                * moistureMatch
                * exposureMatch
                * roleMatch;

        if (suitability <= 0.0F) {
            continue;
        }

        totalWeight += Math.max(
                1,
                Math.round(plant.weight() * suitability)
        );
    }

    if (totalWeight <= 0) {
        return null;
    }

    int selection = random.nextInt(totalWeight);

    // Second pass: select using exactly the same weights.
    for (PlantSpecies plant : species) {

        float shadeMatch = preferenceMatch(
                shade,
                plant.shadeTolerance()
        );

        float moistureMatch = preferenceMatch(
                moisture,
                plant.moisturePreference()
        );

        float exposureMatch = preferenceMatch(
                exposure,
                plant.exposurePreference()
        );

        float roleMatch = roleSuitability(
                plant.role(),
                shade
        );

        float suitability =
                shadeMatch
                * moistureMatch
                * exposureMatch
                * roleMatch;

        if (suitability <= 0.0F) {
            continue;
        }

        int weight = Math.max(
                1,
                Math.round(plant.weight() * suitability)
        );

        if (selection < weight) {
            return plant;
        }

        selection -= weight;
    }

    return null;
}

/**
 * Adds a broad ecological preference based on the plant's role.
 *
 * Species-specific shade tolerance still provides the detailed
 * preference. This role multiplier distinguishes open-ground
 * vegetation from plants adapted to woodland understories.
 */
private static float roleSuitability(
        PlantRole role,
        float shade
) {
    return switch (role) {
        case GROUND_COVER ->
                1.0F - (shade * 0.25F);

        case UNDERSTORY ->
                0.35F + (shade * 0.65F);

        case SPECIAL ->
                1.0F;
    };
}

    /**
     * Converts the distance between an environmental value and
     * a species preference into a suitability value.
     *
     * Exact match = 1.
     * Completely different = 0.
     */
    private static float preferenceMatch(
            float environment,
            float preference
    ) {
        return Math.max(
                0.0F,
                1.0F - Math.abs(environment - preference)
        );
    }

private static VegetationPatch createPatch(
        VegetationProfile profile,
        RandomSource random
) {
    PlantSpecies dominant =
            selectPatchSpecies(profile.species(), random);

    int radius =
            MIN_PATCH_RADIUS
            + random.nextInt(
                    MAX_PATCH_RADIUS - MIN_PATCH_RADIUS + 1
            );

    return new VegetationPatch(
            dominant,
            radius,
            PATCH_DOMINANCE
    );
}

private static PlantSpecies selectPatchSpecies(
        List<PlantSpecies> species,
        RandomSource random
) {
    int totalWeight = 0;

    for (PlantSpecies plant : species) {
        totalWeight += plant.weight();
    }

    int selection = random.nextInt(totalWeight);

    for (PlantSpecies plant : species) {
        if (selection < plant.weight()) {
            return plant;
        }

        selection -= plant.weight();
    }

    return species.getLast();
}

private static boolean placePatch(
        WorldGenLevel level,
        VegetationProfile profile,
        VegetationPatch patch,
        RandomSource random,
        int startX,
        int startZ
) {
    int centerX = startX + random.nextInt(16);
    int centerZ = startZ + random.nextInt(16);

    int attempts =
            MIN_PATCH_ATTEMPTS
            + random.nextInt(
                    MAX_PATCH_ATTEMPTS - MIN_PATCH_ATTEMPTS + 1
            );

    boolean changed = false;

    for (int attempt = 0; attempt < attempts; attempt++) {

        int dx = random.nextInt(
                patch.radius() * 2 + 1
        ) - patch.radius();

        int dz = random.nextInt(
                patch.radius() * 2 + 1
        ) - patch.radius();

        if (dx * dx + dz * dz
                > patch.radius() * patch.radius()) {
            continue;
        }

        int x = centerX + dx;
        int z = centerZ + dz;

        float dominantChance =
                patch.dominance();

        PlantSpecies species;

        if (random.nextFloat() < dominantChance) {
        species = patch.dominantSpecies();
        } else {
        species = null;
        }

        changed |= placeVegetation(
                level,
                profile,
                random,
                new BlockPos(x, 0, z),
                species
        );
    }

    return changed;
}

private static boolean placeVegetation(
        WorldGenLevel level,
        VegetationProfile profile,
        RandomSource random,
        BlockPos requestedPos,
        PlantSpecies forcedSpecies
) {
    int x = requestedPos.getX();
    int z = requestedPos.getZ();

    int surfaceY = level.getHeight(
            Heightmap.Types.OCEAN_FLOOR_WG,
            x,
            z
    ) - 1;

    BlockPos groundPos =
            new BlockPos(x, surfaceY, z);

    BlockState groundState =
            level.getBlockState(groundPos);

    if (!isSuitableGround(groundState)) {
        return false;
    }

    BlockPos plantPos = groundPos.above();

    if (!level.getBlockState(plantPos).isAir()) {
        return false;
    }

    if (forcedSpecies == null
        && random.nextFloat() > profile.smallPlantChance()) {
        return false;
    }

        float shade =
                calculateShade(level, groundPos);

        float moisture =
                calculateMoisture(level, groundPos);

        float exposure =
                calculateExposure(level, groundPos);

    PlantSpecies species = forcedSpecies;

    if (species == null) {
        species = selectSpecies(
                profile.species(),
                shade,
                moisture,
                exposure,
                random
        );
    }

    if (species == null) {
        return false;
    }

BlockState plantState =
        species.block().defaultBlockState();

if (species.growthForm() == PlantGrowthForm.SINGLE_BLOCK) {

    if (!plantState.canSurvive(level, plantPos)) {
        return false;
    }

    return level.setBlock(
            plantPos,
            plantState,
            2
    );
}

// Double-block plants must use Minecraft's double-plant block.
if (!(species.block() instanceof DoublePlantBlock)) {
    return false;
}

BlockPos upperPos = plantPos.above();

// Both positions must be clear before placement.
if (!level.getBlockState(upperPos).isAir()) {
    return false;
}

BlockState lowerState = plantState.setValue(
        DoublePlantBlock.HALF,
        DoubleBlockHalf.LOWER
);

BlockState upperState = plantState.setValue(
        DoublePlantBlock.HALF,
        DoubleBlockHalf.UPPER
);

// Check the lower half against the ground first.
if (!lowerState.canSurvive(level, plantPos)) {
    return false;
}

// Place the lower half before checking the upper half.
if (!level.setBlock(plantPos, lowerState, 2)) {
    return false;
}

if (!upperState.canSurvive(level, upperPos)) {
    level.setBlock(
            plantPos,
            Blocks.AIR.defaultBlockState(),
            2
    );

    return false;
}

if (!level.setBlock(upperPos, upperState, 2)) {
    level.setBlock(
            plantPos,
            Blocks.AIR.defaultBlockState(),
            2
    );

    return false;
}

return true;
}

private static boolean placeSpreadingVegetation(
        WorldGenLevel level,
        VegetationProfile profile,
        RandomSource random,
        int startX,
        int startZ
) {
    boolean changed = false;

    for (int attempt = 0;
         attempt < SPREAD_ATTEMPTS;
         attempt++) {

        int targetX = startX + random.nextInt(16);
        int targetZ = startZ + random.nextInt(16);

        PlantSpecies sourceSpecies =
                findNearbySpecies(
                        level,
                        profile,
                        random,
                        targetX,
                        targetZ
                );

        if (sourceSpecies == null) {
            continue;
        }

        /*
         * Each species controls how readily it spreads.
         * Common grasses spread more readily than flowers.
         */
        if (random.nextFloat() > sourceSpecies.spreadChance()) {
            continue;
        }

        changed |= placeVegetation(
                level,
                profile,
                random,
                new BlockPos(targetX, 0, targetZ),
                sourceSpecies
        );
    }

    return changed;
}

private static PlantSpecies findNearbySpecies(
        WorldGenLevel level,
        VegetationProfile profile,
        RandomSource random,
        int targetX,
        int targetZ
) {
    for (int sample = 0; sample < 8; sample++) {

        int dx = random.nextInt(
                SPREAD_SEARCH_RADIUS * 2 + 1
        ) - SPREAD_SEARCH_RADIUS;

        int dz = random.nextInt(
                SPREAD_SEARCH_RADIUS * 2 + 1
        ) - SPREAD_SEARCH_RADIUS;

        if (dx == 0 && dz == 0) {
            continue;
        }

        int sourceX = targetX + dx;
        int sourceZ = targetZ + dz;

        int sourceY = level.getHeight(
                Heightmap.Types.OCEAN_FLOOR_WG,
                sourceX,
                sourceZ
        );

        BlockPos sourcePos =
                new BlockPos(
                        sourceX,
                        sourceY,
                        sourceZ
                );

        BlockState sourceState =
                level.getBlockState(sourcePos);

        PlantSpecies species =
                findSpeciesForBlock(
                        profile,
                        sourceState
                );

        if (species == null) {
            continue;
        }

        /*
         * Do not allow a plant to spread into a different
         * biome vegetation profile.
         */
        var biomeKey =
                level.getBiome(sourcePos).unwrapKey();

        if (biomeKey.isEmpty()) {
            continue;
        }

        VegetationProfile sourceProfile =
                BiomeVegetationProfiles.profileFor(
                        biomeKey.get()
                );

        if (sourceProfile != profile) {
            continue;
        }

        return species;
    }

    return null;
}

private static PlantSpecies findSpeciesForBlock(
        VegetationProfile profile,
        BlockState state
) {
    for (PlantSpecies species : profile.species()) {
        if (state.is(species.block())) {
            return species;
        }
    }

    return null;
}

private static float calculateExposure(
        WorldGenLevel level,
        BlockPos groundPos
) {
    int centerY = groundPos.getY();

    int higherSamples = 0;
    int totalSamples = 0;

    BlockPos.MutableBlockPos checkPos = groundPos.mutable();

    for (int dx = -TERRAIN_SCAN_RADIUS;
         dx <= TERRAIN_SCAN_RADIUS;
         dx++) {

        for (int dz = -TERRAIN_SCAN_RADIUS;
             dz <= TERRAIN_SCAN_RADIUS;
             dz++) {

            if (dx == 0 && dz == 0) {
                continue;
            }

            int x = groundPos.getX() + dx;
            int z = groundPos.getZ() + dz;

            int surfaceY = level.getHeight(
                    Heightmap.Types.OCEAN_FLOOR_WG,
                    x,
                    z
            ) - 1;

            checkPos.set(x, surfaceY, z);

            totalSamples++;

            if (surfaceY > centerY) {
                higherSamples++;
            }
        }
    }

    if (totalSamples == 0) {
        return 0.5F;
    }

    return higherSamples / (float) totalSamples;
}
}