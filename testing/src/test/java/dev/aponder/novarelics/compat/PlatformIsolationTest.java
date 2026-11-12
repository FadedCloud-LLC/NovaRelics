package dev.aponder.novarelics.compat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import dev.aponder.novarelics.support.api.PluginReferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * NR-003: shared code must be loadable on Spigot. Paper-only types belong in an isolated
 * adapter package (proposed: dev.aponder.novarelics.platform.paper) and Adventure must be
 * shaded+relocated (dev.aponder.novarelics.libs.kyori) rather than referenced directly.
 */
@Tag("regression")
class PlatformIsolationTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importProduction() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPath(PluginReferences.productionClasses());
    }

    @Test
    @DisplayName("NR-003 core code does not use Paper-only API packages")
    void coreDoesNotDependOnPaperPackages() {
        ArchRule rule = noClasses().that().resideInAPackage("dev.aponder.novarelics..")
                .and().resideOutsideOfPackage("dev.aponder.novarelics.platform.paper..")
                .should().dependOnClassesThat().resideInAnyPackage("io.papermc..", "com.destroystokyo..");
        rule.check(classes);
    }

    @Test
    @DisplayName("NR-003 core code does not use un-relocated Adventure (absent on Spigot)")
    void coreDoesNotDependOnUnrelocatedAdventure() {
        ArchRule rule = noClasses().that().resideInAPackage("dev.aponder.novarelics..")
                .and().resideOutsideOfPackage("dev.aponder.novarelics.platform.paper..")
                .should().dependOnClassesThat().resideInAPackage("net.kyori..");
        rule.check(classes);
    }
}
