# Performance Analysis

**No measurements have been taken yet.** Everything below is static analysis. Tick-time (MSPT), TPS and memory figures can only come from a real server with a profiler (spark), and will be recorded here once the harness runs. No performance gains are claimed.

## Static hotspots (ranked by expected impact)

| # | Hotspot | Where | Why it matters | Expected fix |
|---|---|---|---|---|
| P1 | Synchronous JDBC on the main thread when `cooldown.storage: DATABASE` | `CooldownManager` → `StorageManager.getRemainingCooldown` | Runs on every dispatch (MOVE 1/s/player, every damage and break event) and every cooldown placeholder request. With MySQL, each call is a network round trip (≈0.5–5 ms) on the tick thread. | Memory cache as source of truth; async write-behind; load on pre-login |
| P2 | Synchronous player load on join and `/nr give` | `JoinQuitTrigger`, `GiveSubCommand` | A blocking query on the main thread | `AsyncPlayerPreLoginEvent` load |
| P3 | Random teleport loads chunks synchronously | `TeleportAbility.resolveRandom` | Up to 10 `getHighestBlockYAt` calls, each potentially generating a chunk | Paper `getChunkAtAsync` via the adapter; on Spigot, limit to loaded chunks |
| P4 | Aura pass is O(online² × relics) | `AuraManager.processAuras` | For each wearer × relic, scans all world players (and the check always passes, NR-025); reads config values every pass | Cache config on reload; per-world spatial check once per player; precompute the viewer list per pass |
| P5 | Per-viewer particle loop | `ParticleUtil.spawnForNearby` | Iterates all world players for each particle call; renderers call it many times per frame | `World#spawnParticle(..., force=false)` lets the server's tracking range handle viewers, or pass a precomputed viewer list |
| P6 | Event listeners walk all slots for every event | `dispatchFromAllSlots` on MOVE/break/damage/fish | 6 slots × PDC read per event. Cheap individually, but MOVE is high-frequency (already throttled to 1/s). | Cache "wearer has relic with trigger X" per player, invalidated on inventory change |
| P7 | Vein Mine / Tree Feller up to 64/128 blocks in one tick | abilities | A bounded spike that is acceptable | Optional spread over ticks |
| P8 | PDC reads via `getItemMeta()` clone | `RelicBuilder.getRelicId` | `getItemMeta` copies the meta each call | Paper `ItemStack#getPersistentDataContainer()` (read-only view, 1.21.1+) in the adapter |
| P9 | Unbounded maps | `JumpTrigger.wasOnGround`, `MoveTrigger.lastFire`, memory cooldown map (never pruned except on quit) | Slow growth with unique players | Remove on quit; scheduled prune of expired cooldowns |
| P10 | Shaded jar size 13.7 MB | sqlite natives | Startup I/O and memory; disk | Use the server-provided SQLite driver |

## Planned measurements
| Workload | Tool | Environment |
|---|---|---|
| 50 simulated players, MOVE + damage triggers, MEMORY vs DATABASE(SQLite) vs DATABASE(MySQL) | spark profiler, `/mspt` | Paper 1.21.11 & 26.3, flat world, 4 GB heap |
| Aura rendering with 20 wearers within 32 blocks | spark | same |
| 1,000 `/nr reload` cycles: listener/task/heap growth | MockBukkit (deterministic counts) + heap histogram on real server | — |

MockBukkit workloads will be used only to prove the *absence of growth or exceptions*. They are not evidence of tick performance.
