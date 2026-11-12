# Changelog

All notable changes to NovaRelics are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Fixed — Phase 1: critical exploits and data loss
- **NR-001 item duplication.** `DOUBLE_DROPS` no longer duplicates a drop that is the broken block itself (ancient debris, silk-touched ores), which closes the place → break → place loop. It also gives no bonus when drops are disabled by another plugin or the player is in creative. `ANCIENT_DEBRIS` was removed from the bundled Ore Charm list (it could never receive a bonus under the new rule).
- **NR-004 protection bypass.** Vein Mine and Tree Feller now break blocks through `Player#breakBlock`, which fires `BlockBreakEvent`. Protection plugins can deny each block, tool durability is consumed, vanilla drops apply, and the cascade stops if the tool breaks. Web Trap only places cobwebs where a `BlockPlaceEvent` for the player is allowed. Blocks broken or placed by these abilities do not re-trigger relics.
- **NR-008 relic configs lost on save.** Saving now updates the existing YAML in place, writing every field (aura, enchantments, item-flags, max-charges, texture ids). Comments and unknown keys are preserved. `/nr export` writes to `exports/<id>.yml` instead of overwriting the live file. Duplicate copies every field. Enchantments accept namespaced keys (`sharpness`) as well as legacy names.
- **NR-012 path traversal.** Relic ids must match `[a-z0-9_-]{1,64}`. Create, import, save, delete, duplicate and export all resolve paths through a canonical-path check. Relic files with unsafe ids are skipped on load. New message key `invalid-relic-id` (en/es/ru).
- **NR-005 free repairs.** Full Repair takes payment first, checks that the withdrawal succeeded, and cancels the anvil click on failure. A `money-cost` with no Vault economy now denies the repair (and logs a warning) instead of making it free.
- **NR-010 cooldown reset by reconnecting.** Quitting only prunes *expired* cooldowns.
- **NR-007 KEEP_ITEM destroyed the relic.** The relic is removed from the death drops, stored in the player's persistent data and returned on respawn, or on join if the player left while dead. It survives restarts and works the same on Spigot and Paper. Not yet confirmed on a real server.
- **NR-036 raw message keys.** Message lookup now falls back from the active language file to its bundled defaults, then to English, instead of printing the key.

### Design notes (deviations from the audit roadmap)
- NR-001 uses the self-drop rule instead of tracking player-placed blocks. Placing a silk-touched ore and mining it later still gives one bonus. That is the charm's normal effect, deferred, not a repeatable loop. With the rule, `requires-silk-touch: true` can no longer give a bonus.
- NR-004 leaves Auto Replant unchanged. It only re-plants the crop the player just broke at that same spot, which the protection plugin already allowed.

### Changed (behaviour, from the fixes above)
- Vein Mine and Tree Feller drops now follow vanilla rules (Fortune and Silk Touch apply) and consume tool durability.
- Exports are written to `plugins/NovaRelics/exports/`.

### Tests
- 218 tests (170 gate tests pass on MockBukkit 1.21 and 26.3; 48 regression tests remain for open findings, of which 42/43 fail as expected).
- Each fix has positive controls, so a test cannot pass merely because MockBukkit lacks the behaviour.
- Each profile now compiles tests into its own `target/test-classes-<generation>`, so switching profiles no longer requires `clean`.

### Added — Phase 0: test infrastructure (no gameplay or runtime behaviour change)
- `testing/`: a separate Maven test project (MockBukkit 4, JUnit 6, Mockito, ArchUnit, ASM, JaCoCo). It runs against the unchanged production classes in `target/classes`.
  - Profile `mb-1.21` (default): `mockbukkit-v1.21:4.116.3` / Paper API 1.21.11 on JDK 21.
  - Profile `mb-26.3`: `mockbukkit-v26.3:4.120.0` / Paper API 26.3 on JDK 25.
  - Profile `api-matrix`: a static link check of the production bytecode against all 44 published Spigot and Paper APIs from 1.20 to 26.3.
- 191 tests:
  - 117 behaviour tests, covering lifecycle, commands, permissions, configuration, items, triggers, cooldowns, abilities, storage, GUI and stress.
  - 74 `regression`-tagged tests that reproduce audit findings NR-001 to NR-036. These fail until each finding is fixed.
- `.github/workflows/ci.yml`: build with a Java 17 bytecode check, then behaviour tests on both MockBukkit generations (the gate), then regression tests (reported, not gating).
- `.github/workflows/nightly.yml`: full API link matrix and full suites with reports.
- `docs/audit/`: the audit reports, plus `test-results/` holding JUnit summaries, JaCoCo CSVs and per-API link reports.

### Changed
- `pom.xml`: `maven.compiler.release=17` replaces `source`/`target` 17. This now also blocks Java 18+ library APIs, which would break 1.20.1–1.20.4 servers. Emitted bytecode is byte-for-byte identical (verified on all 156 classes).
- `NovaRelics` main class: removed the `final` modifier. MockBukkit 4 loads plugins through a generated subclass, so a final main class cannot be tested. This changes no behaviour; only the class's `ACC_FINAL` flag differs.

### Known issues (documented, not yet fixed)
See `docs/audit/CODE_QUALITY_FINDINGS.md` and `docs/audit/PRIORITIZED_IMPROVEMENT_ROADMAP.md`.

## [1.0.1] - 2026-06-18
- bStats metrics, README update.
