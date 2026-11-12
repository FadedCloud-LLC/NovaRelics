# Dependency Analysis

| Dependency | Declared | Scope | Shaded / relocated | Assessment | Recommendation |
|---|---|---|---|---|---|
| paper-api | 1.18.2-R0.1-SNAPSHOT | provided | — | Below the supported floor (1.20) and Paper-only | Core: `spigot-api 1.20.1-R0.1-SNAPSHOT`, the oldest supported, to guarantee binary compatibility at the floor. Paper adapter: `paper-api 1.20.6` optional. CI additionally *compiles* against the latest Paper API to catch removals early. |
| VaultAPI | 1.7.1 (JitPack) | provided | — | Fine | Keep. JitPack availability is a CI risk, so add a fallback repository or cache. |
| PlaceholderAPI | 2.11.5 | provided | — | Fine (2.11.6+ available) | Bump when convenient |
| LuckPerms API | 5.4 | provided | — | **Unused** (hook never consulted) | Remove the hook, or implement group-based features |
| adventure-api / minimessage / legacy | 4.17.0 | provided | no | Breaks Spigot (NR-003) | `compile` + relocate to `dev.aponder.novarelics.libs.kyori`. Paper users are unaffected because the relocated copy is private. |
| bStats | 3.2.1 | compile | yes → `libs.bstats` | Correct | Keep |
| HikariCP | 5.0.1 | compile | yes → `libs.hikari` | OK (6.x available, Java 11+) | Bump to 6.x |
| slf4j-api (transitive of Hikari) | 2.0.x | compile | **no** | Un-relocated `org.slf4j` in the jar can clash with other plugins or the server's slf4j | Exclude it (the server provides slf4j) or relocate it together with Hikari |
| sqlite-jdbc | 3.43.0.0 | compile | no (natives cannot be relocated easily) | 13 MB of natives; outdated; Spigot and Paper both bundle `org.sqlite` already | Mark as `provided` and rely on the server's driver. Optionally use `libraries:` in plugin.yml to pin a version. |
| MySQL driver | not declared | — | — | Relies on the server-bundled `com.mysql.cj.jdbc.Driver` (present in Spigot/Paper) | Keep, but document it. Add a MariaDB option. |
| log4j-core | 2.17.1 | provided | — | Used only to silence Hikari logging; present on both platforms | Keep, or replace with slf4j/JUL level config |

## Test dependencies actually added (Phase 0, `testing/pom.xml`)
- mockbukkit-v1.21 4.116.3 / mockbukkit-v26.3 4.120.0
- paper-api 1.21.11-R0.1-SNAPSHOT / 26.3.build.200-stable
- JUnit BOM 6.0.3 (junit-jupiter, junit-platform-launcher)
- Mockito 5.24.0
- ArchUnit 1.5.1 (library)
- ASM-tree 9.11.1
- JaCoCo 0.8.16
- Surefire 3.6.1
- maven-dependency-plugin 3.9.0 (API jars)
- The plugin's runtime libraries mirrored at test scope: bStats 3.2.1, HikariCP 5.0.1, sqlite-jdbc 3.43.0.0, log4j-core 2.24.3, VaultAPI 1.7.1, LuckPerms API 5.4

No production dependency was changed. jqwik, Testcontainers and PIT are not yet added.

## Proposed test-scope additions (original plan)

| Artifact | Version | Purpose |
|---|---|---|
| org.mockbukkit.mockbukkit:mockbukkit-v26.3 | 4.120.0 | Primary MockBukkit (JDK 25) |
| org.mockbukkit.mockbukkit:mockbukkit-v1.21 | 4.116.3 | 1.21 generation (JDK 21) |
| com.github.seeseemelk:MockBukkit-v1.20 | 3.93.2 | 1.20 smoke (JDK 21) |
| org.junit:junit-bom | 6.0.x | JUnit Jupiter |
| org.mockito:mockito-junit-jupiter | 5.x | Vault/PAPI/JDBC doubles |
| net.jqwik:jqwik | 1.9.x | Property-based tests |
| org.testcontainers:mysql | 1.21.x | MySQL integration (CI) |
| com.tngtech.archunit:archunit-junit5 | 1.4.x | Enforce "no Paper/Adventure imports in core" |
| jacoco-maven-plugin / pitest-maven | 0.8.13+ / 1.20+ | Coverage / mutation |

Exact patch versions will be resolved from Maven Central when they are added, and recorded in the CHANGELOG.
