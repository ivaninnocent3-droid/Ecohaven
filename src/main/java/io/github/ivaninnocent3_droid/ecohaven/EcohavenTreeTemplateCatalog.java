package io.github.ivaninnocent3_droid.ecohaven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.resources.Identifier;

/**
 * Discovers tree NBT templates during data generation.
 *
 * Naming convention:
 *     <family>_<number>.nbt
 *
 * Examples:
 *     oak_01.nbt
 *     oak_02.nbt
 *     birch_01.nbt
 *
 * This class is only used by the dynamic worldgen bootstrap during datagen.
 * No filesystem scanning happens during normal game runtime.
 */
public final class EcohavenTreeTemplateCatalog {

    private static final String TREE_RESOURCE_PATH =
            "src/main/resources/data/ecohaven/structure/trees";

    private static final Pattern TEMPLATE_PATTERN =
            Pattern.compile("^(.+)_([0-9]+)\\.nbt$");

    private EcohavenTreeTemplateCatalog() {}

    public record TreeTemplate(
            String family,
            String variant,
            Identifier template
    ) {}

    public static Map<String, List<TreeTemplate>> discover() {
        Path treeRoot = findTreeRoot();

        Map<String, List<TreeTemplate>> families = new LinkedHashMap<>();

        try (var paths = Files.list(treeRoot)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".nbt"))
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString()
                    ))
                    .forEach(path -> addTemplate(families, path));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not scan Ecohaven tree templates at "
                            + treeRoot.toAbsolutePath(),
                    exception
            );
        }

        if (families.isEmpty()) {
            throw new IllegalStateException(
                    "No Ecohaven tree templates were found in "
                            + treeRoot.toAbsolutePath()
            );
        }

        families.values().forEach(list ->
                list.sort(Comparator.comparingInt(template ->
                        variantNumber(template.variant())))
        );

        return Map.copyOf(families);
    }

    /**
     * Finds the project source-resource directory regardless of whether
     * datagen is launched from the project root or from run/datagen.
     */
    private static Path findTreeRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();

        while (current != null) {
            Path candidate = current.resolve(TREE_RESOURCE_PATH);

            if (Files.isDirectory(candidate)) {
                return candidate;
            }

            current = current.getParent();
        }

        throw new IllegalStateException(
                "Could not locate Ecohaven tree template directory. "
                        + "Expected to find '"
                        + TREE_RESOURCE_PATH
                        + "' above the datagen working directory."
        );
    }

    private static void addTemplate(
            Map<String, List<TreeTemplate>> families,
            Path path
    ) {
        String filename = path.getFileName().toString();
        Matcher matcher = TEMPLATE_PATTERN.matcher(filename);

        if (!matcher.matches()) {
            throw new IllegalStateException(
                    "Invalid Ecohaven tree template filename: " + filename
                            + ". Expected <family>_<number>.nbt"
            );
        }

        String family = matcher.group(1);
        String variant = matcher.group(2);

        if (family.isBlank()) {
            throw new IllegalStateException(
                    "Invalid Ecohaven tree family in filename: " + filename
            );
        }

        Identifier template = Ecohaven.id(
                "trees/" + filename.substring(
                        0,
                        filename.length() - ".nbt".length()
                )
        );

        families
                .computeIfAbsent(family, ignored -> new ArrayList<>())
                .add(new TreeTemplate(family, variant, template));
    }

    private static int variantNumber(String variant) {
        try {
            return Integer.parseInt(variant);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid numeric tree variant: " + variant,
                    exception
            );
        }
    }
}