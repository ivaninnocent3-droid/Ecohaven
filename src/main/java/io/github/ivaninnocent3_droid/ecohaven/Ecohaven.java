
package io.github.ivaninnocent3_droid.ecohaven;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ecohaven implements ModInitializer {
    public static final String MOD_ID = "ecohaven";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Ecohaven initialised.");

        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                EcohavenWorldPlacedFeatures.TALL_OAK_TREE
        );
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
