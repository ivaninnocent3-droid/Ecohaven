package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import java.util.List;

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

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
        private static final int MIN_PATCH_RADIUS = 6;
        private static final int MAX_PATCH_RADIUS = 10;

        private static final int MIN_PATCH_ATTEMPTS = 18;
        private static final int MAX_PATCH_ATTEMPTS = 32;

        private static final int MAX_PATCH_SPECIES = 3;

        private static final float PATCH_DOMINANCE = 0.68F;

        /*
        * Flowers within one cluster should not sit directly next to
        * one another. This is deliberately a soft spacing rule rather
        * than a fixed grid.
        */
        private static final int MIN_FLOWER_SPACING_SQUARED = 6;

        private static final int CANOPY_SCAN_HEIGHT = 12;
        private static final int MOISTURE_SCAN_RADIUS = 4;
        private static final int TERRAIN_SCAN_RADIUS = 2;

        private static final int SPREAD_ATTEMPTS = 6;
        private static final int SPREAD_SEARCH_RADIUS = 8;

    public VegetationFeature() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.FEATURE_TYPE,
                Ecohaven.id("vegetation"),
                CODEC
        );
    }

private static boolean placePatch(
        WorldGenLevel level,
        VegetationProfile profile,
        VegetationPatch patch,
        RandomSource random,
        int startX,
        int startZ
) {
    if (patch == null) {
        return false;
    }
    int centerX =
            startX + random.nextInt(16);

    int centerZ =
            startZ + random.nextInt(16);

    int attempts =
            MIN_PATCH_ATTEMPTS
            + random.nextInt(
                    MAX_PATCH_ATTEMPTS - MIN_PATCH_ATTEMPTS + 1
            );

    /*
     * Keep track of successful flower positions so the patch
     * develops visible breathing room.
     */
    java.util.ArrayList<BlockPos> placedFlowers =
            new java.util.ArrayList<>();

    boolean changed = false;

    for (int attempt = 0;
         attempt < attempts;
         attempt++) {

        int dx =
                random.nextInt(
                        patch.radius() * 2 + 1
                ) - patch.radius();

        int dz =
                random.nextInt(
                        patch.radius() * 2 + 1
                ) - patch.radius();

        if (dx * dx + dz * dz
                > patch.radius() * patch.radius()) {
            continue;
        }

        int x = centerX + dx;
        int z = centerZ + dz;

        int surfaceY =
                level.getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG,
                        x,
                        z
                ) - 1;

        BlockPos groundPos =
                new BlockPos(x, surfaceY, z);

        /*
         * Check spacing before doing the more expensive
         * ecological calculations.
         */
        boolean tooClose = false;

        for (BlockPos existing : placedFlowers) {

            int distanceX =
                    existing.getX() - x;

            int distanceZ =
                    existing.getZ() - z;

            if (distanceX * distanceX
                    + distanceZ * distanceZ
                    < MIN_FLOWER_SPACING_SQUARED) {

                tooClose = true;
                break;
            }
        }

        if (tooClose) {
            continue;
        }

        /*
         * Recalculate environmental conditions at the actual
         * placement position.
         *
         * This prevents a flower selected at the cluster centre
         * from being forced into an unsuitable part of the patch.
         */
        float shade =
                calculateShade(level, groundPos);

        float moisture =
                calculateMoisture(level, groundPos);

        float exposure =
                calculateExposure(level, groundPos);

        PlantSpecies species;

        if (random.nextFloat() < patch.dominance()) {

        PlantSpecies dominant = patch.dominantSpecies();

        // The dominant flower must still be suitable at this exact position.
        if (ecologicalWeight(
                dominant,
                shade,
                moisture,
                exposure
        ) > 0) {
                species = dominant;
        } else {
                // If the dominant flower does not fit here, choose
                // another suitable flower from the patch palette.
                species = selectSpecies(
                        patch.palette(),
                        shade,
                        moisture,
                        exposure,
                        random
                );
        }

        } else {

        /*
        * Secondary flowers are chosen ONLY from the small
        * environmentally appropriate cluster palette.
        */
        species = selectSpecies(
                patch.palette(),
                shade,
                moisture,
                exposure,
                random
        );
        }

        if (species == null) {
            continue;
        }

        boolean placed =
                placeVegetation(
                        level,
                        profile,
                        random,
                        groundPos,
                        species
                );

        if (placed) {
            placedFlowers.add(
                    groundPos.above()
            );

            changed = true;
        }
    }

    return changed;
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
        if (random.nextFloat() < profile.dominantPatchChance() * 0.5F) {

int patchCenterX =
        startX + random.nextInt(16);

int patchCenterZ =
        startZ + random.nextInt(16);

VegetationPatch patch =
        createPatch(
                level,
                profile,
                random,
                patchCenterX,
                patchCenterZ
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
            } else if (state.getLightDampening() >= 15) {
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

    for (PlantSpecies plant : species) {

        int weight = ecologicalWeight(
                plant,
                shade,
                moisture,
                exposure
        );

        totalWeight += weight;
    }

    if (totalWeight <= 0) {
        return null;
    }

    int selection =
            random.nextInt(totalWeight);

    for (PlantSpecies plant : species) {

        int weight = ecologicalWeight(
                plant,
                shade,
                moisture,
                exposure
        );

        if (weight <= 0) {
            continue;
        }

        if (selection < weight) {
            return plant;
        }

        selection -= weight;
    }

    return null;
}

private static int ecologicalWeight(
        PlantSpecies plant,
        float shade,
        float moisture,
        float exposure
) {
    float shadeMatch =
            preferenceMatch(
                    shade,
                    plant.shadeTolerance()
            );

    float moistureMatch =
            preferenceMatch(
                    moisture,
                    plant.moisturePreference()
            );

    float exposureMatch =
            preferenceMatch(
                    exposure,
                    plant.exposurePreference()
            );

    float roleMatch =
            roleSuitability(
                    plant.role(),
                    shade
            );

    float suitability =
            shadeMatch
            * moistureMatch
            * exposureMatch
            * roleMatch;

    if (suitability <= 0.0F) {
        return 0;
    }

    return Math.max(
            1,
            Math.round(
                    plant.weight() * suitability
            )
    );
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
        WorldGenLevel level,
        VegetationProfile profile,
        RandomSource random,
        int centerX,
        int centerZ
) {
    int centerY = level.getHeight(
            Heightmap.Types.OCEAN_FLOOR_WG,
            centerX,
            centerZ
    ) - 1;

    BlockPos centerGround =
            new BlockPos(centerX, centerY, centerZ);

    BlockState centerGroundState =
            level.getBlockState(centerGround);

    if (!isSuitableGround(centerGroundState)) {
        return null;
    }

    /*
     * IMPORTANT:
     *
     * Environmental conditions are calculated BEFORE choosing
     * any flower species.
     */
    float shade =
            calculateShade(level, centerGround);

    float moisture =
            calculateMoisture(level, centerGround);

    float exposure =
            calculateExposure(level, centerGround);

    List<PlantSpecies> viableFlowers =
            profile.species()
                    .stream()
                    .filter(VegetationFeature::isFlowerSpecies)
                    .filter(species ->
                            ecologicalWeight(
                                    species,
                                    shade,
                                    moisture,
                                    exposure
                            ) > 0.0F
                    )
                    .toList();

    if (viableFlowers.isEmpty()) {
        return null;
    }

    /*
     * Choose the dominant flower from the environmentally
     * suitable species.
     */
    PlantSpecies dominant =
            selectSpecies(
                    viableFlowers,
                    shade,
                    moisture,
                    exposure,
                    random
            );

    if (dominant == null) {
        return null;
    }

    /*
     * Build a deliberately small palette.
     *
     * The first species is always the dominant one. Additional
     * species are selected from the same environmentally viable
     * pool, never from the entire biome flower list.
     */
    java.util.ArrayList<PlantSpecies> palette =
            new java.util.ArrayList<>();

    palette.add(dominant);

    int paletteSize =
            1 + random.nextInt(
                    Math.min(
                            MAX_PATCH_SPECIES,
                            viableFlowers.size()
                    )
            );

    while (palette.size() < paletteSize) {

        PlantSpecies candidate =
                selectSpecies(
                        viableFlowers,
                        shade,
                        moisture,
                        exposure,
                        random
                );

        if (candidate == null) {
            break;
        }

        if (!palette.contains(candidate)) {
            palette.add(candidate);
        }
    }

    int radius =
            MIN_PATCH_RADIUS
            + random.nextInt(
                    MAX_PATCH_RADIUS - MIN_PATCH_RADIUS + 1
            );

    return new VegetationPatch(
            palette,
            dominant,
            radius,
            PATCH_DOMINANCE
    );
}

private static boolean isFlowerSpecies(
        PlantSpecies species
) {
    return species.role() == PlantRole.SPECIAL;
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

// Flowers are intentionally less common than grass and ferns.
if (forcedSpecies == null
        && species.role() == PlantRole.SPECIAL
        && random.nextFloat() < 0.5F) {
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