# NovaRelics

**The most powerful custom relic and survival item framework for Paper Minecraft servers.**

NovaRelics allows server owners to create fully custom relics, charms, artifacts, consumables, keys, and magical equipment — entirely through YAML configuration files. No Java required.

---

## Features

- **30+ built-in abilities** — from healing to vein mining to teleportation
- **28 trigger types** — right-click, anvil, mob kill, block break, movement, and more
- **21 premium aura effects** — spiral, double spiral, pulse, fire, ice, void, celestial, and more
- **7 configurable rarities** — Common through Divine, each with unique colors, sounds, and particles
- **Full skull texture support** — Base64, URL, HeadDatabase, ItemsAdder, Oraxen, Nexo
- **SQLite & MySQL storage** — with HikariCP connection pooling
- **10 optional plugin hooks** — Vault, PAPI, LuckPerms, ItemsAdder, Oraxen, Nexo, HeadDatabase, ProtocolLib, MythicMobs, ModelEngine
- **In-game GUI editor** — create and edit relics without touching files
- **PlaceholderAPI expansion** — 5+ placeholders out of the box
- **Performance-first aura system** — distance checks, chunk awareness, configurable intervals

---

## Installation

1. Download `NovaRelics-1.0.0.jar`
2. Drop it into your `plugins/` folder
3. Restart the server
4. Edit relics in `plugins/NovaRelics/relics/`

**Requirements:** Paper 1.18.2, 1.19.4, 1.20.6, 1.21.11, or 26.1.2 — Java 17+

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/nr help` | Show help | — |
| `/nr list` | List all relics | — |
| `/nr info <id>` | Show relic info | — |
| `/nr give <player> <id> [amount]` | Give relic | `novarelics.give` |
| `/nr take <player> <id>` | Remove relic | `novarelics.take` |
| `/nr editor [id]` | Open GUI editor | `novarelics.editor` |
| `/nr create <id>` | Create blank relic | `novarelics.create` |
| `/nr delete <id>` | Delete relic | `novarelics.delete` |
| `/nr import <file>` | Import relic YAML | `novarelics.import` |
| `/nr export <id>` | Export relic YAML | `novarelics.export` |
| `/nr reload` | Reload configs | `novarelics.reload` |

Aliases: `/relics`, `/nr`

---

## Creating a Relic

Create a file in `plugins/NovaRelics/relics/my_relic.yml`:

```yaml
id: my_relic
display-name: "<gradient:#AA55FF:#55FFFF>My Relic</gradient>"
material: AMETHYST_SHARD
rarity: rare
glow: true

lore:
  - ""
  - "<gray>Right-click to teleport."
  - ""

aura:
  enabled: true
  type: SPIRAL_EFFECT
  particle: ENCHANT
  radius: 1.2
  update-ticks: 5
  only-when-held: true

triggers:
  RIGHT_CLICK:
    - type: TELEPORT
      mode: RANDOM
      radius: 50
    - type: SOUND
      sound: ENTITY_ENDERMAN_TELEPORT
      volume: 1.0

cooldown:
  type: PER_PLAYER
  duration: 30
```

Reload with `/nr reload` and give with `/nr give <player> my_relic`.

---

## Flagship Relics

| Relic | Trigger | Effect |
|-------|---------|--------|
| Mending Wool | Anvil | Fully repairs any item |
| Teleport Crystal | Right-click | Random teleport |
| Vampire Fang | Kill mob | Heal on kill |
| Ore Charm | Break block | Double ore drops |
| Harvest Charm | Break block | Auto-replant crops |

---

## PlaceholderAPI

| Placeholder | Description |
|-------------|-------------|
| `%novarelics_equipped%` | Name of held relic |
| `%novarelics_rarity%` | Rarity of held relic |
| `%novarelics_cooldown_<id>%` | Remaining cooldown seconds |
| `%novarelics_relic_count%` | Total loaded relics |
| `%novarelics_discovered%` | Player's discovered relics |
