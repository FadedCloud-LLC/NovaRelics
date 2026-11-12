# MockBukkit Test Results

**Run date:** 2026-11-12. **Production commit:** `b7fc158`, plus Phase 0 (`release=17`, non-final main class).

Machine-readable results:
- [`test-results/mockbukkit-1.21.json`](test-results/mockbukkit-1.21.json)
- [`test-results/mockbukkit-26.3.json`](test-results/mockbukkit-26.3.json)
- JaCoCo CSVs and per-API link reports in [`test-results/`](test-results/)

## Summary

| Generation | JDK | MockBukkit | Total | Behaviour tests | Regression tests (open findings) | Skipped |
|---|---|---|---|---|---|---|
| 1.21 (Paper API 1.21.11) | 21.0.12 | mockbukkit-v1.21 4.116.3 | 191 | **117 / 117 pass** | 74 run: **68 fail** (expected), 6 pass | 0 |
| 26.3 (Paper 26.3 build 200) | 25.0.3 | mockbukkit-v26.3 4.120.0 | 191 | **117 / 117 pass** | 74 run: **69 fail** (expected), 5 pass | 0 |

**Legacy 1.20 generation:** not run. `com.github.seeseemelk:MockBukkit-v1.20` uses a different package (`be.seeseemelk.mockbukkit`) and is no longer maintained, so it would need a duplicate test tree. The 1.20 range is covered by the static API link check (below) and must be confirmed on real servers.

The CI gate is `-DexcludedGroups=regression`, which passes on both generations. Regression tests are executed and reported on every run; none are skipped or suppressed.

### Regression tests that pass (and why that is correct)

| Test | Reason |
|---|---|
| `ApiCompatibilityTest[paper-api-1.20.1]` | The production bytecode links cleanly against Paper 1.20.1 |
| `PlatformIsolationTest.coreDoesNotDependOnPaperPackages` | No `io.papermc`/`com.destroystokyo` packages are used. Paper-only *methods* on Bukkit types are caught by the API link check instead. |
| `VersionGateTest` for 1.20.6, 1.21.11, 26.1.2 | These three versions are on the allowlist |
| `VersionGateTest.enablesOnMockBukkitReportedVersion` (1.21 only) | MockBukkit 1.21 reports `1.21.11`, which is allowlisted; 26.3 is not |

## Findings confirmed by execution

| Finding | Test(s) | Observed result |
|---|---|---|
| NR-001 duplication | `ExploitRegressionTest.oreCharmPlaceAndBreakLoop`, `oreCharmSilkTouchLoop` (+ control `oreCharmDoublesNormalDrop` passes) | 1 bonus ANCIENT_DEBRIS / DIAMOND_ORE spawned |
| NR-002 version gate | `VersionGateTest` | Plugin disables itself on 21 of 24 stable versions (and on 26.3, as MockBukkit reports it) |
| NR-003 Spigot | `ApiCompatibilityTest` (spigot-api), `PlatformIsolationTest` | 9–17 missing symbols on every Spigot API; core depends on `net.kyori` |
| NR-004 protection bypass | `veinMineRespectsProtection`, `treeFellerRespectsProtection` | Protected blocks became AIR |
| NR-005 economy bypass | `fullRepairWithoutMoneyIsCancelled` | `setCancelled(true)` never called |
| NR-008 data loss | `saveLoadRoundTripPreservesEverything` × 15 bundled relics | All 15 lose aura and/or item-flags/enchantments on save |
| NR-009 (`&` codes) | `ampersandCodesTranslated` | `&aGreen` stays literal |
| NR-010 relog bypass | `cooldownSurvivesReconnect` | Cooldown 0 after reconnect |
| NR-012 path traversal | `createRejectsPathTraversal` × 4 | `escape.yml`, `evil_config.yml`, `relics/sub/dir.yml` written |
| NR-016 renamed constants | Plugin **cannot enable** under MockBukkit without the rename agent: `NoSuchFieldError: PotionEffectType.CONFUSION`. Also `ApiCompatibilityTest` | 6–8 missing symbols from 1.20.5 / 1.21.3 |
| NR-017 armor equip | `armorEquipFromPlayerInventory` | No EQUIP dispatch |
| NR-018 command trigger | `commandTriggerFiresOnce` | `[A, B, A, B]`; `/spawnpoint` matched |
| NR-019 cooldown reduction | `cooldownReductionInMemoryMode` | 59 s remaining after "50 %" reduction |
| NR-020 temporary effects | `flightRestoresPreviousState`, `disableRevertsTemporaryFlight` | Flight revoked / kept permanently |
| NR-022 give/take | `giveOverflowIsNotLost`, `takeRemovesOne` | Overflow relics deleted; whole stack of 5 removed |
| NR-024 storage failure | `brokenStorageDisablesPlugin` | Plugin stays enabled; `NullPointerException` from `HikariDataSource` |
| NR-026 state leak | `triggerStateReleasedOnQuit` | `MoveTrigger.lastFire` keeps the player |
| NR-027 notice spam | `cooldownNoticeIsThrottled` | 10 notices in under a second |
| NR-028 bundled relics | `firstRunInstallsAllBundledRelics` | 5 of 15 installed |
| NR-029 NaN velocity | `knockbackZeroVector` | Velocity not finite |
| NR-030 denied interaction | `deniedInteractionDoesNotFire` | Relic fired |
| NR-031 consume relic | `consumeRelicFailsWhenMissing` | Returned success with nothing consumed |
| NR-032 GUI drag | `dragIntoGuiIsCancelled` | Not cancelled |
| NR-033 duplicate armor | `duplicateArmorRelicFiresOnce` | Fired twice |
| **NR-036 (new)** | `translationMissingAKeyFallsBackToEnglish` | Raw key `no-permission` shown to players |

## Correction to an earlier claim (NR-009)

The audit's scratch probe ran Adventure 4.17.0 *without a server* and showed gradients collapsing to 16 colours. Under MockBukkit, which mirrors Paper, `LegacyComponentSerializer.legacySection()` is supplied by the platform **with** hex support, and `ItemTest.hexColoursAreNotDownsampled` passes.

- **On Paper, hex and gradient colours work.**
- The `&`-code defect is real on Paper.
- Hex loss will occur wherever Paper's provider is absent, such as Spigot after Adventure is shaded for NR-003. The NR-003/NR-009 fix must therefore configure the serializer explicitly.

## MockBukkit limitations found while testing

| Limitation | Effect | How it is handled |
|---|---|---|
| No CraftBukkit Commodore bytecode rewriting | The production plugin cannot enable at all (NR-016) | `support/LegacyRenameAgent` test double (7 field renames + 1 enum→interface fix), documented as such; real servers remain authoritative |
| `Block#getDrops()` always empty | Drop-based exploits are invisible | Mockito `Block` double in NR-001 tests, plus a positive control |
| Player's default inventory view has no top inventory | Armor-slot clicks cannot be reproduced | Mockito `InventoryClickEvent` matching a real server's CRAFTING view (NR-017) |
| `PlayerInventory#addItem` uses armor and off-hand slots | Full-inventory scenarios need every slot filled | Test fills all `getSize()` slots (NR-022) |
| Paper-style hex legacy serializer provided | Hides Spigot-only text issues | Covered by the static Spigot API check and future real Spigot runs |
| Several `simulate*` helpers deprecated for removal in MockBukkit 4 | Compiler warnings only | Track for migration when MockBukkit removes them |
