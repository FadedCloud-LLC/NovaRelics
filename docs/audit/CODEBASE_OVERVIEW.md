# Codebase Overview

## Repository inventory

| Item | Value |
|---|---|
| Build system | Maven (single module), `maven-shade-plugin` 3.5.1 |
| Artifact | `NovaRelics-1.0.1.jar` (13.7 MB shaded; 292 KB unshaded) |
| Java | source/target 17, bytecode major 61 |
| Compile API | `paper-api 1.18.2-R0.1-SNAPSHOT` |
| Entry point | `dev.aponder.novarelics.NovaRelics` (`JavaPlugin`) |
| Metadata | `plugin.yml`: `api-version: 1.18`; 10 softdepends; 1 command (`novarelics`, aliases `nr`, `relics`); 10 permissions |
| Source size | 147 Java files, 8,521 lines; 23 resource files |
| Tests | **None** (`src/test` does not exist) |
| CI | **None** (`.github/` does not exist) |
| Docs | `README.md`, `LICENSE` (MIT) |
| Baseline build | `mvn -B clean package` → **exit 0**, no compiler warnings printed (2026-11-12, JDK 21.0.12, Maven 3.9.11) |

## Package map and inspection coverage

All files listed here were read in full during the audit.

| Package | Files | Lines (approx.) | Responsibility | Inspected |
|---|---|---|---|---|
| `(root)` | `NovaRelics` | 167 | Lifecycle, version gate, service wiring, bStats | ✅ |
| `config` | `ConfigManager` | 167 | config.yml / language / rarities loading, message lookup | ✅ |
| `relic` | Builder, Definition, Loader, Manager | 535 | Relic model, YAML (de)serialisation, ItemStack creation, PDC tagging | ✅ |
| `rarity` | Definition, Manager | 146 | Rarity tiers | ✅ |
| `trigger` | Manager, Type, Context + 22 listeners | ~1,050 | Bukkit event → relic dispatch | ✅ |
| `ability` | Ability, Config, Context, Registry + 50 impls | ~2,900 | Effect implementations | ✅ |
| `cooldown` | Manager, Entry, Type | 146 | Per-player/global cooldowns (memory or SQL) | ✅ |
| `aura` | Manager, Definition, Mode, Type, Renderer + 14 renderers | ~1,000 | Repeating particle auras | ✅ (renderers sampled for API use; all read for Particle references) |
| `command` | Root + 11 subcommands | ~800 | `/nr` command tree, tab completion | ✅ |
| `gui` | GuiManager, GuiUtils, RelicListGui, RelicEditorGui, ChatInputManager | ~510 | Admin editor | ✅ |
| `storage` | Manager, Provider, SQLite, MySQL, PlayerData | ~560 | HikariCP persistence | ✅ |
| `hook` | 10 hooks + PAPI expansion | ~560 | Optional integrations (mostly reflection) | ✅ |
| `texture` | TextureManager, SkullTextureUtil | 131 | Base item resolution (IA/Oraxen/Nexo/HDB/skulls) | ✅ |
| `recipe` | Manager, Type | 126 | Custom recipes (non-functional — NR-021) | ✅ |
| `util` | ColorUtil, ItemUtil, MiniMsg, ParticleUtil | 323 | Helpers | ✅ |
| resources | config, rarities, 3 languages, 15 relics, plugin.yml | — | Defaults | ✅ |

## Commands

| Sub-command | Permission | Player/console | Notes |
|---|---|---|---|
| help | (none) | both | Lists commands the sender can use |
| list, info | (none; root requires `novarelics.use`, default true) | both | |
| give `<player> <relic> [amount]` | novarelics.give | both | NR-022 |
| take `<player> <relic>` | novarelics.take | both | NR-022 |
| editor `[relic]` | novarelics.editor | player | GUI |
| create `<id>` | novarelics.create | both | NR-012 |
| delete `<id>` | novarelics.delete | both | NR-012 |
| import `<file>` / export `<id>` | novarelics.import / export | both | NR-008, NR-012 |
| reload | novarelics.reload | both | |

## Triggers (28 enum values, 22 listener classes)
RIGHT/LEFT/SHIFT_RIGHT/SHIFT_LEFT_CLICK, ANVIL, SMITHING_TABLE, GRINDSTONE, CONSUME, BREAK_BLOCK, PLACE_BLOCK, KILL_MOB, KILL_PLAYER, ENTITY_DAMAGE, ENTITY_DAMAGED, PLAYER_DEATH, PLAYER_RESPAWN, PROJECTILE_HIT, MOVE, JUMP, SNEAK, FISH, EQUIP, UNEQUIP, JOIN, QUIT, WORLD_CHANGE, COMMAND.

`CUSTOM_EVENT` is declared but has no listener (dead enum value).

## Abilities (50 registered)
FULL_REPAIR, REPAIR_PERCENT, HEAL, FEED, TELEPORT, COMMAND, GIVE_MONEY, TAKE_MONEY, GIVE_XP, DOUBLE_DROPS, AUTO_SMELT, VEIN_MINE, TREE_FELLER, LIGHTNING, FIREBALL, POTION_EFFECT, SPAWN_ENTITY, PARTICLE, SOUND, MESSAGE, TITLE, ACTIONBAR, BOOST_DAMAGE, REDUCE_DAMAGE, GAIN_HEALTH_ON_KILL, PREVENT_DEATH, KEEP_ITEM, COOLDOWN_REDUCTION, AUTO_REPLANT, CUSTOM_RECIPE_UNLOCK, EXPLOSION, FLIGHT, SET_ON_FIRE, KNOCKBACK, BROADCAST, GIVE_ITEM, LOOK_TELEPORT, INVINCIBILITY, FIREWORK, STEAL_HEALTH, PULL, LAUNCH, CURE, SHOCKWAVE, GLOW, CONSUME_RELIC, PHANTOM_STRIKE, CHARGE, WEB_TRAP, NEARBY_HEAL.

## Scheduled tasks
- `AuraManager`: a repeating sync task every `aura.global-update-ticks` (4).
- One-shot delayed tasks: AutoReplant, Command (delay), Flight, Glow, WebTrap.
- Async tasks: `StorageManager.savePlayer` and `unloadPlayer`.

## Functional overview (per major feature)

| Feature | How it works | Key files | Dependencies | Version concerns | Known defects | Existing tests |
|---|---|---|---|---|---|---|
| Relic items | YAML → `RelicDefinition` → `RelicBuilder` builds ItemStack, tags PDC `novarelics:id` | relic/* | — | glint, CMD, skulls | NR-008, NR-009, NR-015 | none |
| Triggers | 22 listeners → `TriggerManager.dispatch` → permission, cooldown, abilities | trigger/* | — | — | NR-017, NR-018, NR-027, NR-030, NR-033 | none |
| Abilities | Registry of 50 stateless classes | ability/* | Vault (money) | NR-016 | NR-001, NR-004–007, NR-013/14/19/20, NR-029/31 | none |
| Cooldowns | Memory map or SQL rows | cooldown/*, storage/* | — | — | NR-010, NR-011, NR-019 | none |
| Auras | Global repeating task renders per-relic particle patterns | aura/* | — | particle renames | NR-025, NR-016 | none |
| Storage | HikariCP + SQLite (default) / MySQL | storage/* | sqlite-jdbc, MySQL driver from server | — | NR-011, NR-023, NR-024 | none |
| Editor GUI | Chest GUIs + chat input | gui/* | — | — | NR-008, NR-032 | none |
| Commands | `/nr` tree | command/* | — | — | NR-012, NR-022 | none |
| Integrations | Reflection hooks; PAPI expansion | hook/* | optional plugins | Plugin API drift | NR-021 (unused hooks) | none |
| Recipes | Intended YAML recipes | recipe/* | — | — | NR-021 (never registers) | none |
| Localisation | `language/<lang>.yml` + en fallback | config/* | — | — | All used keys present in en/es/ru (verified) | none |
