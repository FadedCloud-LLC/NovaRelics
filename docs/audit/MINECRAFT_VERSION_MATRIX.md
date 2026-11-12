# Minecraft Version Matrix

Discovery date: **2026-11-12**. Every row below comes from live queries of authoritative sources (see `RESEARCH_AND_SOURCES.md`), not from assumptions.

- **Mojang manifest:** latest release `26.3` (2026-09-15); latest snapshot `26.4-snapshot-8`, which is excluded.
- **Paper (Fill v3 API):** latest 26.3 build `249`, channel STABLE, built 2026-11-11.
- **Spigot (hub.spigotmc.org/versions):** BuildTools versions exist for every release from 1.20 to 26.3.

Machine-readable copy: [`compatibility-matrix.json`](compatibility-matrix.json).

## Compatibility manifest (every stable release in range)

| MC version | Released | Paper build available | Spigot BuildTools | Min Java (Paper metadata) | Current plugin version gate | MockBukkit artifact | Real server | Result |
|---|---|---|---|---|---|---|---|---|
| 1.20 | 2023-06-02 | yes | yes | 17 | **rejected** | — (none for API ≤1.20.4) | NOT TESTED | FAIL (static: version gate) |
| 1.20.1 | 2023-06-12 | yes | yes | 17 | **rejected** | — | NOT TESTED | FAIL (static) |
| 1.20.2 | 2023-09-20 | yes | yes | 17 | **rejected** | — | NOT TESTED | FAIL (static) |
| 1.20.3 | 2023-12-04 | **no Paper build** | yes | 17 | **rejected** | — | NOT TESTED | Paper: NOT APPLICABLE · Spigot: FAIL (static) |
| 1.20.4 | 2023-12-07 | yes | yes | 17 | **rejected** | — | NOT TESTED | FAIL (static) |
| 1.20.5 | 2024-04-23 | yes | yes | 21 | **rejected** | MockBukkit-v1.20 3.93.2 (Paper 1.20.6, Java 21) | NOT TESTED | FAIL (static) |
| 1.20.6 | 2024-04-29 | yes | yes | 21 | allowed | MockBukkit-v1.20 3.93.2 | NOT TESTED | Paper: NOT TESTED · Spigot: FAIL (static, NR-003) |
| 1.21 | 2024-06-13 | yes | yes | 21 | **rejected** | mockbukkit-v1.21 4.116.3 (Paper 1.21.11) | NOT TESTED | FAIL (static) |
| 1.21.1 | 2024-08-08 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.2 | 2024-10-22 | **no Paper build** | yes | 21 | **rejected** | ″ | NOT TESTED | Paper: NOT APPLICABLE · Spigot: FAIL (static) |
| 1.21.3 | 2024-10-23 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.4 | 2024-12-03 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.5 | 2025-03-25 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.6 | 2025-06-17 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.7 | 2025-06-30 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.8 | 2025-07-17 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.9 | 2025-09-30 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.10 | 2025-10-07 | yes | yes | 21 | **rejected** | ″ | NOT TESTED | FAIL (static) |
| 1.21.11 | 2025-12-09 | yes | yes | 21 | allowed | ″ | NOT TESTED | Paper: NOT TESTED · Spigot: FAIL (static, NR-003) |
| 26.1 | 2026-03-24 | **no Paper build** (Paper list: 26.1.1, 26.1.2) | yes | 25 | **rejected** | — | NOT TESTED | Paper: NOT APPLICABLE · Spigot: FAIL (static) |
| 26.1.1 | 2026-04-01 | yes | yes | 25 | **rejected** | — | NOT TESTED | FAIL (static) |
| 26.1.2 | 2026-04-09 | yes | yes | 25 | allowed | — | NOT TESTED | Paper: NOT TESTED · Spigot: FAIL (static, NR-003) |
| 26.2 | 2026-06-16 | yes | yes | 25 | **rejected** | mockbukkit-v26.2 4.117.0 | NOT TESTED | FAIL (static) |
| 26.3 | 2026-09-15 | yes (build 249 STABLE) | yes | 25 | **rejected** | mockbukkit-v26.3 4.120.0 (Paper 26.3 build 200, Java 25) | NOT TESTED | FAIL (static) |

### Status legend
- **FAIL (static)**: the code provably refuses to run on this version. The allowlist in `NovaRelics.java:27` (NR-002) calls `disablePlugin`. On Spigot, the missing Adventure library fails first (NR-003). Static proof is sufficient to mark FAIL; a real-server run will be added as regression evidence.
- **NOT TESTED**: the code is allowed to run, but no real-server run has happened. This is not a PASS.
- **NOT APPLICABLE**: the platform never published a build for this version.

### Summary
- **Versions currently validated as working:** none on a real server.
  - The MockBukkit behaviour suite (117 tests) passes on the 1.21.11 and 26.3 APIs, but only with the documented rename-agent test double and a server version the gate accepts.
  - Static linking passes only on Paper 1.20 – 1.20.4.
- **Versions provably broken today:** all Spigot versions, and Paper on 18 of 21 Paper-published versions.
- **Versions possibly working:** Paper 1.20.6, 1.21.11 and 26.1.2, which are allowed by the gate. NR-016 (renamed constants) and NR-015 (skull textures) still need a real-server run on each.

## Static link check (executed 2026-11-12)

`ApiCompatibilityTest -Papi-matrix` reads the production bytecode and resolves every referenced Bukkit/Paper/Adventure class, field and method against each published API jar. Full per-API reports are in [`test-results/api-compat/`](test-results/api-compat/).

| API range | Problems | Missing / incompatible symbols |
|---|---|---|
| Paper 1.20, 1.20.1, 1.20.2, 1.20.4 | **0** | — (links cleanly; still blocked by the NR-002 version gate at runtime) |
| Paper 1.20.5 – 1.21.1 | 6 | `PotionEffectType.CONFUSION/HARM/SLOW/SLOW_DIGGING`, `Particle.CRIT_MAGIC/SPELL_WITCH` |
| Paper 1.21.3 – 26.3 | 8 | the 6 above, plus `Attribute.GENERIC_MAX_HEALTH` and `Sound#valueOf` (Sound became an interface) |
| Spigot 1.20 – 1.20.4 | 9 | Adventure classes (4), `Player#sendMessage(Component)`, `ConsoleCommandSender#sendMessage(Component)`, `Player#sendActionBar(String)`, `Player#getTargetEntity`, `PlayerDeathEvent#setCancelled` |
| Spigot 1.20.5 – 1.21.2 | 15 | the 9 above, plus the 6 renamed constants |
| Spigot 1.21.3 – 26.3 | 17 | all of the above |

On Paper the renamed constants are *expected* to be patched at load time by the server's Commodore rewriting, because the plugin declares `api-version: 1.18`. Only a real-server run can confirm that.

The Spigot problems include missing **classes and methods**, which bytecode rewriting does not supply. Every Spigot row is therefore a confirmed static FAIL.

## Why real-server runs have not happened yet (BLOCKED items)
1. Starting a vanilla-derived server requires accepting the **Minecraft EULA** (`eula=true`). That is an agreement the server operator must accept, so it was not set automatically. **Approval is needed before the real-server harness runs.**
2. Spigot jars cannot be downloaded; they must be built locally with BuildTools (about 5–10 min per version). This is planned for the nightly CI matrix, not ad-hoc runs.
3. Docker is not installed locally, so the Testcontainers MySQL tests are deferred to CI. The SQLite tests can run locally.

## Proposed CI matrix tiers
| Tier | Rows |
|---|---|
| PR smoke | Paper 1.20.1 (Java 17), Paper 1.21.11 (Java 21), Paper 26.3 (Java 25), Spigot 1.21.11 |
| Nightly | Every row above with a published build (Paper ×21, Spigot ×24) |
| Release | Full nightly matrix must be green, with zero NOT TESTED rows for declared versions |
