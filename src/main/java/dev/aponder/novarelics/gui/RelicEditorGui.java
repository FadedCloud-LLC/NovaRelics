package dev.aponder.novarelics.gui;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.rarity.RarityDefinition;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class RelicEditorGui implements InventoryHolder, Listener {

    private final NovaRelics plugin;
    private final Player player;
    private final RelicDefinition relic;
    private final Inventory inventory;

    public RelicEditorGui(NovaRelics plugin, Player player, RelicDefinition relic) {
        this.plugin = plugin;
        this.player = player;
        this.relic = relic;
        this.inventory = Bukkit.createInventory(this, 54,
                MiniMsg.toLegacy("<gradient:#AA55FF:#55FFFF>Edit</gradient> <gray>— " + relic.getId()));
        populate();
    }

    private List<String> rarityIds() {
        return new ArrayList<>(
                plugin.getRarityManager().getRarities().stream()
                        .map(RarityDefinition::getId).toList());
    }

    private void populate() {
        inventory.clear();

        inventory.setItem(4, plugin.getRelicManager().buildItem(relic));

        inventory.setItem(10, GuiUtils.createItem(Material.NAME_TAG,
                "<yellow>Edit Name",
                "<gray>Current: <white>" + relic.getDisplayName(),
                "<dark_gray>Click to change via chat"));

        inventory.setItem(12, GuiUtils.createItem(Material.GRASS_BLOCK,
                "<yellow>Edit Material",
                "<gray>Current: <white>" + relic.getMaterial().name(),
                "<dark_gray>Click to change via chat"));

        inventory.setItem(14, GuiUtils.createItem(Material.BOOK,
                "<yellow>Edit Lore",
                "<gray>Lines: <white>" + relic.getLore().size(),
                "<dark_gray>Click to add a line via chat",
                "<dark_gray>Shift-click to clear all lore"));

        List<String> ids = rarityIds();
        int idx = ids.indexOf(relic.getRarity().toLowerCase());
        if (idx < 0) idx = 0;
        String prev = ids.get(idx == 0 ? ids.size() - 1 : idx - 1);
        String next = ids.get((idx + 1) % ids.size());
        inventory.setItem(16, GuiUtils.createItem(Material.DIAMOND,
                "<yellow>Edit Rarity",
                "<gray>Current: <white>" + relic.getRarity(),
                "<dark_gray>← " + prev + "  |  " + next + " →",
                "<dark_gray>Click to cycle"));

        inventory.setItem(28, GuiUtils.createItem(Material.COMPARATOR,
                "<yellow>Triggers & Abilities",
                "<gray>Triggers defined: <white>" + relic.getTriggers().size(),
                "<dark_gray>Edit via the relic's YAML file"));

        inventory.setItem(30, GuiUtils.createItem(Material.CLOCK,
                "<yellow>Edit Cooldown",
                "<gray>Current: <white>" + relic.getCooldownSeconds() + "s",
                "<dark_gray>Click to change via chat"));

        inventory.setItem(32, GuiUtils.createItem(Material.BLAZE_ROD,
                "<yellow>Toggle Aura",
                "<gray>Enabled: " + (relic.getAura().isEnabled() ? "<green>Yes" : "<red>No"),
                "<dark_gray>Click to toggle"));

        inventory.setItem(34, GuiUtils.createItem(Material.GLOWSTONE_DUST,
                "<yellow>Toggle Glow",
                "<gray>Glow: " + (relic.isGlow() ? "<green>Yes" : "<red>No"),
                "<dark_gray>Click to toggle"));

        inventory.setItem(47, GuiUtils.createItem(Material.LIME_DYE,
                "<green><bold>Save Relic",
                "<gray>Saves current state to file"));

        inventory.setItem(48, GuiUtils.createItem(Material.RED_DYE,
                "<red>Delete Relic",
                "<gray>Removes the relic permanently",
                "<dark_gray>Cannot be undone"));

        inventory.setItem(50, GuiUtils.createItem(Material.CYAN_DYE,
                "<aqua>Duplicate Relic",
                "<gray>Creates: <white>" + relic.getId() + "_copy"));

        inventory.setItem(53, GuiUtils.createItem(Material.ARROW,
                "<gray>← Back to List"));

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, GuiUtils.gray());
        }
    }

    public void open() {
        player.openInventory(inventory);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player clicker)) return;

        boolean shift = event.isShiftClick();

        switch (event.getRawSlot()) {
            case 10 -> plugin.getChatInputManager().request(clicker,
                    "<yellow>Enter new display name (MiniMessage format):",
                    input -> {
                        relic.setDisplayName(input);
                        plugin.getRelicManager().save(relic);
                        new RelicEditorGui(plugin, clicker, relic).open();
                    });

            case 12 -> plugin.getChatInputManager().request(clicker,
                    "<yellow>Enter material name (e.g. <white>DIAMOND_SWORD<yellow>):",
                    input -> {
                        try {
                            relic.setMaterial(Material.valueOf(input.toUpperCase().replace(" ", "_")));
                            plugin.getRelicManager().save(relic);
                        } catch (IllegalArgumentException e) {
                            MiniMsg.send(clicker, MiniMsg.parse("<red>Unknown material: <white>" + input));
                        }
                        new RelicEditorGui(plugin, clicker, relic).open();
                    });

            case 14 -> {
                if (shift) {
                    relic.getLore().clear();
                    plugin.getRelicManager().save(relic);
                    populate();
                } else {
                    plugin.getChatInputManager().request(clicker,
                            "<yellow>Type a lore line to add (MiniMessage format):",
                            input -> {
                                relic.getLore().add(input);
                                plugin.getRelicManager().save(relic);
                                new RelicEditorGui(plugin, clicker, relic).open();
                            });
                }
            }

            case 16 -> {
                List<String> ids = rarityIds();
                int idx = ids.indexOf(relic.getRarity().toLowerCase());
                if (idx < 0) idx = 0;
                relic.setRarity(ids.get((idx + 1) % ids.size()));
                plugin.getRelicManager().save(relic);
                populate();
            }

            case 28 -> MiniMsg.send(clicker, MiniMsg.parse(
                    "<yellow>Edit triggers in the relic's YAML file, then run <white>/nr reload</white>."));

            case 30 -> plugin.getChatInputManager().request(clicker,
                    "<yellow>Enter cooldown in seconds (0 to disable):",
                    input -> {
                        try {
                            relic.setCooldownSeconds(Math.max(0, Long.parseLong(input)));
                            plugin.getRelicManager().save(relic);
                        } catch (NumberFormatException e) {
                            MiniMsg.send(clicker, MiniMsg.parse("<red>Invalid number: <white>" + input));
                        }
                        new RelicEditorGui(plugin, clicker, relic).open();
                    });

            case 32 -> {
                relic.getAura().setEnabled(!relic.getAura().isEnabled());
                plugin.getRelicManager().save(relic);
                populate();
            }

            case 34 -> {
                relic.setGlow(!relic.isGlow());
                plugin.getRelicManager().save(relic);
                populate();
            }

            case 47 -> {
                plugin.getRelicManager().save(relic);
                MiniMsg.send(clicker, MiniMsg.parse(
                        plugin.getConfigManager().getMessage("editor-created",
                                "<id>", relic.getId())));
            }

            case 48 -> Bukkit.getScheduler().runTask(plugin, () -> {
                clicker.closeInventory();
                plugin.getRelicManager().delete(relic.getId());
                MiniMsg.send(clicker, MiniMsg.parse(
                        plugin.getConfigManager().getMessage("editor-deleted",
                                "<id>", relic.getId())));
            });

            case 50 -> Bukkit.getScheduler().runTask(plugin, () -> {
                clicker.closeInventory();
                String newId = relic.getId() + "_copy";
                plugin.getRelicManager().duplicate(relic.getId(), newId);
                MiniMsg.send(clicker, MiniMsg.parse(
                        plugin.getConfigManager().getMessage("editor-duplicated",
                                "<id>", relic.getId(), "<new_id>", newId)));
            });

            case 53 -> Bukkit.getScheduler().runTask(plugin, () -> {
                clicker.closeInventory();
                new RelicListGui(plugin, clicker, 0).open();
            });
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
