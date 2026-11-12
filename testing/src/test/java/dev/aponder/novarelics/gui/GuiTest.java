package dev.aponder.novarelics.gui;

import dev.aponder.novarelics.support.PluginTestBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GuiTest extends PluginTestBase {

    private PlayerMock admin;

    @BeforeEach
    void open() {
        admin = op();
        server.dispatchCommand(admin, "nr editor");
    }

    private InventoryView view() { return admin.getOpenInventory(); }

    @Test
    void editorOpensRelicList() {
        assertInstanceOf(RelicListGui.class, view().getTopInventory().getHolder());
        assertEquals(54, view().getTopInventory().getSize());
        assertNotNull(view().getTopInventory().getItem(0), "first relic shown");
    }

    @Test
    void clicksInGuiAreCancelled() {
        var e = admin.simulateInventoryClick(view(), ClickType.LEFT, 49);
        assertTrue(e.isCancelled());
        var shift = admin.simulateInventoryClick(view(), ClickType.SHIFT_LEFT, 60); // bottom inventory
        assertTrue(shift.isCancelled(), "shift-click from own inventory into the GUI is blocked");
    }

    @Test
    void clickingRelicOpensItsEditor() {
        admin.simulateInventoryClick(view(), ClickType.LEFT, 0);
        server.getScheduler().performOneTick();
        assertInstanceOf(RelicEditorGui.class, view().getTopInventory().getHolder());
    }

    @Test
    void editorOpenedForUnknownRelicReportsIt() {
        server.dispatchCommand(admin, "nr editor nope");
        assertTrue(messages(admin).stream().anyMatch(m -> m.contains("was not found")));
    }

    @Test
    void rarityButtonCyclesAndSaves() {
        server.dispatchCommand(admin, "nr editor ore_charm");
        String before = plugin.getRelicManager().getRelic("ore_charm").getRarity();
        admin.simulateInventoryClick(view(), ClickType.LEFT, 16);
        assertNotEquals(before, plugin.getRelicManager().getRelic("ore_charm").getRarity());
    }

    @Test
    void closingUnregistersGuiListener() {
        int before = org.bukkit.event.HandlerList.getRegisteredListeners(plugin).size();
        admin.closeInventory();
        assertTrue(org.bukkit.event.HandlerList.getRegisteredListeners(plugin).size() < before);
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-032 dragging items into the editor GUI is cancelled")
    void dragIntoGuiIsCancelled() {
        ItemStack cursor = new ItemStack(Material.DIAMOND, 2);
        InventoryDragEvent drag = new InventoryDragEvent(view(), null, cursor, false,
                Map.of(10, new ItemStack(Material.DIAMOND, 1), 11, new ItemStack(Material.DIAMOND, 1)));
        server.getPluginManager().callEvent(drag);
        assertTrue(drag.isCancelled(), "items dragged into the GUI would be deleted on the next refresh");
    }
}
