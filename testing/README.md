# NovaRelics tests

The automated test suite for NovaRelics. It uses MockBukkit, JUnit 6, Mockito and ArchUnit, plus a static bytecode-vs-API compatibility checker.

## Why a separate Maven project?

The plugin is compiled against `paper-api 1.18.2`, but MockBukkit needs a modern `paper-api` on the classpath. Maven cannot hold two versions of one artifact in a single module.

This project therefore runs against the **unchanged production classes** in `../target/classes` (configured as its output directory). The bytecode under test is byte-for-byte what ships, and JaCoCo reports coverage of the real classes.

## Running

```bash
# from the repository root
mvn -B package -DskipTests                                  # build the plugin first

mvn -B -f testing/pom.xml verify                             # MockBukkit 1.21 (JDK 21) – default
mvn -B -f testing/pom.xml clean verify -Pmb-26.3             # MockBukkit 26.3 (needs JDK 25)

mvn -B -f testing/pom.xml verify -DexcludedGroups=regression # behaviour tests only (CI gate)
mvn -B -f testing/pom.xml test -Dgroups=regression           # open-finding regression tests
mvn -B -f testing/pom.xml clean test -Papi-matrix -Dtest=ApiCompatibilityTest   # all 44 Spigot/Paper APIs
```

Each profile compiles tests into its own `target/test-classes-<generation>`, so switching profiles needs no `clean`.

## Results

| Output | Location |
|---|---|
| JUnit XML | `target/surefire-reports/` |
| Coverage (HTML / CSV) | `target/site/jacoco/` |
| API link reports | `target/api-compat/<api>.txt` |

## Test categories

| Tag | Meaning |
|---|---|
| *(none)* | Behaviour tests. They must pass, and form the CI gate. |
| `regression` | Each one reproduces a confirmed audit finding (`NR-xxx` in the display name) and **fails until that finding is fixed**. When fixed, remove the tag so the test joins the gate. Never delete or weaken one to get a green build. |
| `control` | Positive control next to a regression test, proving the test harness can observe the behaviour. |

## Test doubles you should know about

| Double | Why it exists |
|---|---|
| `support/LegacyRenameAgent` | Real servers rewrite old plugins' bytecode (CraftBukkit *Commodore*), so renamed constants such as `PotionEffectType.CONFUSION → NAUSEA` still link. MockBukkit does not, and the plugin would not even enable. The agent applies **only** the 7 renames plus 1 enum→interface fix that `ApiCompatibilityTest` reports. It is not evidence of real-server behaviour (finding NR-016). Disable it with `-Dnovarelics.legacyRename=false`. |
| `support/CompatServerMock` | Reports Bukkit version `1.21.11`, which passes the plugin's version allowlist. This lets behaviour be tested on the 26.3 API too. The allowlist itself is tested in `compat/VersionGateTest` (NR-002). |
| Mockito `Block` in `ExploitRegressionTest` | MockBukkit's `Block#getDrops` always returns an empty list. |
| Mockito `InventoryClickEvent` in `TriggerTest` | MockBukkit's own player inventory view has no top inventory; a real server has a `CRAFTING` one. |
