# API Compatibility Audit

The compile target is `io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT`. `plugin.yml` declares `api-version: 1.18`.

## Key mechanism to understand

Because `api-version` is older than the running server, CraftBukkit (and Paper, which hooks the same code) rewrites the plugin's bytecode at load time:

- `Commodore` performs the rewriting.
- `FieldRename` maps renamed enum constants and registry fields.

Many old field references (for example `Particle.SPELL_WITCH`) only *link* because of this shim. Two consequences follow:
1. **Compiling against a newer API does not prove anything about older servers**, and compiling against 1.18.2 does not prove anything about newer ones.
2. **String-based lookups** from user config (`Particle.valueOf(cfg)`, `PotionEffectType.getByName(cfg)`, `Sound.valueOf(cfg)`, `Material.valueOf(cfg)`) are not covered by field renames. Whether a config name works depends on the server version.

Every row marked *runtime-verify* must be confirmed on a real server. MockBukkit runs the plugin against a single API version without Commodore, so it cannot prove these.

## Executed evidence (Phase 0)
- **Static link check** (`testing/.../compat/ApiCompatibilityTest`) against all 44 API jars from 1.20 to 26.3. Its results are summarised in `MINECRAFT_VERSION_MATRIX.md` and confirm every *runtime-verify* row below, along with the exact version where each one appears:
  - the potion-effect and particle renames: 1.20.5;
  - `Attribute.GENERIC_MAX_HEALTH` and `Sound` becoming an interface: 1.21.3;
  - Adventure and the 4 Paper-only methods: every Spigot version.
- **MockBukkit runtime:** without bytecode rewriting, the plugin fails to enable on the 1.21.11 and 26.3 APIs with `NoSuchFieldError: PotionEffectType.CONFUSION` thrown from `CureAbility`'s static initialiser. This confirms the "aborts plugin enable" risk noted below.
- **Remaining unknown:** whether real Paper/Spigot servers' Commodore covers all 8 symbols for an `api-version: 1.18` plugin. This is the first job for the real-server matrix.

## Findings by API area

| Area | Usage | Problem | Affected versions | Status | Recommended change |
|---|---|---|---|---|---|
| Adventure / MiniMessage | `MiniMsg` (provided scope) | Not present on Spigot → `NoClassDefFoundError` on enable | Spigot all | Confirmed static (NR-003) | Shade + relocate adventure-api/minimessage/legacy serializer |
| Legacy serialization | `legacySection()` without hex | Hex/gradients down-sampled | All | **Confirmed executed** (NR-009) | Builder with `hexColors()` + `useUnusualXRepeatedCharacterHexFormat()` |
| Player#sendActionBar(String) | ActionBarAbility | Paper-only method | Spigot all | Confirmed static | `player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(...))` |
| LivingEntity#getTargetEntity | PhantomStrikeAbility | Paper-only | Spigot all | Confirmed static | `world.rayTraceEntities(eye, dir, dist, e -> e != player)` |
| PlayerDeathEvent#setCancelled | PreventDeathAbility | Paper-only `Cancellable` on death event | Spigot all | Confirmed static | Cancel lethal `EntityDamageEvent` (works on both) |
| Attribute.GENERIC_MAX_HEALTH | Heal, GainHealth, NearbyHeal, StealHealth, PreventDeath | Renamed `MAX_HEALTH` in 1.21.3; `Attribute` became an interface/registry | ≥1.21.3 | Runtime-verify (Commodore) | Resolve via `Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"))` with fallback to `generic.max_health` |
| PotionEffectType.CONFUSION/HARM/SLOW/SLOW_DIGGING | CureAbility static init | Renamed in 1.20.5 (NAUSEA/INSTANT_DAMAGE/SLOWNESS/MINING_FATIGUE) | ≥1.20.5 | Runtime-verify. A failure here is a static-init error, which makes the whole CureAbility class fail and **aborts AbilityRegistry construction → plugin enable fails** | Resolve by key via `Registry.EFFECT` / `PotionEffectType.getByKey` |
| PotionEffectType.getByName(cfg) | PotionEffectAbility | Deprecated; legacy names vs keys | All | Runtime-verify | Try `NamespacedKey.minecraft(lower)` then legacy alias table |
| Particle.CRIT_MAGIC / SPELL_WITCH (direct) | Magic/Shadow aura renderers | Renamed ENCHANTED_HIT / WITCH in 1.20.5 | ≥1.20.5 | Runtime-verify | Resolve by name through `ParticleUtil.resolveParticle(new, old)` |
| Particle.valueOf(cfg) | ParticleUtil.getParticle | Config uses 1.20.5+ names (`ENCHANT`, `TOTEM`); `TOTEM` is invalid ≥1.20.5, `ENCHANT` invalid <1.20.5 | 1.20–1.20.4 and ≥1.20.5 respectively | Confirmed static (silent fallback to ENCHANT) | Alias table both directions + warning on unknown |
| Particle requiring data (DUST, DUST_COLOR_TRANSITION, BLOCK, ITEM) | ParticleAbility/auras when configured | `spawnParticle` without data throws | All | Confirmed static | Detect `particle.getDataType()` and build default data |
| Sound.valueOf / RarityDefinition | SoundAbility | `Sound` became interface/registry in 1.21.3; `valueOf` deprecated | ≥1.21.3 | Runtime-verify | Use `player.playSound(loc, "minecraft:key", …)` string overload (stable on every version) |
| Enchantment.getByName | RelicLoader | Deprecated; legacy names (`DURABILITY`, `DAMAGE_ALL`) renamed in 1.20.5 | All | Runtime-verify | `Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key))` + alias table |
| Enchantment.SILK_TOUCH (field) | DoubleDrops | Stable name | — | OK | — |
| ItemMeta#setCustomModelData(int) | RelicBuilder | Superseded by `CustomModelDataComponent` in 1.21.4; int setter still works but is deprecated | ≥1.21.4 | Deprecated, functional | Keep; add component path in Paper adapter later |
| Unbreaking glow trick | RelicBuilder | 1.20.5+ has `ItemMeta#setEnchantmentGlintOverride(Boolean)` | ≥1.20.5 | Works, but adds a real Unbreaking I enchant (gameplay effect!) | Use glint override when the method exists, fall back to the hidden enchant |
| CraftMetaSkull reflection | SkullTextureUtil | Field became `ResolvableProfile` in 1.20.5 | ≥1.20.5 | Runtime-verify (NR-015) | `PlayerProfile` + `PlayerTextures` API (1.18.1+) |
| EntityType.valueOf / Material.valueOf | SpawnEntity, GiveItem, editor | Names changed (`SNOWMAN`→`SNOW_GOLEM` 1.20.5, etc.) | varies | Runtime-verify | `Material.matchMaterial` + `Registry.ENTITY_TYPE` |
| SmithingInventory slot 3 result | SmithingTableTrigger | Correct for 1.20+ (template slot added in 1.20) | — | OK | — |
| AsyncPlayerChatEvent | ChatInputManager | Deprecated on Paper (still fired) | Paper | OK | Keep for Spigot; optional `AsyncChatEvent` in Paper adapter |
| `getDescription()` | NovaRelics, expansion | Deprecated on Paper (`getPluginMeta`) | Paper | OK, warning only | Keep (works on both) |
| Bukkit version string parsing | Version gate | Brittle; blocks valid versions | All | Confirmed static (NR-002) | Remove |
| PersistentDataContainer `has(key, type)` | RelicBuilder | Stable since 1.14 | — | OK | — |
| Scheduler (BukkitScheduler) | auras, abilities, storage | Folia not supported (not required) | — | OK | Note in docs |
| `plugin.yml` | `api-version: 1.18` | Below supported floor; triggers legacy rewriting paths | All | Maintainability | Raise to `1.20` *after* replacing renamed constants (raising first would break NR-016 items) |
| `paper-plugin.yml` | absent | Not needed; using bukkit `plugin.yml` keeps Spigot support | — | OK | Do not add |

## Spigot vs Paper isolation design (proposal)

```
dev.aponder.novarelics
├── platform/Platform.java          (interface: actionBar, rayTraceEntity, keepOnDeath, setDisplayName(Component|legacy))
├── platform/SpigotPlatform.java    (pure Bukkit/Spigot API — always loadable)
└── platform/paper/PaperPlatform.java (only referenced via Class.forName after
                                     capability probe "io.papermc.paper.event.player.AsyncChatEvent")
```

- Selection happens once in `onEnable`. Paper classes are never referenced from shared code, so they cannot be loaded on Spigot.
- Compile against `spigot-api` for the core. Compile the paper package against `paper-api` with `<optional>` scope, or with a separate source set.
