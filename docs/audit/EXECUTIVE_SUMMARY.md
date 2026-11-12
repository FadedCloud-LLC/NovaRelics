# NovaRelics Audit: Executive Summary

| | |
|---|---|
| Project | NovaRelics 1.0.1 (author: Anthony Ponder / FadedCloud LLC) |
| Commit | `b7fc158` on `main` |
| Audit date | 2026-11-12 |
| Scope | All 147 Java files (8,521 lines) and all 23 resources were read in full; live version discovery; baseline build; executed probe |
| Production code changed | **None** |

## Headline

NovaRelics is small, readable and well structured: a registry of 50 stateless abilities driven by 22 trigger listeners. It is **not release-ready** for the required Spigot + Paper 1.20 → 26.3 range.

- **Provably broken on every Spigot version.** Adventure is not bundled, and Paper-only APIs are used.
- **Provably refuses to start on 18 of 21 Paper versions.** A hard-coded version allowlist rejects them, including the current stable release, **26.3**.
- **Two critical gameplay/security issues.** An infinite-duplication loop exists in the bundled Ore Charm, and multi-block abilities bypass land protection.
- **Two silent data-quality failures.** Saving a relic from the editor or export erases its aura, enchantments and flags. Every hex/gradient colour is reduced to the 16 legacy colours (reproduced by execution).
- **No tests or CI exist.** Coverage is 0 %.

## Phase 0 update (2026-11-12): test infrastructure delivered
| | |
|---|---|
| Test suite | `testing/`: **191 tests** (MockBukkit 4, JUnit 6, Mockito, ArchUnit, ASM, JaCoCo) |
| MockBukkit 1.21 (JDK 21) | 117 / 117 behaviour tests pass; 74 regression tests run, 68 fail as expected |
| MockBukkit 26.3 (JDK 25) | 117 / 117 behaviour tests pass; 74 regression tests run, 69 fail as expected |
| Static API link matrix | 44 Spigot + Paper APIs, 1.20 → 26.3. Only Paper 1.20 – 1.20.4 link cleanly; every Spigot API fails |
| Coverage | 59.0 % lines / 38.2 % branches (targets 90 / 85 % not yet met) |
| Findings confirmed by execution | **25 of 36** |
| Corrections | NR-009 narrowed (hex works on Paper; `&` codes broken) and downgraded to Medium. **New NR-036**: deleted language keys are shown raw to players |
| Production changes | `release=17` (bytecode identical); `final` removed from `NovaRelics` (MockBukkit requirement, no behaviour change) |
| CI | `.github/workflows/ci.yml` (gate: behaviour tests on both generations) and `nightly.yml` (API matrix, full suites) |
| Real servers | Still **BLOCKED**: awaiting EULA confirmation |

Updated severity totals: **4 Critical, 7 High, 14 Medium, 11 Low (36)**.

## Baseline facts

| Check | Result |
|---|---|
| `mvn -B clean package` (JDK 21) | ✅ exit 0 |
| Bytecode level | Java 17 (major 61) ✅ |
| Existing tests | 0 |
| Versions validated on a real server | **0** (BLOCKED: EULA acceptance needed) |
| Findings (pre-Phase 0) | 35 total: **4 Critical, 8 High, 12 Medium, 11 Low**; see Phase 0 update above for the current 36 (see `CODE_QUALITY_FINDINGS.md`, `findings.json`) |

## Answers to the final questions

**1. What is currently broken or unreliable?**
- Startup on almost every version (NR-002, NR-003).
- Gradients and hex colours (NR-009, confirmed by execution).
- Relic saving erases config (NR-008).
- Armour EQUIP/UNEQUIP never fires (NR-017).
- Recipes never register (NR-021).
- Cooldown Reduction does nothing in the default mode (NR-019).
- The COMMAND trigger fires repeatedly (NR-018).
- `/nr give` silently loses overflow, and `/nr take` removes whole stacks (NR-022).
- The bundled Mending Wool is likely unusable in survival (NR-006, needs a server).
- KEEP_ITEM likely destroys the item (NR-007, needs a server).

**2. Which parts are incompatible with specific Minecraft versions?**
- **All Spigot versions:** Adventure is missing; `sendActionBar(String)`, `getTargetEntity` and the cancellable `PlayerDeathEvent` exist only on Paper.
- **Paper, all versions except 1.20.6, 1.21.11 and 26.1.2:** rejected by the version gate.
- **1.20.5+:** skull textures (NR-015).
- **Constant renames (NR-016):** renamed potion effects and particles (≥1.20.5) and `Attribute`/`Sound` registry changes (≥1.21.3) depend on server bytecode rewriting. A failure in `CureAbility`'s static initialiser would abort plugin enable.
- **Config particle names:** `ENCHANT` and `TOTEM` are valid in only one half of the range each.

**3. Which classes and modules should be refactored?**
- `MiniMsg` (serializer).
- `RelicLoader.save` (non-destructive save).
- `NovaRelics` (remove the gate; guarded initialisation).
- New `platform/` adapter layer.
- `StorageManager`/providers (async, cache, a shared JDBC base).
- `CooldownManager`.
- `EquipTrigger`, `CommandTrigger`.
- `SkullTextureUtil`.

No full rewrite is justified.

**4. Are there security vulnerabilities or gameplay exploits?** Yes:
- Infinite duplication (NR-001).
- Protection bypass (NR-004).
- Economy bypass (NR-005).
- Cooldown bypass by relogging (NR-010).
- Path traversal letting staff with `novarelics.create`/`import`/`delete` write, read or delete arbitrary YAML files (NR-012).
- Nether-roof teleport (NR-013).
- Wall phasing (NR-014).
- Relic use inside protected regions (NR-030).

No SQL injection was found: all queries use bound parameters.

**5. What could reduce lag?**
- Remove synchronous JDBC from the main thread (NR-011).
- Asynchronous or loaded-chunk-only random teleport.
- Cache aura config and make the viewer computation O(n).
- Slim the 13.7 MB jar.

These claims are static only. No measurements exist yet; they are planned with spark on real servers.

**6. What modern APIs or practices should be adopted?**
- `Player#breakBlock`.
- `PlayerProfile`/`PlayerTextures`.
- Registry/NamespacedKey lookups.
- `ItemMeta#setEnchantmentGlintOverride` (1.20.5+, via capability check).
- Paper `PlayerArmorChangeEvent`/`PlayerJumpEvent`/`getItemsToKeep` behind an adapter.
- `--release 17`.
- Relocated Adventure.
- Constructor injection.
- ArchUnit platform-isolation rules.
- CI version matrix.

**7. Which features lack meaningful MockBukkit coverage?** All of them; there are no tests. `MOCKBUKKIT_TEST_PLAN.md` defines suites for every subsystem.

**8. Which tests require actual Spigot or Paper servers?**
- Plugin load on each version.
- Commodore/renamed-constant resolution.
- Anvil pickup cost.
- Death inventory handling.
- Skull textures.
- Protection-plugin interaction.
- Real Spigot (no Adventure) behaviour.
- Tick-time profiling.

**9. Which versions have been successfully validated?** **None on a real server.** MockBukkit behaviour tests pass on the 1.21.11 and 26.3 APIs (with documented test doubles); static linking passes only on Paper 1.20–1.20.4.

**10. What should be corrected first?**
1. **Phase 0:** test infrastructure and failing regression tests.
2. **Phase 1:** NR-001 → NR-004 → NR-008 → NR-012 → NR-005 → NR-010 → NR-007.
3. **Phase 2:** NR-002 → NR-003 → NR-009 → NR-016 → NR-015, then the real-server matrix.
4. **Phases 3–4:** reliability and performance.

See `PRIORITIZED_IMPROVEMENT_ROADMAP.md`.

## Decisions needed from the owner
1. Approve Phase 0. It adds test-only dependencies, CI workflows and `release=17`, and changes no runtime behaviour.
2. Confirm Minecraft EULA acceptance for disposable local and CI test servers, so the real-server matrix can run.
3. Confirm the platform direction: one jar with relocated Adventure plus a Paper adapter (recommended), or keep Paper-only and drop the Spigot requirement.

## Report index
`CODEBASE_OVERVIEW` · `ARCHITECTURE_ANALYSIS` · `CODE_QUALITY_FINDINGS` · `API_COMPATIBILITY_AUDIT` · `MINECRAFT_VERSION_MATRIX` · `JAVA_COMPATIBILITY` · `MOCKBUKKIT_TEST_PLAN` · `MOCKBUKKIT_TEST_RESULTS` · `REAL_SERVER_TEST_RESULTS` · `TEST_COVERAGE_REPORT` · `PERFORMANCE_ANALYSIS` · `SECURITY_AUDIT` · `DEPENDENCY_ANALYSIS` · `RESEARCH_AND_SOURCES` · `REGRESSION_TEST_PLAN` · `FEATURE_OPPORTUNITIES` · `PRIORITIZED_IMPROVEMENT_ROADMAP`, plus `findings.json` and `compatibility-matrix.json`.
