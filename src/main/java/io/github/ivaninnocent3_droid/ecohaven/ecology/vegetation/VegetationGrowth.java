
package io.github.ivaninnocent3_droid.ecohaven.ecology.vegetation;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;

public final class VegetationGrowth {

    private static final int TICK_INTERVAL = 100;
    private static final int PLAYER_RADIUS = 8;
    private static final int SPREAD_RADIUS = 4;

    private static final int ATTEMPTS_PER_PLAYER = 2;

    private static final float SHORT_GRASS_GROWTH_CHANCE = 0.01F;

    private VegetationGrowth() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(
                VegetationGrowth::tick
        );
    }

    private static void tick(ServerLevel level) {

        if (level.getGameTime() % TICK_INTERVAL != 0) {
            return;
        }

        RandomSource random = level.getRandom();

        for (ServerPlayer player : level.players()) {

            BlockPos playerPos = player.blockPosition();

            for (int attempt = 0;
                 attempt < ATTEMPTS_PER_PLAYER;
                 attempt++) {

                int x = playerPos.getX()
                        + random.nextInt(PLAYER_RADIUS * 2 + 1)
                        - PLAYER_RADIUS;

                int z = playerPos.getZ()
                        + random.nextInt(PLAYER_RADIUS * 2 + 1)
                        - PLAYER_RADIUS;

                if (attempt == 0) {
                    tryGrowShortGrass(level, random, x, z);
                } else {
                    trySpreadPlant(level, random, x, z);
                }
            }
        }
    }

    private static VegetationProfile getProfile(
            ServerLevel level,
            BlockPos pos
    ) {
        var biomeKey = level.getBiome(pos).unwrapKey();

        if (biomeKey.isEmpty()) {
            return null;
        }

        return BiomeVegetationProfiles.profileFor(biomeKey.get());
    }

    private static boolean containsSpecies(
            VegetationProfile profile,
            PlantSpecies target
    ) {
        return profile.species().contains(target);
    }

    /*
     * SHORT GRASS GROWTH
     *
     * Short grass can establish naturally on existing grass blocks.
     * This is separate from the world-generation vegetation feature.
     */
    private static void tryGrowShortGrass(
            ServerLevel level,
            RandomSource random,
            int x,
            int z
    ) {
        int surfaceY = level.getHeight(
                Heightmap.Types.OCEAN_FLOOR,
                x,
                z
        ) - 1;

        BlockPos groundPos = new BlockPos(x, surfaceY, z);

        if (!level.getBlockState(groundPos).is(Blocks.GRASS_BLOCK)) {
            return;
        }

        BlockPos plantPos = groundPos.above();

        if (!level.getBlockState(plantPos).isAir()) {
            return;
        }

        VegetationProfile profile = getProfile(level, groundPos);

        if (profile == null
                || !containsSpecies(
                        profile,
                        PlantSpeciesRegistry.SHORT_GRASS
                )) {
            return;
        }

        // Require sufficient light and avoid heavily shaded locations.
        if (level.getMaxLocalRawBrightness(plantPos) < 9) {
            return;
        }

        if (calculateShade(level, groundPos) > 0.55F) {
            return;
        }

        if (random.nextFloat() >= SHORT_GRASS_GROWTH_CHANCE) {
            return;
        }

        BlockState grassState =
                Blocks.SHORT_GRASS.defaultBlockState();

        if (grassState.canSurvive(level, plantPos)) {
            level.setBlock(plantPos, grassState, 3);
        }
    }

    /*
     * NATURAL PLANT SPREADING
     *
     * Existing plants can spread into air above suitable ground
     * or replace vanilla leaf litter.
     */
    private static void trySpreadPlant(
            ServerLevel level,
            RandomSource random,
            int x,
            int z
    ) {
        int surfaceY = level.getHeight(
                Heightmap.Types.OCEAN_FLOOR,
                x,
                z
        ) - 1;

        BlockPos surfacePos = new BlockPos(x, surfaceY, z);

        VegetationProfile profile = getProfile(level, surfacePos);

        if (profile == null) {
            return;
        }

        PlantSpecies sourceSpecies = findNearbySpecies(
                level,
                profile,
                random,
                x,
                z
        );

        if (sourceSpecies == null) {
            return;
        }

        if (random.nextFloat() > sourceSpecies.spreadChance()) {
            return;
        }

        BlockPos plantPos;
        BlockState previousState;

        BlockState surfaceState = level.getBlockState(surfacePos);
        BlockState aboveState = level.getBlockState(surfacePos.above());

        /*
         * Handle both possible heightmap results:
         * - leaf litter is the surface block
         * - leaf litter is immediately above the reported surface
         */
        if (surfaceState.is(Blocks.LEAF_LITTER)) {

            plantPos = surfacePos;
            previousState = surfaceState;

        } else if (aboveState.is(Blocks.LEAF_LITTER)) {

            plantPos = surfacePos.above();
            previousState = aboveState;

        } else if (aboveState.isAir()) {

            plantPos = surfacePos.above();
            previousState = aboveState;

        } else {
            return;
        }

        if (level.getMaxLocalRawBrightness(plantPos) < 4) {
            return;
        }

        placeSpecies(
                level,
                sourceSpecies,
                plantPos,
                previousState
        );
    }

    private static PlantSpecies findNearbySpecies(
            ServerLevel level,
            VegetationProfile targetProfile,
            RandomSource random,
            int targetX,
            int targetZ
    ) {
        for (int sample = 0; sample < 8; sample++) {

            int dx = random.nextInt(SPREAD_RADIUS * 2 + 1)
                    - SPREAD_RADIUS;

            int dz = random.nextInt(SPREAD_RADIUS * 2 + 1)
                    - SPREAD_RADIUS;

            if (dx == 0 && dz == 0) {
                continue;
            }

            int x = targetX + dx;
            int z = targetZ + dz;

            int surfaceY = level.getHeight(
                    Heightmap.Types.OCEAN_FLOOR,
                    x,
                    z
            );

            BlockPos sourcePos = new BlockPos(x, surfaceY, z);

            PlantSpecies species = findSpecies(
                    targetProfile,
                    level.getBlockState(sourcePos)
            );

            if (species == null) {
                species = findSpecies(
                        targetProfile,
                        level.getBlockState(sourcePos.below())
                );
            }

            if (species == null) {
                continue;
            }

            VegetationProfile sourceProfile =
                    getProfile(level, sourcePos);

            if (sourceProfile != targetProfile) {
                continue;
            }

            return species;
        }

        return null;
    }

    private static PlantSpecies findSpecies(
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

    /*
     * Places a single-block or double-block species.
     * If double-block placement fails, the original litter state
     * is restored.
     */
    private static boolean placeSpecies(
            ServerLevel level,
            PlantSpecies species,
            BlockPos plantPos,
            BlockState previousState
    ) {
        BlockState plantState =
                species.block().defaultBlockState();

        if (species.growthForm() == PlantGrowthForm.SINGLE_BLOCK) {

            if (!plantState.canSurvive(level, plantPos)) {
                return false;
            }

            return level.setBlock(
                    plantPos,
                    plantState,
                    3
            );
        }

        if (!(species.block() instanceof DoublePlantBlock)) {
            return false;
        }

        BlockPos upperPos = plantPos.above();

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

        if (!lowerState.canSurvive(level, plantPos)) {
            return false;
        }

        if (!level.setBlock(plantPos, lowerState, 3)) {
            return false;
        }

        if (!upperState.canSurvive(level, upperPos)
                || !level.setBlock(upperPos, upperState, 3)) {

            level.setBlock(
                    plantPos,
                    previousState,
                    3
            );

            return false;
        }

        return true;
    }

    private static float calculateShade(
            ServerLevel level,
            BlockPos groundPos
    ) {
        BlockPos.MutableBlockPos checkPos = groundPos.mutable();

        int shade = 0;

        for (int y = 1; y <= 8; y++) {

            checkPos.set(
                    groundPos.getX(),
                    groundPos.getY() + y,
                    groundPos.getZ()
            );

            BlockState state = level.getBlockState(checkPos);

            if (state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                shade += 2;
            } else if (state.isSolid()) {
                shade += 2;
            }
        }

        return Math.min(1.0F, shade / 8.0F);
    }
}
