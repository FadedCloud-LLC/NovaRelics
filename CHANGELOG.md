# Changelog

All notable changes to NovaRelics are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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
