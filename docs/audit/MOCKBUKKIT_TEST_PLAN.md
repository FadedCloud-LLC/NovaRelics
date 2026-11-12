# MockBukkit Test Plan

**Status: Phase 0 IMPLEMENTED (2026-11-12).** There are 191 tests in `testing/`; results are in `MOCKBUKKIT_TEST_RESULTS.md`.

Deviations from the original plan, and why:
- **Separate `testing/` Maven project instead of `src/test`.** The plugin compiles against paper-api 1.18.2 and MockBukkit needs 1.21/26.3, and one Maven module cannot hold both. Tests run against the unchanged `target/classes`.
- **Default profile is `mb-1.21` (JDK 21)**, with `mb-26.3` (JDK 25) selected explicitly. The default matches the JDK most contributors have.
- **No 1.20 MockBukkit run.** The legacy `MockBukkit-v1.20` uses a different package; the 1.20 range is covered by the static API link check instead.
- **jqwik not added**: its JUnit Platform 6 compatibility is unverified. Round-trip coverage uses all 15 bundled relics as fixtures instead. **PIT** is deferred to Phase 1.
- **ArchUnit** is used as a plain library, not the JUnit 5 engine, to avoid platform-version conflicts.
- **Added:** `ApiCompatibilityTest` (static bytecode link check), `LegacyRenameAgent` (Commodore test double), `CompatServerMock` (version-gate bypass for behaviour tests).

The plan below is kept for traceability. Rows not yet implemented are listed in `TEST_COVERAGE_REPORT.md` ("Gap plan").

## Stack

| Component | Version (verified on Maven Central, 2026-11-12) | JDK |
|---|---|---|
| `org.mockbukkit.mockbukkit:mockbukkit-v26.3` | 4.120.0 (Paper 26.3 build 200) | 25 |
| `org.mockbukkit.mockbukkit:mockbukkit-v1.21` | 4.116.3 (Paper 1.21.11) | 21 |
| `com.github.seeseemelk:MockBukkit-v1.20` | 3.93.2 (Paper 1.20.6), legacy group, no longer patched | 21 |
| JUnit Jupiter | 6.0.x (what MockBukkit 4.x is built against) | — |
| Mockito | 5.x (only for Vault `Economy`, PAPI, and JDBC failure injection) | — |
| JaCoCo | 0.8.13+ (Java 25 support) | — |
| PIT | 1.20+ with `pitest-junit5-plugin` | — |
| jqwik | 1.9 (property tests for parsers) | — |

### Execution layout
One Maven profile per API generation, each run by Surefire on its own toolchain:
- `mb-26` is the default (JDK 25).
- `mb-121` uses JDK 21.
- `mb-120` uses JDK 21 and is a smoke subset only, because the legacy artifact is unmaintained.

The same test sources serve every profile. Tests that rely on behaviour existing only in one generation are tagged (for example `@Tag("api-1.21+")`) and *excluded with a recorded reason*, never silently skipped.

## Test source structure

```
src/test/java/dev/aponder/novarelics/
├── support/        PluginTestBase (MockBukkit.mock + load), RelicFixtures, YamlFixtures, TestEconomy
├── lifecycle/      enable/disable, repeated cycles, invalid config, storage failure
├── commands/       one class per sub-command + root/tab-complete
├── permissions/    matrix test across every sub-command and relic.permission
├── configuration/  ConfigManager, language fallback, rarities, RelicLoader round-trip
├── items/          RelicBuilder, PDC, glow, flags, enchant parsing, MiniMsg/legacy
├── triggers/       one class per listener (event simulation)
├── abilities/      one class per ability family
├── cooldown/       memory, shared, global, relog, reduction
├── auras/          scheduler-driven rendering decisions
├── gui/            editor/list click, drag, close, chat input
├── storage/        SQLite temp-dir provider tests; MySQL via Testcontainers (CI only)
├── security/       path traversal, dupe, protection bypass, economy bypass
└── regression/     one test per NR-xxx, written failing first
```

## Coverage by subsystem

| Subsystem | Example cases (★ = written as a failing regression test first) |
|---|---|
| Lifecycle | enables on mocked server (★NR-002 for each MockBukkit generation); disable cancels aura task (`server.getScheduler().getPendingTasks()` empty); enable→disable→enable ×5 leaves exactly 22 trigger listeners registered; corrupt `config.yml` still enables with defaults; storage init failure → plugin disables cleanly (★NR-024) |
| Commands | each sub-command: no args → usage; unknown player/relic; console vs player (`editor` player-only); `give` amount 0/-1/abc/2147483647 (★NR-022); `give` full inventory → overflow dropped (★); `take` stack of 5 → removes 1 (★); `create` id validation `../x`, `a/b`, 65 chars (★NR-012); tab completion filtered by permission |
| Permissions | every sub-command × {op, explicit node, `novarelics.admin` child inheritance, no perm, console}; relic `permission:` blocks dispatch; runtime `addAttachment` / remove |
| Configuration | missing/empty/invalid YAML files; wrong types (`radius: "abc"`); negative cooldown; unknown trigger/ability/material/enchant names → warning, not crash; language fallback es→en for missing keys; `<prefix>` replacement; ★RelicLoader **round-trip property test** (load → save → load equal, NR-008) |
| Items | PDC id set / read; non-relic & air → null; glow adds flag; `custom-model-data`; enchant names (legacy + key) per generation; ★MiniMsg hex retention (NR-009), ★`&` legacy codes |
| Triggers | right/left/shift click mapping; off-hand ignored; cancelled interact ignored (★NR-030); break/place priorities; damage dealt vs received; kill mob vs player; death/respawn; fish states; sneak only on start; jump detection; world change; ★armor equip via player inventory (NR-017); ★command trigger fires once (NR-018) |
| Abilities | every one of the 50: success path + "wrong event type → false" + boundary params (0, negative, huge); Vault-dependent abilities with mock economy present/absent/insufficient; ★DoubleDrops on self-dropping blocks (NR-001); ★FullRepair insufficient money cancels click (NR-005); ★KeepItem (logic part; final verification real server); teleport safety rejects lava/roof (★NR-013) |
| Cooldowns | per-player, shared-key, global; expiry via `server.getScheduler().performTicks` + clock injection; ★relog keeps cooldown (NR-010); ★reduction in MEMORY mode (NR-019); notify throttled (★NR-027) |
| Scheduler | auto-replant delay; command delay; flight end restores previous state (★NR-020); glow end; web removal; tasks cancelled on disable |
| Auras | renders only when enabled/held; disabled worlds; `update-ticks` semantics (★NR-025) — assert via a capturing renderer test double (MockBukkit does not expose particle packets reliably) |
| GUI | list paging; click opens editor; all clicks cancelled; ★drag cancelled (NR-032); chat input `cancel`; quit clears pending input |
| Storage | SQLite in `@TempDir`: create tables, save/load player, cooldown upsert, concurrent saves (executor ×50), corrupt DB file → recovers or disables, pool closed on disable |
| Stress | 10k `PlayerMoveEvent` from 50 players: no exceptions, MOVE throttled to 1/s; 1k `/nr give` executions; 100 reload cycles: no listener/task growth |

## Known MockBukkit limitations, and how each case is handled

| Behaviour | Why MockBukkit is insufficient | Strategy |
|---|---|---|
| Commodore / FieldRename bytecode rewriting (NR-016) | MockBukkit loads classes without rewriting | Real-server smoke on every matrix row |
| Vanilla anvil `mayPickup` cost rule (NR-006) | No NMS menu logic | Real-server scripted test via a test-only helper plugin |
| Inventory clearing after `PlayerDeathEvent` (NR-007) | Death pipeline simplified | Real server |
| `Player#breakBlock` drops, tool damage, protection plugins (NR-004) | Simplified | Unit-test that the event is fired; real server + WorldGuard for integration |
| Particles / sounds actually displayed | Not observable | Test doubles for renderer decisions only |
| Skull texture application (NR-015) | Profile/texture resolution not implemented the same way | Real server |
| Main-thread lag of SQL (NR-011) | No tick timing | Real server + spark profiler |
| Spigot behaviour (no Adventure) | MockBukkit is Paper-based | Real Spigot server; plus a unit test that core packages have no `net.kyori`/`io.papermc` imports (ArchUnit) |

## Quality gates (targets from the brief)
Line ≥ 90 %, branch ≥ 85 % (JaCoCo, excluding `hook/*` reflection shims, which are documented); 100 % of command permission paths; 100 % NR-regressions; PIT mutation score ≥ 70 % on `relic`, `trigger`, `cooldown`, `ability` packages (initial target).
