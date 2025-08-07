import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PrecompiledHeader {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^#\\s*include \"([^\"]+)\"$");
    private static final boolean INCLUDE_INLINE_HEADERS = true;
    private static final String HOTSPOT_PATH = "src/hotspot";
    private static final String PRECOMPILED_HPP = "src/hotspot/share/precompiled/precompiled.hpp";

    private PrecompiledHeader() {
        throw new UnsupportedOperationException("Instances not allowed");
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || args.length > 2) {
            System.err.println("Usage: min_inclusion_count [jdk_root=.]");
            System.exit(1);
        }

        int minInclusionCount = Integer.parseInt(args[0]);
        Path jdkRoot = Path.of(args.length == 2 ? args[1] : ".").toAbsolutePath();
        if (!Files.isDirectory(jdkRoot)) {
            throw new IllegalArgumentException("jdk_root is not a directory: " + jdkRoot);
        }
        Path hotspotPath = jdkRoot.resolve(HOTSPOT_PATH);
        if (!Files.isDirectory(hotspotPath)) {
            throw new IllegalArgumentException("Invalid hotspot directory: " + hotspotPath);
        }

        Map<String, Integer> occurrences = new HashMap<>();
        try (Stream<Path> paths = Files.walk(hotspotPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith(".cpp") || !name.endsWith(".hpp");
                    })
                    .flatMap(path -> {
                        try {
                            return Files.lines(path);
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    })
                    .map(INCLUDE_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .forEach(include -> occurrences.compute(include, (k, old) -> Objects.requireNonNullElse(old, 0) + 1));
        }

        List<String> inlineIncludes = occurrences.keySet().stream()
                .filter(s -> s.endsWith(".inline.hpp"))
                .toList();
        if (INCLUDE_INLINE_HEADERS) {
            // Remove duplicates, if present
            inlineIncludes.stream()
                    .map(s -> s.replace(".inline.hpp", ".hpp"))
                    .forEach(occurrences::remove);
        } else {
            // Replace .inline.hpp include with the non-inline header, if it exists
            for (String include : inlineIncludes) {
                int inlineIncludeCount = occurrences.remove(include);
                String noInlineInclude = include.replace(".inline.hpp", ".hpp");
                if (!Files.exists(hotspotPath.resolve(noInlineInclude))) {
                    continue;
                }
                occurrences.compute(noInlineInclude, (k, c) -> inlineIncludeCount + Objects.requireNonNullElse(c, 0));
            }
        }

        String includes = occurrences.entrySet().stream()
                .filter(entry -> entry.getValue() > minInclusionCount)
                .map(Map.Entry::getKey)
                .sorted()
                .map(header -> String.format("#include \"%s\"", header))
                .collect(Collectors.joining(System.lineSeparator()));

        Path precompiledHpp = jdkRoot.resolve(PRECOMPILED_HPP);
        try (Stream<String> lines = Files.lines(precompiledHpp)) {
            String precompiledHppHeader = lines
                    .takeWhile(Predicate.not(s -> INCLUDE_PATTERN.matcher(s).matches()))
                    .collect(Collectors.joining(System.lineSeparator()));
            Files.write(precompiledHpp, precompiledHppHeader.getBytes());
        }
        Files.write(precompiledHpp, (System.lineSeparator() + includes + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

}
