package dev.aponder.novarelics.gui;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RelicListGui implements InventoryHolder, Listener {

    private final NovaRelics plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<RelicDefinition> relics;
    private int page;
    private static final int ITEMS_PER_PAGE = 45;

    public RelicListGui(NovaRelics plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.relics = new ArrayList<>(plugin.getRelicManager().getAllRelics());
        this.inventory = Bukkit.createInventory(this, 54,
                MiniMsg.toLegacy("<gradient:#AA55FF:#55FFFF>NovaRelics</gradient> <gray>— Relic List"));
        populate();
    }

    private void populate() {
        inventory.clear();

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, relics.size());

        for (int i = start; i < end; i++) {
            RelicDefinition relic = relics.get(i);
            ItemStack item = plugin.getRelicManager().buildItem(relic);
            inventory.setItem(i - start, item);
        }

        // Navigation
        if (page > 0) {
            inventory.setItem(45, GuiUtils.createItem(org.bukkit.Material.ARROW,
                    "<yellow>← Previous Page"));
        }
        inventory.setItem(49, GuiUtils.createItem(org.bukkit.Material.BOOK,
                "<gray>Page " + (page + 1) + "/" + Math.max(1, (int) Math.ceil(relics.size() / (double) ITEMS_PER_PAGE))));
        if (end < relics.size()) {
            inventory.setItem(53, GuiUtils.createItem(org.bukkit.Material.ARROW,
                    "<yellow>Next Page →"));
        }

        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, GuiUtils.gray());
            }
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

        int slot = event.getRawSlot();
        if (slot == 45 && page > 0) {
            int prevPage = page - 1;
            Bukkit.getScheduler().runTask(plugin, () -> {
                clicker.closeInventory();
                new RelicListGui(plugin, clicker, prevPage).open();
            });
        } else if (slot == 53) {
            int nextPage = page + 1;
            Bukkit.getScheduler().runTask(plugin, () -> {
                clicker.closeInventory();
                new RelicListGui(plugin, clicker, nextPage).open();
            });
        } else if (slot >= 0 && slot < ITEMS_PER_PAGE) {
            int relicIndex = page * ITEMS_PER_PAGE + slot;
            if (relicIndex < relics.size()) {
                RelicDefinition selected = relics.get(relicIndex);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    clicker.closeInventory();
                    new RelicEditorGui(plugin, clicker, selected).open();
                });
            }
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
