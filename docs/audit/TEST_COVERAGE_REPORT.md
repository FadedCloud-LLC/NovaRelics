# Test Coverage Report

Measured 2026-11-12 with JaCoCo 0.8.16 over the production classes. All 191 tests ran, including the failing regression tests. Source data:
- [`test-results/jacoco-mockbukkit-1.21.csv`](test-results/jacoco-mockbukkit-1.21.csv)
- [`test-results/jacoco-mockbukkit-26.3.csv`](test-results/jacoco-mockbukkit-26.3.csv)

| Metric | Measured (1.21 / 26.3) | Target | Status |
|---|---|---|---|
| Line coverage | **59.0 % / 59.0 %** (2 275 / 3 858) | ≥ 90 % | ❌ not met |
| Branch coverage | **38.3 % / 38.2 %** | ≥ 85 % | ❌ not met |
| Method coverage | 71.0 % | — | — |
| Command permission paths | 8 protected + 3 public sub-commands, inherited admin permission, runtime revocation | 100 % | ✅ |
| Confirmed-defect regression tests | 25 findings reproduced by 74 tests (see MOCKBUKKIT_TEST_RESULTS.md) | 100 % of fixed findings | ✅ for every finding testable in MockBukkit |
| Mutation score (PIT) | not run yet | ≥ 70 % core | ⏳ planned after Phase 1 |
| Supported versions verified on real servers | 0 / 45 | 100 % | ❌ BLOCKED (EULA) |

The JaCoCo check is configured at 90 / 85 % but reports a warning rather than failing the build (`jacoco.haltOnFailure=false`). The threshold will be enforced once Phase 1 adds the remaining tests. It is not hidden: CI logs `Rule violated … lines covered ratio is 0.59`.

## Coverage by package (1.21)

| Package | Lines | Notes |
|---|---|---|
| (root) `NovaRelics` | 96.1 % | |
| command | 97.8 % | |
| config | 90.8 % | |
| relic | 84.6 % | |
| cooldown | 83.6 % | DATABASE cooldown mode not yet exercised |
| rarity | 82.7 % | |
| ability | 78.9 % | |
| aura | 78.9 % | Manager covered; renderers mostly not |
| trigger | 74.7 % | |
| trigger.impl | 67.5 % | Anvil/grindstone/smithing/fish/projectile listeners not yet driven |
| command.sub | 67.4 % | |
| gui | 59.2 % | Chat-input edit flows not yet driven |
| ability.impl | 57.3 % | 24 of 50 abilities have direct tests |
| storage | 50.9 % | MySQL provider requires Testcontainers (Docker; CI only) |
| util | 45.3 % | ParticleUtil dust/transition paths |
| hook | 30.7 % | Reflection hooks for absent plugins; documented exclusion candidate |
| texture | 18.6 % | Skull textures: real-server item (NR-015) |
| recipe | 17.3 % | Feature is non-functional (NR-021) |
| aura.impl | 8.8 % | 14 particle renderers; decision-level tests planned |

## Gap plan to reach the targets (Phase 1, alongside fixes)
1. Aura renderers: a capturing `ParticleUtil` seam, then one test per renderer (≈ +250 lines).
2. The remaining 26 abilities, especially Teleport random/bed, Lightning, Explosion, Fireball, Firework, Glow, Pull, Shockwave, StealHealth, WebTrap, AutoSmelt, AutoReplant and TreeFeller happy paths (≈ +350 lines).
3. Anvil, grindstone, smithing, fish and projectile trigger listeners (≈ +80 lines).
4. GUI chat-input editing flows (≈ +90 lines).
5. MySQL provider via Testcontainers in CI (≈ +130 lines).

## Documented exclusions (proposed)
- `hook/*Hook` reflection wrappers for ItemsAdder, Oraxen, Nexo, HeadDatabase and MythicMobs. Those plugins cannot be test dependencies; only the "absent → disabled, no exception" path is covered.
- `NovaRelics#isVersionSupported`: to be deleted by the NR-002 fix.
