# Security & Exploit Audit

Scope: plugin code and bundled defaults. No public servers or external services were contacted. Exploit claims below come from code analysis. Each one will get an isolated test in a disposable environment before and after its fix.

| ID | Threat | Vector | Severity | Status |
|---|---|---|---|---|
| NR-001 | Item duplication | Ore Charm + place/break ancient debris or silk-touch ores | **Critical** | Confirmed (static) |
| NR-004 | Protection bypass / grief | Vein Mine / Tree Feller / Web Trap / Auto Replant change blocks without events | **Critical** | Confirmed (static) |
| NR-005 | Economy bypass | Full Repair result taken without paying | High | Confirmed (static) |
| NR-010 | Cooldown bypass | Disconnect and reconnect | High | Confirmed (static) |
| NR-012 | Arbitrary YAML write/read/delete in the server directory | `/nr create ../../X/config`, `/nr import ../x`, crafted `id:` | High (requires `novarelics.create`/`import`/`delete`) | Confirmed (static) |
| NR-007 | Item destruction | KEEP_ITEM re-adds the item before the server clears the inventory | High | Needs real server |
| NR-013 | Map-boundary escape | Random teleport onto the Nether roof | Medium | Confirmed (static) |
| NR-014 | Phasing through walls | Phantom Strike destination not validated | Medium | Confirmed (static) |
| NR-030 | Use inside protected regions | `ClickTrigger` ignores denied interactions | Medium | Confirmed (static) |
| NR-031 | Heal without damage | Steal Health in PvP-off zones | Low | Confirmed (static) |
| NR-032 | Admin item loss | Drag into editor GUI | Low | Confirmed (static) |
| NR-024 | Credentials in transit | MySQL `useSSL=false` hard-coded | Medium | Confirmed (static) |
| — | SQL injection | All SQL uses `PreparedStatement` with bound parameters; table names constant | None found | Verified |
| — | Command injection | `COMMAND` ability substitutes `%player%` (charset `[A-Za-z0-9_]`) and `%world%` (admin-controlled). Config authors are trusted admins. | Informational | Note: Geyser/Floodgate prefixes like `.` or `*` are still safe for console dispatch |
| — | MiniMessage injection | Player-controlled strings are never parsed as MiniMessage (only player names and admin config) | None found | Verified |
| — | Async Bukkit access | Only `ChatInputManager` handles an async event, and it hops to the main thread before touching state | OK | Verified |
| — | Unbounded task creation | Each ability use schedules at most one task. Spam is bounded by cooldowns, but relics with `cooldown: 0` (Flight, Glow, WebTrap configured by an admin) can queue a task per click. | Low | Recommend minimum cooldown or per-player task dedupe |
| — | Secret handling | MySQL password in plain `config.yml` (normal for Bukkit plugins). No secrets in the repo; default password is literally `password`. | Low | Ship an empty password plus a startup warning when the default is unchanged |
| — | Dependency vulnerabilities | See DEPENDENCY_ANALYSIS.md (sqlite-jdbc 3.43.0.0 is outdated; no OWASP scan has run yet) | Pending | Add OWASP dependency-check / `mvn versions` in CI |
| — | bStats | Sends anonymous usage data (`storage_type` pie). Disclosed via the standard bStats config. | Informational | Mention in README |

## Recommended hardening (summary)
1. Fire real `BlockBreakEvent`s (`Player#breakBlock`) for every multi-block effect, and guard against re-entrancy.
2. Mark player-placed blocks, and exclude self-dropping blocks from drop multipliers.
3. Validate relic ids with a strict regex and a canonical-path check.
4. Never clear unexpired cooldowns on quit.
5. Make payment the first step of any economy-gated action; cancel on failure.
6. Respect `useItemInHand()`/`useInteractedBlock()` DENY and `ignoreCancelled = true` throughout. Move world-mutating effects to MONITOR-safe scheduling only when the event is not cancelled.
