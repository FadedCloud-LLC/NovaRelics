package dev.aponder.novarelics.cooldown;

import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.support.Relics;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

class CooldownTest extends PluginTestBase {

    private PlayerMock p;
    private CooldownManager cd;

    @BeforeEach
    void setUp() {
        installRelic("timed", Relics.probe("timed", 60, "RIGHT_CLICK", "SNEAK"));
        installRelic("quick", Relics.probe("quick", 1, "RIGHT_CLICK"));
        installRelic("free", Relics.probe("free", 0, "RIGHT_CLICK"));
        p = player();
        cd = plugin.getCooldownManager();
    }

    private void click(String relicId) {
        p.getInventory().setItemInMainHand(relic(relicId));
        server.getPluginManager().callEvent(new PlayerInteractEvent(p, Action.RIGHT_CLICK_AIR,
                p.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND));
    }

    private long firedCount() {
        return messages(p).stream().filter(m -> m.startsWith("fired")).count();
    }

    private RelicDefinition def(String id) { return plugin.getRelicManager().getRelic(id); }

    @Test
    void cooldownBlocksSecondUseAndNotifies() {
        click("timed");
        click("timed");
        var msgs = messages(p);
        assertEquals(1, msgs.stream().filter(m -> m.startsWith("fired")).count());
        // first message drained above: re-run to see notice
        click("timed");
        assertTrue(messages(p).stream().anyMatch(m -> m.contains("cooldown")));
        long remaining = cd.getRemainingSeconds(p, def("timed"));
        assertTrue(remaining > 55 && remaining <= 60, "remaining=" + remaining);
    }

    @Test
    void zeroCooldownNeverBlocks() {
        for (int i = 0; i < 5; i++) click("free");
        assertEquals(5, firedCount());
        assertFalse(cd.isOnCooldown(p, def("free")));
    }

    @Test
    void cooldownExpires() throws InterruptedException {
        click("quick");
        assertTrue(cd.isOnCooldown(p, def("quick")));
        Thread.sleep(1100);
        assertFalse(cd.isOnCooldown(p, def("quick")));
        click("quick");
        assertEquals(2, firedCount());
    }

    @Test
    void cooldownIsPerPlayerByDefault() {
        click("timed");
        PlayerMock other = player();
        assertFalse(cd.isOnCooldown(other, def("timed")));
    }

    @Test
    void globalCooldownIsShared() {
        installRelic("world_boss", Relics.probe("world_boss", 60, "RIGHT_CLICK").replace("PER_PLAYER", "GLOBAL"));
        click("world_boss");
        assertTrue(cd.isOnCooldown(player(), def("world_boss")));
    }

    @Test
    void sharedKeyLinksRelics() {
        installRelic("s1", Relics.probe("s1", 60, "RIGHT_CLICK").replace("shared-key: \"\"", "shared-key: grp"));
        installRelic("s2", Relics.probe("s2", 60, "RIGHT_CLICK").replace("shared-key: \"\"", "shared-key: grp"));
        click("s1");
        assertTrue(cd.isOnCooldown(p, def("s2")));
    }

    @Test
    void clearCooldown() {
        click("timed");
        cd.clearCooldown(p, def("timed"));
        assertFalse(cd.isOnCooldown(p, def("timed")));
    }

    @Test
    void placeholderStyleRemainingIsZeroWhenReady() {
        assertEquals(0, cd.getRemainingSeconds(p, def("timed")));
    }

    // ---------------------------------------------------------------- regressions

    @Test
    @DisplayName("NR-010 reconnecting does not reset cooldowns")
    void cooldownSurvivesReconnect() {
        click("timed");
        p.disconnect();
        p.reconnect();
        assertTrue(cd.isOnCooldown(p, def("timed")), "cooldown wiped by quit");
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-019 COOLDOWN_REDUCTION works with the default MEMORY cooldown storage")
    void cooldownReductionInMemoryMode() {
        installRelic("reducer", """
                id: reducer
                material: STICK
                triggers:
                  RIGHT_CLICK:
                    - type: COOLDOWN_REDUCTION
                      target-relic: timed
                      reduction: 50
                """);
        click("timed");
        click("reducer");
        long remaining = cd.getRemainingSeconds(p, def("timed"));
        assertTrue(remaining <= 30, "expected <= 30 s after a 50 % reduction, was " + remaining);
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-027 the cooldown notice is not repeated on every passive event")
    void cooldownNoticeIsThrottled() {
        p.getInventory().setItemInOffHand(relic("timed"));
        p.simulateSneak(true);
        messages(p);
        for (int i = 0; i < 10; i++) { p.simulateSneak(false); p.simulateSneak(true); }
        long notices = messages(p).stream().filter(m -> m.contains("cooldown")).count();
        assertTrue(notices <= 1, "cooldown notice sent " + notices + " times in one second");
    }
}
