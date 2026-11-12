package dev.aponder.novarelics.abilities;

import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.support.Relics;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KEEP_ITEM (NR-007). The test reproduces the server's death sequence: the death event,
 * then the server clearing the inventory, then respawn.
 * Final confirmation still requires a real server (vanilla death handling is not simulated).
 */
class KeepItemTest extends PluginTestBase {

    private PlayerMock p;

    @BeforeEach
    void setUp() {
        installRelic("soulbound", Relics.single("soulbound", "PLAYER_DEATH", "KEEP_ITEM"));
        p = player();
    }

    private PlayerDeathEvent die(boolean keepInventory) {
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack s : p.getInventory().getContents()) if (s != null) drops.add(s.clone());
        PlayerDeathEvent e = new PlayerDeathEvent(p, org.bukkit.damage.DamageSource.builder(
                org.bukkit.damage.DamageType.GENERIC).build(), drops, 0, "died");
        e.setKeepInventory(keepInventory);
        server.getPluginManager().callEvent(e);
        if (!keepInventory) p.getInventory().clear(); // what the server does after the event
        return e;
    }

    private boolean dropsContainRelic(PlayerDeathEvent e) {
        return e.getDrops().stream().anyMatch(s -> "soulbound".equals(relicId(s)));
    }

    @Test
    @DisplayName("NR-007 relic is removed from drops and returned on respawn")
    void relicReturnedOnRespawn() {
        p.getInventory().setItemInMainHand(relic("soulbound"));
        p.getInventory().addItem(new ItemStack(Material.DIRT, 5));
        PlayerDeathEvent e = die(false);
        assertFalse(dropsContainRelic(e), "relic must not drop");
        assertTrue(e.getDrops().stream().anyMatch(s -> s.getType() == Material.DIRT), "other items still drop");
        assertEquals(0, count(p.getInventory(), "soulbound"), "inventory cleared by the server");

        p.respawn();
        assertEquals(1, count(p.getInventory(), "soulbound"));
        assertFalse(plugin.getKeptItems().hasPending(p));
    }

    @Test
    @DisplayName("NR-007 relic is returned on join if the player logged out while dead")
    void relicReturnedOnJoinAfterLeavingDead() {
        p.getInventory().setItemInMainHand(relic("soulbound"));
        die(false);
        assertTrue(plugin.getKeptItems().hasPending(p));
        p.disconnect();
        p.setHealth(20);
        p.reconnect();
        assertEquals(1, count(p.getInventory(), "soulbound"));
    }

    @Test
    void keepInventoryLeavesEverythingAlone() {
        p.getInventory().setItemInMainHand(relic("soulbound"));
        PlayerDeathEvent e = die(true);
        assertTrue(dropsContainRelic(e), "relic stays in the normal (kept) drops list");
        assertFalse(plugin.getKeptItems().hasPending(p), "nothing stored when keepInventory is on");
        assertEquals(1, count(p.getInventory(), "soulbound"));
    }

    @Test
    void stackAmountPreserved() {
        ItemStack stack = relic("soulbound");
        stack.setAmount(3);
        p.getInventory().setItemInMainHand(stack);
        die(false);
        p.respawn();
        assertEquals(3, count(p.getInventory(), "soulbound"));
    }
}
