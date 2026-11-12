package dev.aponder.novarelics.compat;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.support.CompatServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NR-002: the plugin must enable on every stable release in the supported range.
 * The list is the compatibility manifest (docs/audit/MINECRAFT_VERSION_MATRIX.md).
 */
@Tag("regression")
class VersionGateTest {

    @AfterEach
    void tearDown() { MockBukkit.unmock(); }

    @ParameterizedTest(name = "NR-002 enables on {0}")
    @ValueSource(strings = {"1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
            "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9",
            "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3"})
    @DisplayName("NR-002 version allowlist")
    void enablesOnSupportedVersion(String version) {
        MockBukkit.mock(new CompatServerMock(version + "-R0.1-SNAPSHOT"));
        NovaRelics plugin = MockBukkit.load(NovaRelics.class);
        assertTrue(plugin.isEnabled(), "disabled itself on " + version);
    }

    @Test
    @DisplayName("NR-002 enables on the version MockBukkit actually reports")
    void enablesOnMockBukkitReportedVersion() {
        var server = MockBukkit.mock();
        NovaRelics plugin = MockBukkit.load(NovaRelics.class);
        assertTrue(plugin.isEnabled(), "disabled itself on " + server.getBukkitVersion());
    }
}
