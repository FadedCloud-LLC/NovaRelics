# Prioritised Improvement Roadmap

Each phase is a focused, reviewable change set. Every phase follows the same sequence:
1. Regression tests are written first and fail.
2. The fix is implemented.
3. All tests pass.
4. A CHANGELOG entry is written.

No production code is changed until this plan is approved.

## Phase 0 — Test infrastructure ✅ DONE (2026-11-12)
Delivered:
- `testing/` (191 tests; 117 behaviour tests green on 1.21 and 26.3; 74 regression tests failing as expected)
- static API link matrix
- `ci.yml` and `nightly.yml`
- `release=17` (bytecode identical)
- `NovaRelics` made non-final (MockBukkit requirement)
- `CHANGELOG.md`

Still open from the original Phase 0 scope:
- PIT mutation testing (moved to Phase 1)
- reaching 90/85 % coverage (59/38 % now; gap plan in `TEST_COVERAGE_REPORT.md`)

Original scope:
- Add the MockBukkit (v26.3 primary, v1.21 secondary), JUnit 6, Mockito, jqwik, JaCoCo and ArchUnit test dependencies, Maven toolchains, and per-generation Surefire profiles.
- Add `maven.compiler.release=17` (compile-time safety only; produces identical bytecode).
- Write the `support/` fixtures, the lifecycle, command, permission and configuration suites, and all failing regression tests from `REGRESSION_TEST_PLAN.md`. Failing tests are recorded, not suppressed.
- Add the GitHub Actions `ci.yml` (PR tier) and `nightly.yml` (matrix) skeletons.
- **Effort:** M. **Risk:** none to runtime.

## Phase 1 — Critical exploits and data loss
| Order | Finding | Change | Effort |
|---|---|---|---|
| 1 | NR-001 | Exclude self-dropping blocks; track placed blocks; fix `ore_charm.yml` | S |
| 2 | NR-004 | `Player#breakBlock` + re-entrancy guard in Vein Mine/Tree Feller; protection-aware block changes in Web Trap/Auto Replant | M |
| 3 | NR-008 | Non-destructive relic save (edit-in-place YAML); export to `exports/` | M |
| 4 | NR-012 | Id validation + canonical-path guard | S |
| 5 | NR-005 | Pay-first, cancel-on-failure in Full Repair | S |
| 6 | NR-010 | Keep cooldowns across quit; prune task | S |
| 7 | NR-007 | KeepItem via respawn restore (Spigot) / `getItemsToKeep` (Paper adapter) | S |
| 8 | NR-036 | Message lookup honours jar defaults and English fallback | XS |

## Phase 2 — Compatibility (unlocks the version matrix)
| Order | Finding | Change | Effort |
|---|---|---|---|
| 1 | NR-002 | Remove the version allowlist | XS |
| 2 | NR-003 | Shade/relocate Adventure; `Platform` interface with Spigot default + Paper adapter; replace Paper-only calls | M |
| 3 | NR-009 | Hex-capable legacy serializer; correct `&` handling | S |
| 4 | NR-016 | Registry/key-based lookups with alias tables for particles, effects, enchants, attributes, sounds, entity types | M |
| 5 | NR-015 | `PlayerProfile` skull textures with deterministic UUID | S |
| 6 | — | Compile core against `spigot-api 1.20.1`; then raise `api-version` to `1.20` | S |
| 7 | — | Real-server harness (after EULA approval); run the full matrix; update `MINECRAFT_VERSION_MATRIX.md` with real PASS/FAIL | L |

## Phase 3 — Correctness and reliability
NR-006, NR-011 (async storage + cache), NR-013, NR-014, NR-017, NR-018, NR-019, NR-020, NR-022, NR-024, NR-030, NR-031.

## Phase 4 — Performance and maintainability
NR-023 (jar slimming), NR-025, NR-026, NR-027, NR-028, NR-029, NR-032, NR-033.

Also:
- Extract `AbstractJdbcProvider`.
- Constructor-inject collaborators incrementally.
- Remove or implement the unused hooks and config keys (NR-021).

## Phase 5 — Optional features
See `FEATURE_OPPORTUNITIES.md`.

## Release gate (definition of "release ready")
- Every declared version row in the matrix is PASS on its real server, with zero NOT TESTED rows.
- All regression tests pass.
- Coverage is ≥ 90 % lines and ≥ 85 % branches, or a documented exclusion applies.
- There are no unexplained skipped tests.
- The OWASP scan reports no high CVEs.
