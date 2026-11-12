# Research and Sources

All sources were queried on 2026-11-12. "Live API" means the data was fetched programmatically during the audit, not recalled.

| Source | Type | What it established | Why it is relevant |
|---|---|---|---|
| https://piston-meta.mojang.com/mc/game/version_manifest_v2.json | Live API (Mojang) | Latest release 26.3; full list of releases 1.20 → 26.3 with dates | Authoritative version list for the compatibility manifest |
| https://fill.papermc.io/v3/projects/paper | Live API (PaperMC) | Paper builds exist for every release except 1.20.3, 1.21.2 and 26.1 | Defines the Paper rows of the matrix |
| https://fill.papermc.io/v3/projects/paper/versions/{v} | Live API | Minimum Java: 17 (≤1.20.4), 21 (1.20.5–1.21.11), 25 (26.x) | Java toolchain per row |
| https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/latest | Live API | 26.3 build 249, STABLE, 2026-11-11 | Current build for smoke tests |
| https://hub.spigotmc.org/versions/ | Live index (SpigotMC) | BuildTools revisions for every release 1.20 → 26.3 | Defines the Spigot rows |
| https://www.spigotmc.org/wiki/buildtools/ | Docs | Spigot must be built locally; no redistribution | Harness design, CI caching |
| https://repo1.maven.org/maven2/org/mockbukkit/mockbukkit/ | Live Maven Central | mockbukkit-v26.3 4.120.0, v26.2 4.117.0, v1.21 4.116.3 | Test stack selection |
| MockBukkit jar manifests (downloaded to scratch) | Artifact inspection | v26.3 → Paper 26.3.build.200, class major 69 (Java 25); v1.21 → Paper 1.21.11, major 65 (Java 21); legacy MockBukkit-v1.20 3.93.2 → paper-api 1.20.6, major 65 | Proves no MockBukkit runs on Java 17 |
| https://github.com/MockBukkit/MockBukkit | Docs | Coordinates `org.mockbukkit.mockbukkit:mockbukkit-v26.3`; paper-api version read from the `Paper-Version` manifest attribute | Dependency setup |
| https://hub.spigotmc.org/jira/si/jira.issueviews:issue-html/SPIGOT-7652/SPIGOT-7652.html | Issue tracker | CraftBukkit `FieldRename`/`Commodore` remap legacy constants for old api-versions, and have had remap bugs | Basis for NR-016: renamed-constant compatibility relies on the server shim and must be runtime-verified |
| https://hub.spigotmc.org/jira/si/jira.issueviews:issue-html/SPIGOT-5658/SPIGOT-5658.html | Issue tracker | `api-version` causes bytecode modification of the plugin | Same as above |
| https://ayakael.net/mirrors/papermc/src/commit/36fa573f4856b3da17b87b84c78d55f49991fc3f/patches/server/Hook-into-CB-plugin-rewrites.patch | Paper patch mirror | Paper hooks into CB's plugin rewrites | The shim exists on Paper too |
| https://hub.spigotmc.org/jenkins/job/CraftBukkit-RSS/2955/changes | Changelog | `settings.compatibility.enum-compatibility-mode` in bukkit.yml | Server-side toggle to note in docs |
| https://papermc.io/news/important-dev-psa-future-removal-of-cb-package-relocation | PaperMC news | Paper is removing legacy compatibility layers over time | Reason not to depend on the rewrite shim long-term |
| https://docs.papermc.io/ | Docs | Paper API (Adventure native, `PlayerArmorChangeEvent`, `PlayerJumpEvent`, `getItemsToKeep`) | Paper adapter design |
| https://github.com/PaperMC/Paper | Source | Server implementation reference | Behaviour checks |

## Verified locally
- `mvn -B clean package` succeeds on JDK 21.0.12 / Maven 3.9.11.
- `javap` shows class major 61.
- The jar listing shows un-relocated `org/slf4j` and 62 native libraries.
- A scratch harness reproduced NR-009.

## Not yet researched (open items)
- An exact `FieldRename` table per version (`PotionEffectType`, `Particle`, `Attribute`). The real-server matrix will settle this empirically, which is more reliable than reading the table.
- A current CVE status for sqlite-jdbc 3.43.0.0 and HikariCP 5.0.1. The OWASP dependency-check job will report it.
