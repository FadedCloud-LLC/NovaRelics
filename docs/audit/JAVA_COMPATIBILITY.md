# Java Compatibility

## Runtime requirements (from Paper Fill v3 `java.version.minimum`, queried 2026-11-12)

| MC family | Minimum Java |
|---|---|
| 1.20 – 1.20.4 | 17 |
| 1.20.5 – 1.21.11 | 21 |
| 26.1 – 26.3 | 25 |

Spigot follows the same Mojang requirements: the server jar's own class-file version enforces them.

## Current build configuration

| Item | Value | Assessment |
|---|---|---|
| `maven.compiler.source/target` | 17 | Correct *target*. The oldest supported runtime (1.20.1) is Java 17, so one jar can serve every version. |
| `--release` flag | **not used** | ⚠ Compiling with JDK 21 and `-source/-target 17` does **not** stop code from calling Java 18–21 APIs (for example `List.getFirst()`), which would throw `NoSuchMethodError` on Java 17 servers. Use `<maven.compiler.release>17</maven.compiler.release>`. |
| Emitted bytecode | major 61 (Java 17) | Verified with `javap` on `target/classes/.../NovaRelics.class`. |
| Language features used | records: none; `switch` expressions, pattern `instanceof`, `Stream.toList()` (Java 16) | All are valid at Java 17. A scan found no Java 21-only APIs. |
| Shaded deps bytecode | HikariCP 5.0.1 (Java 11), sqlite-jdbc 3.43 (Java 8), bStats 3.2.1 (Java 8) | OK on 17. |
| Local JDKs available | Temurin 17.0.16, Temurin 21.0.12, Oracle 25.0.3, Oracle 26.0.2 | Enough to run every matrix tier locally. |

## Test-framework Java constraints

| Artifact | Bytecode | Implication |
|---|---|---|
| `com.github.seeseemelk:MockBukkit-v1.20:3.93.2` (Paper API 1.20.6) | Java 21 (major 65) | 1.20 API-generation tests must run on JDK 21. |
| `org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.116.3` (Paper API 1.21.11) | Java 21 (major 65) | JDK 21. |
| `org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.120.0` (Paper 26.3) | Java 25 (major 69) | JDK 25. |
| JUnit | MockBukkit 4.x is built against JUnit Jupiter 6.0.3 | Use the JUnit 6 BOM. |

**Consequence:** MockBukkit **cannot** prove Java 17 runtime compatibility, because no MockBukkit artifact runs on Java 17. Java 17 safety must come from:
1. `--release 17` at compile time, and
2. a real Paper/Spigot 1.20.1 server started on JDK 17 in CI.

## Recommendations
0. ✅ **Done in Phase 0:** `maven.compiler.release=17` is set in `pom.xml`. The MD5 of all 156 classes is identical before and after; CI asserts class major 61.
1. Switch to `maven.compiler.release=17` for `src/main`, and set the test compilation `release` per MockBukkit generation (Maven toolchains, with one Surefire execution per JDK).
2. Add the `maven-enforcer-plugin` `enforceBytecodeVersion` rule (extra-enforcer-rules) so that no shaded dependency above Java 17 slips in.
3. Keep a single production jar. No version-specific artifact is needed: every API difference found (see `API_COMPATIBILITY_AUDIT.md`) can be handled at runtime.
