# Regression Test Plan

Rule: every confirmed finding gets a test that **fails on the current code** before any fix is written. The failing output is preserved in `MOCKBUKKIT_TEST_RESULTS.md`. The fix is accepted only when that test passes and nothing else regresses. Tests are never weakened or deleted to make a build pass.

| Finding | Regression test | Layer | Failing assertion today |
|---|---|---|---|
| NR-001 | `DoubleDropsDupeTest`: break a placed `ANCIENT_DEBRIS` → total drops == 1 | MockBukkit + real server | 2 drops |
| NR-002 | `VersionGateTest`: plugin enabled on every MockBukkit generation | MockBukkit | disabled (MockBukkit reports 1.21.11 / 26.3 → 26.3 rejected) |
| NR-003 | `CorePlatformIsolationTest` (ArchUnit: no `net.kyori`/`io.papermc` in core) + Spigot real-server enable | Unit + real server | imports present |
| NR-004 | `MultiBlockBreakFiresEventsTest`: a listener cancelling `BlockBreakEvent` at a location prevents its removal | MockBukkit | block removed |
| NR-005 | `FullRepairPaymentTest`: insufficient funds → click cancelled, item unchanged | MockBukkit (mock Economy) | not cancelled |
| NR-006 | Anvil pickup scenario | Real server | n/a |
| NR-007 | KeepItem death scenario | Real server (+ MockBukkit logic test) | item missing |
| NR-008 | `RelicLoaderRoundTripTest` for all 15 bundled relics + jqwik generated relics | Unit | aura/enchant/flags lost |
| NR-009 | `MiniMsgHexTest` (gradient retains `§x`, `&a` → `§a`) | Unit | reproduced already |
| NR-010 | `CooldownSurvivesRelogTest` | MockBukkit | cooldown 0 after reconnect |
| NR-011 | `CooldownDatabaseModeNoMainThreadIoTest` (JDBC proxy asserts no calls on server thread) | MockBukkit | calls observed |
| NR-012 | `RelicIdValidationTest` (`../x`, `a/b`, `..\\x`, absolute paths, >64 chars) | MockBukkit | file written outside folder |
| NR-013 | `RandomTeleportNetherRoofTest` | MockBukkit (world mock with bedrock roof) | y ≥ 128 accepted |
| NR-014 | `PhantomStrikeSafetyTest` | MockBukkit | teleport into solid block |
| NR-015 | Skull texture applied | Real server | n/a |
| NR-016 | All ability/renderer classes load + every bundled config name resolves | Real server, every matrix row | n/a |
| NR-017 | `ArmorEquipTriggerTest` (click in CRAFTING view slot 5–8) | MockBukkit | no dispatch |
| NR-018 | `CommandTriggerOnceTest` | MockBukkit | N dispatches |
| NR-019 | `CooldownReductionMemoryModeTest` | MockBukkit | unchanged |
| NR-020 | `FlightRestoresPreviousStateTest`, `DisableRevertsTemporaryEffectsTest` | MockBukkit | flight revoked / not restored |
| NR-021 | `RecipeRegistrationTest` (or the feature is removed and the docs updated) | MockBukkit | 0 recipes |
| NR-022 | `GiveOverflowDropsTest`, `TakeRemovesOneTest` | MockBukkit | items lost / whole stack removed |
| NR-023 | `ShadedJarContentsTest` (no `org/slf4j`, no sqlite natives) | Build verification (Maven enforcer / test on jar) | present |
| NR-024 | `StorageFailureDisablesPluginTest`, `ConcurrentSaveTest` | MockBukkit | NPE / CME |
| NR-025 | `AuraIntervalSemanticsTest` | MockBukkit | 16-tick interval |
| NR-026 | `TriggerStateClearedOnQuitTest` | MockBukkit | map retains entry |
| NR-027 | `CooldownNotifyThrottleTest` | MockBukkit | one message per event |
| NR-028 | `FirstRunCopiesAllBundledRelicsTest` | MockBukkit | 5 files |
| NR-029 | `KnockbackZeroVectorTest` | MockBukkit | exception |
| NR-030 | `DeniedInteractDoesNotTriggerTest` | MockBukkit | dispatched |
| NR-031 | `StealHealthCancelledDamageTest` | MockBukkit | healed |
| NR-032 | `EditorDragCancelledTest` | MockBukkit | not cancelled |
| NR-033 | `DuplicateArmorRelicDispatchTest` (document intended semantics first) | MockBukkit | 2 dispatches |
| NR-036 | `ConfigurationTest.translationMissingAKeyFallsBackToEnglish` | MockBukkit | raw key `no-permission` |

## Implementation status (Phase 0, 2026-11-12)
The planned names above were consolidated into fewer classes. Find each test by the `NR-xxx` text in its `@DisplayName`; every one is tagged `@Tag("regression")`.
- **Implemented and failing as expected:** NR-001, 002, 003, 004, 005, 008, 009 (`&` part), 010, 012, 016, 017, 018, 019, 020, 022, 024, 026, 027, 028, 029, 030, 031, 032, 033, 036.
- **Not implementable in MockBukkit (real server):** NR-006, NR-007, NR-015.
- **Planned for Phase 1:** NR-011 (JDBC-on-main-thread proxy), NR-013, NR-014, NR-021, NR-023, NR-025.
