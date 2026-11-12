# Feature Opportunities

These are optional enhancements, kept separate from defects. None of them is required for correctness.

| Opportunity | Value | Effort | Notes |
|---|---|---|---|
| Finish **charges** (`max-charges` + existing `nr_charges` table) | Consumable or limited relics, a common request | M | Store charges in the item PDC rather than SQL, so they travel with the item and need no DB |
| Finish **recipes** (parse a `recipes:` YAML section) | Craftable relics | S–M | Loader already reserves `customData`; wire it up and add a `RecipeChoice.ExactChoice` for relic ingredients |
| Use rarity `sound` / `particles` / `glow` | Rarity identity on give and drop | S | Fields are already parsed |
| `/nr give` to offline players or `@a` | Admin convenience | S | |
| Conditions per ability (`chance`, `world`, `biome`, `health-below`, `sneaking`) | Much richer configs without code | M | Generalises the existing per-ability `chance` |
| Paper-only enhancements behind the adapter: native Components in lore, `PlayerArmorChangeEvent`, `PlayerJumpEvent`, async chunk teleport | Better fidelity on Paper; Spigot unchanged | M | Requires the platform split (roadmap phase 2) |
| Public API (`NovaRelicsAPI`, custom `RelicTriggerEvent`, `AbilityExecuteEvent` cancellable) | Lets other plugins react to or extend relics | M | Also makes `CUSTOM_EVENT` meaningful |
| Config migration (`config-version`) | Safe upgrades | S | Needed before renaming any keys |
| Folia support | Regionised servers | L | Not requested; needs region scheduler abstraction |
| Editor: triggers and abilities in the GUI | Fewer YAML edits | L | Only after NR-008 is fixed |
| bStats charts: relic count, enabled triggers | Usage insight | XS | |
