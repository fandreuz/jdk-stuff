import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderAnalyzer {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^#\\s*include \"([^\"]+)\"$");
    private static final PathMatcher PATH_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.{cpp,hpp}");
    private static final boolean INCLUDE_INLINE = true;

    public static void main(String[] args) throws IOException {
        Path directory = Path.of(args[0]);
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Not a directory");
        }
        Path target = Path.of(args[1]);
        int minInclusion = Integer.parseInt(args[2]);

        Map<String, Integer> occurrences = new HashMap<>();
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!PATH_MATCHER.matches(file.getFileName())) return FileVisitResult.CONTINUE;

                for (String line : Files.readAllLines(file)) {
                    Matcher matcher = INCLUDE_PATTERN.matcher(line);
                    if (!matcher.find()) continue;

                    String header = matcher.group(1);
                    occurrences.compute(header, (k, old) -> old == null ? 1 : old + 1);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        if (!INCLUDE_INLINE) {
            List<String> inlineIncludes = occurrences.keySet().stream()
                    .filter(s -> s.endsWith(".inline.hpp"))
                    .toList();
            for (String include : inlineIncludes) {
                int count = occurrences.remove(include);
                String noInlineInclude = include.replace(".inline.hpp", ".hpp");
                if (!Files.exists(directory.resolve(noInlineInclude))) {
                    continue;
                }
                occurrences.compute(noInlineInclude, (k, c) -> count + Objects.requireNonNullElse(c, 0));
            }
        }

        List<String> lines = occurrences.entrySet().stream()
                .filter(entry -> entry.getValue() > minInclusion)
                .map(Map.Entry::getKey)
                .sorted()
                .map(header -> String.format("#include \"%s\"", header))
                .toList();
        Files.write(target, lines);
    }

}
