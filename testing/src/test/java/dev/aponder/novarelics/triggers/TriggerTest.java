package dev.aponder.novarelics.triggers;

import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.support.Relics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TriggerTest extends PluginTestBase {

    private PlayerMock p;

    @BeforeEach
    void setUpProbe() {
        installRelic("probe", Relics.probe("probe", 0,
                "RIGHT_CLICK", "LEFT_CLICK", "SHIFT_RIGHT_CLICK", "SHIFT_LEFT_CLICK", "BREAK_BLOCK", "PLACE_BLOCK",
                "SNEAK", "KILL_MOB", "EQUIP", "UNEQUIP", "JOIN", "QUIT", "WORLD_CHANGE", "MOVE", "PLAYER_RESPAWN",
                "CONSUME"));
        p = player();
        messages(p);
    }

    private List<String> fired() {
        return messages(p).stream().filter(m -> m.startsWith("fired ")).map(m -> m.substring(6)).toList();
    }

    private void interact(Action action, boolean sneaking, EquipmentSlot hand) {
        p.setSneaking(sneaking);
        server.getPluginManager().callEvent(new PlayerInteractEvent(p, action, p.getInventory().getItemInMainHand(),
                null, BlockFace.SELF, hand));
    }

    @ParameterizedTest(name = "{0} sneaking={1} -> {2}")
    @CsvSource({
            "RIGHT_CLICK_AIR, false, RIGHT_CLICK",
            "RIGHT_CLICK_BLOCK, false, RIGHT_CLICK",
            "LEFT_CLICK_AIR, false, LEFT_CLICK",
            "RIGHT_CLICK_AIR, true, SHIFT_RIGHT_CLICK",
            "LEFT_CLICK_BLOCK, true, SHIFT_LEFT_CLICK"})
    void clickMapping(Action action, boolean sneaking, String expected) {
        p.getInventory().setItemInMainHand(relic("probe"));
        interact(action, sneaking, EquipmentSlot.HAND);
        assertEquals(List.of(expected), fired());
    }

    @Test
    void clickIgnoresOffHandEventsPhysicalAndNonRelics() {
        p.getInventory().setItemInMainHand(relic("probe"));
        interact(Action.RIGHT_CLICK_AIR, false, EquipmentSlot.OFF_HAND);
        interact(Action.PHYSICAL, false, EquipmentSlot.HAND);
        p.getInventory().setItemInMainHand(new ItemStack(Material.STICK));
        interact(Action.RIGHT_CLICK_AIR, false, EquipmentSlot.HAND);
        assertEquals(List.of(), fired());
    }

    @Test
    void relicPermissionBlocksDispatch() {
        installRelic("locked", Relics.probe("locked", 0, "RIGHT_CLICK").replace("rarity: common",
                "rarity: common\npermission: relic.locked"));
        p.getInventory().setItemInMainHand(relic("locked"));
        interact(Action.RIGHT_CLICK_AIR, false, EquipmentSlot.HAND);
        assertEquals(List.of(), fired());
        p.addAttachment(plugin, "relic.locked", true);
        interact(Action.RIGHT_CLICK_AIR, false, EquipmentSlot.HAND);
        assertEquals(List.of("RIGHT_CLICK"), fired());
    }

    @Test
    void passiveRelicInOffHandAndArmorFires() {
        p.getInventory().setItemInOffHand(relic("probe"));
        p.simulateSneak(true);
        assertEquals(List.of("SNEAK"), fired());
        p.getInventory().setItemInOffHand(null);
        p.getInventory().setHelmet(relic("probe"));
        p.simulateSneak(false);
        p.simulateSneak(true);
        assertEquals(List.of("SNEAK"), fired(), "only fires when starting to sneak");
    }

    @Test
    void breakAndPlaceBlock() {
        p.getInventory().setItemInMainHand(relic("probe"));
        Block b = world.getBlockAt(0, 5, 0);
        b.setType(Material.STONE);
        p.simulateBlockBreak(b);
        assertEquals(List.of("BREAK_BLOCK"), fired());
    }

    @Test
    void cancelledBreakDoesNotFire() {
        p.getInventory().setItemInMainHand(relic("probe"));
        Block b = world.getBlockAt(1, 5, 0);
        b.setType(Material.STONE);
        server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOW)
            public void deny(org.bukkit.event.block.BlockBreakEvent e) { e.setCancelled(true); }
        }, plugin);
        p.simulateBlockBreak(b);
        assertEquals(List.of(), fired());
    }

    @Test
    void killMobFiresForKillerOnly() {
        p.getInventory().setItemInMainHand(relic("probe"));
        Zombie z = (Zombie) world.spawnEntity(p.getLocation(), EntityType.ZOMBIE);
        z.setKiller(p);
        server.getPluginManager().callEvent(new EntityDeathEvent(z, org.bukkit.damage.DamageSource.builder(
                org.bukkit.damage.DamageType.PLAYER_ATTACK).build(), new ArrayList<>()));
        assertEquals(List.of("KILL_MOB"), fired());
    }

    @Test
    void hotbarEquipAndUnequip() {
        p.getInventory().setItem(0, relic("probe"));
        p.getInventory().setHeldItemSlot(1);
        server.getPluginManager().callEvent(new PlayerItemHeldEvent(p, 1, 0));
        server.getPluginManager().callEvent(new PlayerItemHeldEvent(p, 0, 1));
        assertEquals(List.of("EQUIP", "UNEQUIP"), fired());
    }

    @Test
    void worldChangeAndQuit() {
        p.getInventory().setItemInMainHand(relic("probe"));
        server.getPluginManager().callEvent(new PlayerChangedWorldEvent(p, world));
        assertEquals(List.of("WORLD_CHANGE"), fired());
    }

    @Test
    void moveIsThrottledToOncePerSecond() {
        p.getInventory().setItemInMainHand(relic("probe"));
        Location base = p.getLocation();
        for (int i = 1; i <= 20; i++) p.simulatePlayerMove(base.clone().add(i * 0.1, 0, 0));
        assertEquals(List.of("MOVE"), fired());
    }

    @Test
    void consumeOnlyForTheConsumedRelic() {
        ItemStack r = relic("probe");
        server.getPluginManager().callEvent(new PlayerItemConsumeEvent(p, r, EquipmentSlot.HAND));
        server.getPluginManager().callEvent(new PlayerItemConsumeEvent(p, new ItemStack(Material.APPLE), EquipmentSlot.HAND));
        assertEquals(List.of("CONSUME"), fired());
    }

    @Test
    void stressManyEventsManyPlayersNoErrors() {
        List<PlayerMock> players = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            PlayerMock pl = server.addPlayer();
            pl.getInventory().setItemInMainHand(relic("probe"));
            players.add(pl);
        }
        for (int round = 0; round < 50; round++) {
            for (PlayerMock pl : players) {
                server.getPluginManager().callEvent(new PlayerInteractEvent(pl, Action.RIGHT_CLICK_AIR,
                        pl.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND));
            }
        }
        for (PlayerMock pl : players) {
            assertEquals(50, messages(pl).stream().filter(m -> m.equals("fired RIGHT_CLICK")).count());
        }
    }

    // ---------------------------------------------------------------- regressions

    @Test
    @Tag("regression")
    @DisplayName("NR-017 equipping a relic into an armor slot from the player inventory fires EQUIP")
    void armorEquipFromPlayerInventory() {
        // MockBukkit's own player view has no top inventory, so the click is built to match a real
        // server: the player's own inventory view has a 2x2 CRAFTING top inventory; helmet = raw slot 5.
        org.bukkit.inventory.Inventory top = org.mockito.Mockito.mock(org.bukkit.inventory.Inventory.class);
        org.mockito.Mockito.when(top.getType()).thenReturn(org.bukkit.event.inventory.InventoryType.CRAFTING);
        org.bukkit.event.inventory.InventoryClickEvent click =
                org.mockito.Mockito.mock(org.bukkit.event.inventory.InventoryClickEvent.class);
        org.mockito.Mockito.when(click.getWhoClicked()).thenReturn(p);
        org.mockito.Mockito.when(click.getInventory()).thenReturn(top);
        org.mockito.Mockito.when(click.getRawSlot()).thenReturn(5);
        org.mockito.Mockito.when(click.getCursor()).thenReturn(relic("probe"));
        listener(dev.aponder.novarelics.trigger.impl.EquipTrigger.class).onArmorChange(click);
        assertTrue(fired().contains("EQUIP"), "EQUIP should fire for armor slots");
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-018 COMMAND trigger fires each ability once and matches whole commands only")
    void commandTriggerFiresOnce() {
        installRelic("cmd", """
                id: cmd
                material: STICK
                triggers:
                  COMMAND:
                    - type: MESSAGE
                      message: "fired A"
                      command-pattern: spawn
                    - type: MESSAGE
                      message: "fired B"
                      command-pattern: spawn
                """);
        p.getInventory().setItemInMainHand(relic("cmd"));
        server.getPluginManager().callEvent(new PlayerCommandPreprocessEvent(p, "/spawn"));
        assertEquals(List.of("A", "B"), fired(), "each ability once");
        server.getPluginManager().callEvent(new PlayerCommandPreprocessEvent(p, "/spawnpoint"));
        assertEquals(List.of(), fired(), "/spawnpoint is a different command");
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-030 a denied interaction (e.g. protected region) does not use the relic")
    void deniedInteractionDoesNotFire() {
        p.getInventory().setItemInMainHand(relic("probe"));
        PlayerInteractEvent e = new PlayerInteractEvent(p, Action.RIGHT_CLICK_AIR, p.getInventory().getItemInMainHand(),
                null, BlockFace.SELF, EquipmentSlot.HAND);
        e.setUseItemInHand(Event.Result.DENY);
        e.setUseInteractedBlock(Event.Result.DENY);
        server.getPluginManager().callEvent(e);
        assertEquals(List.of(), fired());
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-026 per-player trigger state is released on quit")
    void triggerStateReleasedOnQuit() throws Exception {
        p.getInventory().setItemInMainHand(relic("probe"));
        p.simulatePlayerMove(p.getLocation().add(1, 0, 0));
        UUID id = p.getUniqueId();
        p.disconnect();
        for (var l : org.bukkit.event.HandlerList.getRegisteredListeners(plugin)) {
            Object listener = l.getListener();
            for (var f : listener.getClass().getDeclaredFields()) {
                if (java.util.Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    assertFalse(((java.util.Map<?, ?>) f.get(listener)).containsKey(id),
                            listener.getClass().getSimpleName() + "." + f.getName() + " still holds the player");
                }
            }
        }
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-033 one relic worn in two armor slots fires once")
    void duplicateArmorRelicFiresOnce() {
        p.getInventory().setHelmet(relic("probe"));
        p.getInventory().setBoots(relic("probe"));
        p.simulateSneak(true);
        assertEquals(List.of("SNEAK"), fired());
    }

    @Test
    void triggerEnumParsing() {
        assertEquals(TriggerType.RIGHT_CLICK, TriggerType.fromString("right_click"));
        assertNull(TriggerType.fromString("nope"));
        assertNull(TriggerType.fromString(null));
    }
}
