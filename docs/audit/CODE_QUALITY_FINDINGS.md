# Code Quality Findings

Audit date: 2026-11-12. Commit audited: `b7fc158` (`main`). All 147 Java source files and all 23 resource files were read.

**How to read the status labels**

- **Confirmed (static)**: the defect is certain from reading the code, with no runtime ambiguity.
- **Confirmed (executed)**: the behaviour was reproduced by running the compiled classes (see "Evidence").
- **Suspected – needs real server**: the analysis is strong, but the outcome depends on server internals that MockBukkit cannot reproduce faithfully.

Severity ranking follows the order in the master prompt: security/exploit → duplication/data loss → compatibility → correctness → lifecycle/thread safety → reliability → performance → maintainability.

A machine-readable copy of these findings is in [`findings.json`](findings.json).

> **Phase 0 update (2026-11-12):** 25 findings are now **confirmed by executed tests** (MockBukkit 1.21 and 26.3, plus a static link check against 44 Spigot/Paper APIs). See `MOCKBUKKIT_TEST_RESULTS.md`.
>
> Testing also changed two entries:
> - **NR-009 was narrowed.** Hex and gradients work on Paper; only `&` codes are broken there. It is downgraded to Medium.
> - **NR-036 was added.** Deleted language keys are shown raw to players.
>
> **Phase 1 update (2026-11-12): 8 findings fixed** on branch `phase1-critical-fixes`: NR-001, NR-004, NR-005, NR-007 (still needs real-server confirmation), NR-008, NR-010, NR-012 and NR-036. Each fix has passing regression tests plus positive controls. **28 findings remain open.**
>
> Totals: **36 findings — 4 Critical, 7 High, 14 Medium, 11 Low.**

---

## Critical

### NR-001 — Ore Charm enables infinite item duplication (place-and-break loop)
| | |
|---|---|
| Category | Gameplay exploit / item duplication |
| Files | `ability/impl/DoubleDropsAbility.java:13-37`, `resources/relics/ore_charm.yml` |
| Versions / platforms | All |
| Status | Confirmed (static); regression test required on a real server |
| Confidence | High |

**Current implementation:** `DOUBLE_DROPS` drops a second copy of `block.getDrops(tool)` for every listed block. It does not check whether a player placed the block.

**Evidence:** `ore_charm.yml` lists `ANCIENT_DEBRIS`, and `requires-silk-touch: false`. Ancient debris always drops itself, so placing it and breaking it again yields 2 debris, which repeats forever. With a Silk Touch tool, every listed ore also drops its own ore block, so the same loop works for diamond and emerald ore.

**Root cause:** Player-placed blocks are not tracked, and self-dropping blocks are not excluded.

**Recommended change:**
1. Skip any block whose drops include the block's own material.
2. Skip blocks placed by players. Track placements in chunk PersistentDataContainer, or use the `BlockPlaceEvent` metadata approach.
3. Change the bundled config so it excludes `ANCIENT_DEBRIS` and Silk Touch.

**Alternatives:** Require a minimum tool tier, or apply a vanilla-style Fortune bonus instead of doubling.

**Tests:** MockBukkit unit test of the drop-calculation helper; a real-server regression test of the place → break → count loop.

### NR-002 — Hard-coded version allowlist disables the plugin on most supported versions
| | |
|---|---|
| Category | Compatibility failure |
| Files | `NovaRelics.java:27-65` |
| Versions | Rejected: 1.20–1.20.5, 1.21–1.21.10, 26.1, 26.1.1, **26.2, 26.3 (current stable)** |
| Status | Confirmed (static) |
| Confidence | High |

**Current implementation:** `Set.of("1.18.2","1.19.4","1.20.6","1.21.11","26.1.2")` is matched against the start of `Bukkit.getBukkitVersion()`. Any other version disables the plugin in `onEnable`.

**Impact:** The plugin refuses to start on 18 of the 23 stable releases in the required range, including the newest release (26.3).

**Recommended change:** Remove the allowlist. Rely on `api-version`, plus capability checks (for example `Registry` presence or class probes) for any feature that really is version-specific.

**Tests:** A MockBukkit lifecycle test that the plugin enables; a real-server enable smoke test on every matrix row.

### NR-003 — Plugin cannot load on Spigot
| | |
|---|---|
| Category | Compatibility failure (platform) |
| Files | `pom.xml` (Adventure `provided`), `util/MiniMsg.java`, `NovaRelics.java:149`, `ActionBarAbility.java:22`, `PhantomStrikeAbility.java:25`, `PreventDeathAbility.java:34` |
| Platforms | Spigot (all versions) |
| Status | Confirmed (static) for Adventure; Paper-only methods confirmed against the Spigot API; needs real-server confirmation |
| Confidence | High |

**Evidence:**
- Adventure / MiniMessage is a `provided` dependency. Paper ships it; Spigot does not.
- `MiniMsg` is the first class used in `onEnable` (the `console(...)` message), so `NoClassDefFoundError: net/kyori/adventure/text/Component` happens before anything else runs.
- Several calls exist only in the Paper API: `Player#sendActionBar(String)`, `LivingEntity#getTargetEntity(int, boolean)`, and the `Cancellable` interface on `PlayerDeathEvent`.

Commit `20da96b` ("remove Spigot support") shows this was a deliberate choice. It conflicts with the new requirement to support Spigot.

**Recommended change:**
- Shade and relocate `adventure-api`, `adventure-text-minimessage` and `adventure-text-serializer-legacy`, and use them only to convert MiniMessage into legacy strings (which also fixes NR-009).
- Replace the Paper-only calls with Spigot equivalents, or isolate them behind a `PaperAdapter` that is loaded only after a capability check:
  - `player.spigot().sendMessage(ChatMessageType.ACTION_BAR, …)`
  - `World#rayTraceEntities`
  - A `PreventDeath` built on `EntityDamageEvent` (cancel lethal damage), which is also how the totem works.

### NR-004 — Vein Mine / Tree Feller break blocks without firing `BlockBreakEvent` (protection bypass)
| | |
|---|---|
| Category | Security / griefing |
| Files | `VeinMineAbility.java:28-34`, `TreeFellerAbility.java:26-32`, also `WebTrapAbility.java:20-24`, `AutoReplantAbility.java:41-50` |
| Status | Confirmed (static) |
| Confidence | High |

**Evidence:** Connected blocks are removed with `block.setType(AIR)`, and `getDrops` is dropped manually. No `BlockBreakEvent` is fired per block, so WorldGuard, GriefPrevention, Lands, Towny and CoreProtect never see the change. A player standing on the edge of a claim can vein-mine or fell into it. Tool durability is also never consumed.

**Recommended change:** Use `Player#breakBlock(Block)` (Bukkit 1.17+). It fires `BlockBreakEvent`, respects protection, applies tool damage and drops items through vanilla logic. Add a re-entrancy guard so the extra breaks do not re-trigger the ability.

---

## High

### NR-005 — Full Repair charges money only after the result is taken, and does not cancel on failure
`FullRepairAbility.java:26-50`, `trigger/impl/AnvilTrigger.java:44-61`. The anvil result is set in `PrepareAnvilEvent`. The Vault money check runs in `InventoryClickEvent`, but when the balance is too low it only sends a message and returns `false`. The click is **not cancelled**, so the player keeps a fully repaired item for free.

**Fix:** Cancel the click when the payment fails, and withdraw first, checking `transactionSuccess()`.

Status: Confirmed (static). Severity: High (economy bypass).

### NR-006 — Bundled Mending Wool is probably unusable in survival (`xp-cost: 0`)
`FullRepairAbility.java:24`, `relics/mending_wool.yml`. The repair cost is set to 0. Vanilla's anvil pickup rule (`AnvilMenu#mayPickup`) requires a cost greater than 0 for non-creative players.

Status: **Suspected – needs real server**, because MockBukkit does not run vanilla menu logic.

**Fix:** Clamp the cost to at least 1, or document creative-only use.

### NR-007 — KEEP_ITEM can destroy the relic
`KeepItemAbility.java:12-24`. The relic is removed from `event.getDrops()` and re-added to the inventory *inside* `PlayerDeathEvent`. Unless `keepInventory` is on, the server clears the inventory after the event, so the item is deleted. The comment mentions a respawn listener that does not exist.

**Fix:** Use Paper's `event.getItemsToKeep()` where available. On Spigot, store the item and restore it in `PlayerRespawnEvent`.

Status: Suspected – needs real server. Confidence: medium-high.

### NR-008 — Saving a relic silently deletes most of its configuration
`RelicLoader.save` (`RelicLoader.java:152-190`) writes only `id, display-name, lore, material, rarity, custom-model-data (>0), glow, skull-texture, permission, category, triggers, cooldown`.

It drops:
- `aura` (the whole section)
- `enchantments`
- `item-flags`
- `max-charges`
- `head-database-id`, `itemsadder-id`, `oraxen-id`, `nexo-id`

Every editor GUI click saves, and `/nr export` writes to **the same file in `relics/`**. Opening a bundled relic in the editor and changing anything therefore permanently erases its aura and flags. The GUI's "Toggle Aura" button saves a value that is never written.

`RelicManager.duplicate` also leaves out the aura.

Status: Confirmed (static). **Fix:** Load the original YAML and change only the edited keys, or make the serialiser cover every field. Add a round-trip test (load → save → load must be equal).

### NR-009 — `&` codes appear as literal text; hex/gradients depend on Paper's serializer *(downgraded to Medium after Phase 0)*

**Phase 0 correction:** under MockBukkit, which mirrors Paper, `legacySection()` comes from the platform with hex support, so gradients survive (`ItemTest.hexColoursAreNotDownsampled` passes). The down-sampling shown below happens only when Adventure runs without Paper's provider: the scratch probe, and Spigot once Adventure is shaded for NR-003. The `&`-code defect is confirmed on Paper (`ItemTest.ampersandCodesTranslated` fails). The original analysis follows.

`MiniMsg.java:15-18,46-48,63-65`. `LegacyComponentSerializer.legacySection()` is used without `.hexColors()`.

**Evidence (executed):** The compiled class was run against Adventure 4.17.0 in a scratch harness (no repo changes):
```
toLegacy("<gradient:#FF6600:#FFDD00>Phoenix Feather</gradient>")
  -> §6P§6h§6o§6e§6n§6i§6x§6 §6F§6e§6a§6t§6h§6e§6r   (hex §x format present: false)
toLegacy("&aGreen &lBold")  -> "&aGreen &lBold"            (codes not translated)
toLegacy("&#FF0000Red hex") -> "§4Red hex"                 (hex down-sampled)
```
This affects every item name and lore line, titles, action bars, GUI titles and chat messages. The headline "hex colour / gradient titles" feature does not work.

**Fix:** Use `LegacyComponentSerializer.builder().character('§').hexColors().useUnusualXRepeatedCharacterHexFormat().build()`. Convert legacy `&` codes with `legacyAmpersand().deserialize` and then serialise to MiniMessage *before* MiniMessage parsing. On Paper, set Components directly (`meta.displayName(Component)`) through the adapter.

### NR-010 — Cooldowns can be bypassed by reconnecting
`JoinQuitTrigger.java:31`, `CooldownManager.java:106-108`. `clearPlayer(uuid)` wipes every per-player cooldown on quit when `cooldown.storage: MEMORY` (the default). A 120 s Phoenix Feather cooldown resets with a relog.

**Fix:** Keep entries until they expire, and prune expired entries on a timer.

Status: Confirmed (static).

### NR-011 — Synchronous SQL on the main thread
- `CooldownManager` in `DATABASE` mode calls `StorageManager.getRemainingCooldown`, which runs a JDBC query on every trigger dispatch. That includes MOVE (once per second per player), every damage event, and every `%novarelics_cooldown_<id>%` placeholder request.
- `JoinQuitTrigger.onJoin` and `GiveSubCommand` call `getPlayer()`, which loads the player from the database synchronously.
- The SQLite pool size is 1, so concurrent async saves block main-thread reads.

**Fix:** Keep an in-memory cooldown cache with write-behind persistence, and load player data in `AsyncPlayerPreLoginEvent`.

Status: Confirmed (static). Measuring real tick impact needs a profiler on a real server.

### NR-012 — Path traversal in create/import/delete/save
- `CreateSubCommand.java:30` takes the id from the command (`args[0]`), and `RelicManager.save` then calls `new File(relicsFolder, id + ".yml")`.
  - `/nr create ../../Essentials/config` overwrites another plugin's `config.yml`.
  - `/nr import ../config` reads files outside the folder.
- An `id:` value inside a relic YAML is used the same way on save and on `/nr delete`.

This escalates `novarelics.create`/`import` (often handed to builders) into arbitrary YAML write/delete inside the server directory.

**Fix:** Validate ids against `^[a-z0-9_\-]{1,64}$`, and check that `file.getCanonicalPath()` starts with the relics folder.

Status: Confirmed (static).

---

## Medium

| ID | Finding | Files | Status |
|---|---|---|---|
| NR-013 | `TELEPORT mode: RANDOM` uses `getHighestBlockYAt`. In the Nether this is the bedrock roof, so players can reach the roof. It also loads target chunks synchronously (lag). | `TeleportAbility.java:51-69` | Confirmed (static) |
| NR-014 | `PHANTOM_STRIKE` teleports behind the target with no safety check, so players can phase into walls and through doors. Also Paper-only `getTargetEntity`. | `PhantomStrikeAbility.java:13-23` | Confirmed (static) |
| NR-015 | Skull textures reflect into `CraftMetaSkull.profile` with an authlib `GameProfile`. On 1.20.5+ that field is a `ResolvableProfile`, so textures silently fail. A random UUID per build also stops identical relics from stacking. Use `Bukkit.createPlayerProfile(UUID, name)` + `PlayerTextures#setSkin(URL)` (Spigot and Paper since 1.18.1). | `SkullTextureUtil.java` | Suspected – needs real server |
| NR-016 | Compiled against `paper-api 1.18.2` while referencing constants renamed or removed later: `Particle.CRIT_MAGIC/SPELL_WITCH`, `PotionEffectType.CONFUSION/HARM/SLOW/SLOW_DIGGING`, `Attribute.GENERIC_MAX_HEALTH`, `Enchantment.getByName`. Loading depends on CraftBukkit's `Commodore`/`FieldRename` bytecode rewriting (selected by `api-version: 1.18`). String lookups from config (`Particle.valueOf`, `PotionEffectType.getByName`, `Sound.valueOf`, `Material.valueOf`) are version-dependent: `TOTEM` vs `TOTEM_OF_UNDYING`, `ENCHANT` vs `ENCHANTMENT_TABLE`. | many; see API_COMPATIBILITY_AUDIT.md | Suspected – needs real server per version |
| NR-017 | Armor EQUIP/UNEQUIP never fires. The listener checks `InventoryType.PLAYER`, but the top inventory of a player's own inventory view is `CRAFTING`. Shift-click, hotbar-swap, right-click-equip and dispensers are also ignored. Bundled Swift Talisman and Vitality Locket only fire on hotbar scrolling. | `EquipTrigger.java:48-70` | Confirmed (static) |
| NR-018 | `COMMAND` trigger calls `dispatch` once per *matching ability entry*, and each dispatch runs all abilities, so effects fire N² times. A prefix match also makes `/home` match `/homes`. | `CommandTrigger.java:28-40` | Confirmed (static) |
| NR-019 | `COOLDOWN_REDUCTION` writes only to SQL storage under a hard-coded `relic:` key. It has no effect in MEMORY mode (the default), and ignores shared keys and GLOBAL cooldowns. | `CooldownReductionAbility.java` | Confirmed (static) |
| NR-020 | Temporary state is not restored on disable or overlap. `FLIGHT` revokes flight that players legitimately had (`/fly`); overlapping uses end early; disabling mid-effect leaves survival players with permanent flight. `GLOW` and `WEB_TRAP` (cobwebs left in the world) have the same problem. | `FlightAbility`, `GlowAbility`, `WebTrapAbility` | Confirmed (static) |
| NR-021 | Incomplete or dead features: recipes never register (`customData` is never filled by the loader); `max-charges` and the `nr_charges` table are unused; `allowed-items` is ignored; rarity `glow/sound/particles/aura-enabled` are unused; several config keys are unused (`drops.*`, `cooldown.cooldown-bar`, `performance.async-data-loading`, `performance.particle-distance-squared`, `gui.*`); the LuckPerms/MythicMobs/ModelEngine/ProtocolLib hooks are never used. | `RecipeManager`, `RelicLoader`, `HookManager`, `config.yml` | Confirmed (static) |
| NR-022 | `/nr give` allows unbounded amounts, and `addItem` leftovers are silently discarded when the inventory is full (the relic is lost but reported as given). `/nr take` removes the whole stack but reports "1". | `GiveSubCommand.java:50-64`, `TakeSubCommand.java:49-59` | Confirmed (static) |
| NR-023 | The shaded jar is 13.7 MB. It bundles `sqlite-jdbc` (with 62 native libraries) that Spigot/Paper already provide, and un-relocated `org.slf4j` classes (conflict risk). | `pom.xml` | Confirmed (jar listing) |
| NR-036 | **Found by Phase 0 tests.** When a message key is missing from the active language file, players see the raw key (for example `no-permission`). `getString(key, key)` passes an explicit default, so Bukkit never consults the jar defaults set with `setDefaults`. The English-fallback loop also skips keys because `contains()` counts defaults. | `ConfigManager.java:52-74,149-153` | Confirmed (executed: `ConfigurationTest.translationMissingAKeyFallsBackToEnglish`) |
| NR-024 | Storage robustness. If both providers fail, the plugin keeps running with a null datasource (NPEs). Async saves after quit can race `onDisable` and be lost. `PlayerData`'s `HashSet` is mutated on the main thread while being serialised async. Expired cooldown rows are never purged. MySQL `useSSL=false&allowPublicKeyRetrieval=true` is hard-coded. | `storage/*` | Confirmed (static) |

## Low

| ID | Finding | Files |
|---|---|---|
| NR-025 | The aura "nearby viewer" check always passes because the wearer is in `world.getPlayers()`. `update-ticks` counts global intervals (4 ticks each) rather than server ticks, so `update-ticks: 4` means 16 ticks. The per-pass cost is O(online² × relics). | `AuraManager.java:86-113` |
| NR-026 | `JumpTrigger.wasOnGround` and `MoveTrigger.lastFire` are never cleared on quit (slow leak). JUMP relies on the client-controlled `isOnGround`; Paper has `PlayerJumpEvent`. | `JumpTrigger`, `MoveTrigger` |
| NR-027 | The cooldown message is sent on *every* passive event while on cooldown (MOVE, damage, break), which floods chat. | `TriggerManager.java:70-73` |
| NR-028 | Only 5 of the 15 bundled relics are copied on first run (the README says 15). | `ConfigManager.java:112-124` |
| NR-029 | `KNOCKBACK`/`PULL`/`SHOCKWAVE` normalise a zero vector when an entity shares the player's position, producing NaN velocity, an exception, and an ability failure. | knockback-style abilities |
| NR-030 | `ClickTrigger` ignores denied interactions (`useItemInHand() == DENY`), so relics fire inside protected regions. Ability side effects run at HIGH priority, so cancellation at HIGHEST still leaves the side effects applied. | `ClickTrigger`, `BreakBlockTrigger` |
| NR-031 | `STEAL_HEALTH` heals even when the damage was cancelled (PvP-disabled zones). `CONSUME_RELIC` reports success even when nothing was consumed. | `StealHealthAbility`, `ConsumeRelicAbility` |
| NR-032 | The editor GUI does not cancel `InventoryDragEvent`; items dragged into the editor are deleted on the next `populate()`. | `RelicEditorGui`, `RelicListGui` |
| NR-033 | The same relic worn in two armour slots dispatches twice; main hand/off hand dedupe compares definitions only. | `TriggerManager.java:122-140` |
| NR-034 | The editor's chat input relies on `AsyncPlayerChatEvent`, which is deprecated on Paper (still fired). | `ChatInputManager.java` |
| NR-035 | `ResultSet`s are not closed explicitly; they are closed with the statement, which is acceptable but untidy. | `storage/*Provider.java` |

## Maintainability observations (not defects)

- Every manager reaches into the `NovaRelics` god object (the static `getInstance()` is used in utils). Constructor-injected collaborators would make unit testing much cheaper; this is the main obstacle to MockBukkit-free unit tests.
- `TriggerContext` duplicates `AbilityContext` and is unused.
- Ability lookups are string-keyed with `getParams()` raw casts (`List<String>` unchecked). A small typed accessor (`getStringList`) would remove the `ClassCastException` risk when a user writes a scalar instead of a list.
- Classes are generally small and readable. Cyclomatic complexity is low everywhere; the largest class is `RelicEditorGui` at 242 lines. No rewrite is justified. Fix in focused increments.
