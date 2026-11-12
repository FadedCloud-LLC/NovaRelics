package dev.aponder.novarelics.compat;

import dev.aponder.novarelics.support.api.ApiSurface;
import dev.aponder.novarelics.support.api.PluginReferences;
import dev.aponder.novarelics.support.api.PluginReferences.Ref;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Static link-time compatibility check of the production bytecode against real
 * Spigot and Paper API jars (regression for NR-003 and NR-016).
 *
 * <p>It answers: "which referenced classes, fields and methods do not exist on this
 * platform/version?" — exactly the symbols that would throw NoClassDefFoundError,
 * NoSuchFieldError, NoSuchMethodError or IncompatibleClassChangeError at runtime,
 * <em>unless</em> the server's legacy bytecode rewriting (CraftBukkit Commodore,
 * driven by plugin.yml api-version) happens to cover them. Passing here is therefore
 * stronger than needed; failing does not prove a real-server crash, but every listed
 * symbol must be either fixed or covered by a real-server test.
 *
 * <p>API jars come from testing/target/api-jars (populated by maven-dependency-plugin;
 * the full Spigot+Paper matrix with -Papi-matrix). Reports: testing/target/api-compat/.
 */
class ApiCompatibilityTest {

    private static final Path JAR_DIR = Path.of("target", "api-jars");
    private static final Path REPORT_DIR = Path.of("target", "api-compat");
    private static List<Ref> refs;

    @BeforeAll
    static void scan() throws IOException {
        refs = PluginReferences.scan();
        Files.createDirectories(REPORT_DIR);
    }

    @Test
    void scannerFindsPlatformReferences() {
        // Guards the scanner itself: the plugin obviously uses JavaPlugin and Player.
        assertTrue(refs.stream().anyMatch(r -> r.owner().equals("org/bukkit/plugin/java/JavaPlugin")));
        assertTrue(refs.stream().anyMatch(r -> r.owner().equals("org/bukkit/entity/Player")));
    }

    static Stream<String> apiJars() throws IOException {
        if (!Files.isDirectory(JAR_DIR)) return Stream.empty();
        try (Stream<Path> s = Files.list(JAR_DIR)) {
            return s.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".jar")).sorted().toList().stream();
        }
    }

    @ParameterizedTest(name = "NR-003/NR-016 links against {0}")
    @Tag("regression")
    @MethodSource("apiJars")
    void allReferencedSymbolsExist(String jarName) throws IOException {
        String label = jarName.replace(".jar", "");
        boolean paper = jarName.startsWith("paper-api");
        try (ApiSurface api = ApiSurface.ofJars(label, List.of(JAR_DIR.resolve(jarName)))) {
            if (paper) api.withClasspathAdventure();
            check(api);
        }
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-016 links against the paper-api MockBukkit runs on")
    void allReferencedSymbolsExistOnMockBukkitApi() throws IOException {
        try (ApiSurface api = ApiSurface.ofClasspath("classpath-paper-api-" + System.getProperty("test.api.generation", "?"))) {
            check(api);
        }
    }

    private void check(ApiSurface api) throws IOException {
        SortedMap<String, SortedSet<String>> problems = PluginReferences.problems(api, refs);
        StringBuilder sb = new StringBuilder();
        sb.append("API surface: ").append(api.name()).append('\n')
          .append("Referenced platform symbols: ").append(refs.size()).append('\n')
          .append("Problems: ").append(problems.size()).append("\n\n");
        problems.forEach((problem, users) -> {
            sb.append(problem).append('\n');
            users.forEach(u -> sb.append("        used in ").append(u).append('\n'));
        });
        Files.writeString(REPORT_DIR.resolve(api.name() + ".txt"), sb.toString());
        assertTrue(problems.isEmpty(), sb::toString);
    }
}
