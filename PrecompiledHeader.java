import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PrecompiledHeader {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^#\\s*include \"([^\"]+)\"$");
    private static final String HOTSPOT_PATH = "src/hotspot";
    private static final String PRECOMPILED_HPP = "src/hotspot/share/precompiled/precompiled.hpp";
    private static final String INLINE_HPP_SUFFIX = ".inline.hpp";

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
                        return name.endsWith(".cpp") || name.endsWith(".hpp");
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
                .filter(s -> s.endsWith(INLINE_HPP_SUFFIX))
                .toList();
        // Each .inline.hpp implicitly pulls in the non-inline version too, so we can increase
        // its counter
        for (String inlineInclude : inlineIncludes) {
            int inlineCount = occurrences.get(inlineInclude);
            String noInlineInclude = inlineInclude.replace(INLINE_HPP_SUFFIX, ".hpp");
            int noInlineCount = Objects.requireNonNullElse(occurrences.get(noInlineInclude), 0);
            occurrences.put(noInlineInclude, inlineCount + noInlineCount);
        }

        Set<String> headers = occurrences.entrySet().stream()
                .filter(entry -> entry.getValue() > minInclusionCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        // If both inline and non-inline headers should be included, prefer the inline one
        for (String inlineInclude : inlineIncludes) {
            if (headers.contains(inlineInclude)) {
                String noInlineInclude = inlineInclude.replace(INLINE_HPP_SUFFIX, ".hpp");
                headers.remove(noInlineInclude);
            }
        }

        String includes = headers.stream()
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
