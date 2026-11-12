package dev.aponder.novarelics.abilities;

import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Abilities exercised directly through the registry (the dispatch path is covered by TriggerTest). */
class AbilityTest extends PluginTestBase {

    private PlayerMock p;
    private RelicDefinition relicDef;

    @BeforeEach
    void setUp() {
        p = player();
        p.setGameMode(GameMode.SURVIVAL);
        relicDef = new RelicDefinition("unit");
        relicDef.setDisplayName("Unit Relic");
    }

    private boolean run(String type, Event event, Object... kv) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) params.put((String) kv[i], kv[i + 1]);
        AbilityContext ctx = new AbilityContext(p, relicDef, p.getInventory().getItemInMainHand(),
                TriggerType.RIGHT_CLICK, event);
        return plugin.getAbilityRegistry().fire(new AbilityConfig(type, params), ctx);
    }

    private double maxHealth() {
        return p.getAttribute(Attribute.MAX_HEALTH).getValue();
    }

    @Test
    void registryKnowsAllAbilitiesAndRejectsUnknown() {
        assertEquals(50, plugin.getAbilityRegistry().getTypes().size());
        for (Ability a : plugin.getAbilityRegistry().getAll().values()) {
            assertFalse(a.getName().isBlank());
            assertFalse(a.getDescription().isBlank());
        }
        assertFalse(run("NOT_AN_ABILITY", null));
        assertTrue(plugin.getAbilityRegistry().has("heal"), "lookup is case-insensitive");
    }

    @Test
    void healFlatPercentAndCapped() {
        p.setHealth(10);
        assertTrue(run("HEAL", null, "amount", 4));
        assertEquals(14, p.getHealth(), 1e-9);
        p.setHealth(10);
        run("HEAL", null, "amount", 50, "percent", true);
        assertEquals(Math.min(maxHealth(), 10 + maxHealth() / 2), p.getHealth(), 1e-9);
        run("HEAL", null, "amount", 1000);
        assertEquals(maxHealth(), p.getHealth(), 1e-9);
    }

    @Test
    void feedRestoresHunger() {
        p.setFoodLevel(2);
        p.setSaturation(0);
        assertTrue(run("FEED", null, "hunger", 6, "saturation", 3));
        assertEquals(8, p.getFoodLevel());
        assertEquals(3f, p.getSaturation(), 1e-6);
        run("FEED", null, "hunger", 100);
        assertEquals(20, p.getFoodLevel());
    }

    @Test
    void potionEffectAppliesAndRejectsUnknown() {
        assertTrue(run("POTION_EFFECT", null, "effect", "SPEED", "duration", 100, "amplifier", 1));
        PotionEffect e = p.getPotionEffect(PotionEffectType.SPEED);
        assertNotNull(e);
        assertEquals(1, e.getAmplifier());
        assertFalse(run("POTION_EFFECT", null, "effect", "NOT_AN_EFFECT"));
    }

    @Test
    void cureRemovesOnlyNegativeEffectsByDefault() {
        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
        assertTrue(run("CURE", null));
        assertNull(p.getPotionEffect(PotionEffectType.POISON));
        assertNotNull(p.getPotionEffect(PotionEffectType.SPEED));
        run("CURE", null, "negative-only", false);
        assertTrue(p.getActivePotionEffects().isEmpty());
    }

    @Test
    void giveItemAndXp() {
        assertTrue(run("GIVE_ITEM", null, "material", "DIAMOND", "amount", 3));
        assertTrue(p.getInventory().contains(Material.DIAMOND, 3));
        assertFalse(run("GIVE_ITEM", null, "material", "NOT_A_MATERIAL"));
        int before = p.getLevel();
        run("GIVE_XP", null, "amount", 5, "levels", true);
        assertEquals(before + 5, p.getLevel());
    }

    @Test
    void messageAndBroadcast() {
        PlayerMock other = player();
        messages(p);
        assertTrue(run("MESSAGE", null, "message", "<green>hello %player%"));
        assertTrue(messages(p).contains("hello " + p.getName()));
        assertFalse(run("MESSAGE", null), "empty message is a no-op");
        run("BROADCAST", null, "message", "boom {player}");
        assertTrue(messages(other).contains("boom " + p.getName()));
    }

    @Test
    void launchAndChargeSetVelocityAndResetFall() {
        p.setFallDistance(10);
        assertTrue(run("LAUNCH", null, "vertical", 1.5));
        assertEquals(1.5, p.getVelocity().getY(), 1e-9);
        assertEquals(0f, p.getFallDistance());
        assertTrue(run("CHARGE", null, "power", 2.0, "vertical", 0.3));
        assertEquals(0.3, p.getVelocity().getY(), 1e-9);
    }

    @Test
    void teleportToCoordinates() {
        assertTrue(run("TELEPORT", null, "mode", "COORDINATES", "x", 100, "y", 70, "z", -50));
        Location l = p.getLocation();
        assertEquals(100, l.getX(), 1e-9);
        assertEquals(70, l.getY(), 1e-9);
        assertEquals(-50, l.getZ(), 1e-9);
    }

    @Test
    void invincibilityAndFireNearby() {
        assertTrue(run("INVINCIBILITY", null, "duration", 40));
        assertEquals(40, p.getNoDamageTicks());
        LivingEntity z = (LivingEntity) world.spawnEntity(p.getLocation().add(1, 0, 0), EntityType.ZOMBIE);
        assertTrue(run("SET_ON_FIRE", null, "radius", 3, "duration", 2));
        assertEquals(40, z.getFireTicks());
        assertTrue(p.getFireTicks() <= 0, "self excluded by default");
    }

    @Test
    void consumeRelicRemovesFromInventory() {
        installRelic("eat_me", dev.aponder.novarelics.support.Relics.probe("eat_me", 0, "RIGHT_CLICK"));
        relicDef = plugin.getRelicManager().getRelic("eat_me");
        ItemStack stack = relic("eat_me");
        stack.setAmount(3);
        p.getInventory().addItem(stack);
        assertTrue(run("CONSUME_RELIC", null, "amount", 2));
        assertEquals(1, count(p.getInventory(), "eat_me"));
    }

    @Test
    void eventSpecificAbilitiesRefuseOtherEvents() {
        assertFalse(run("DOUBLE_DROPS", null));
        assertFalse(run("AUTO_SMELT", null));
        assertFalse(run("VEIN_MINE", null));
        assertFalse(run("TREE_FELLER", null));
        assertFalse(run("BOOST_DAMAGE", null));
        assertFalse(run("REDUCE_DAMAGE", null));
        assertFalse(run("PREVENT_DEATH", null));
        assertFalse(run("KEEP_ITEM", null));
        assertFalse(run("AUTO_REPLANT", null));
    }

    @Test
    void reduceDamageAppliesMultiplierAndFlatNeverNegative() {
        EntityDamageEvent e = new EntityDamageEvent(p, EntityDamageEvent.DamageCause.FALL,
                org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.FALL).build(), 10);
        assertTrue(run("REDUCE_DAMAGE", e, "multiplier", 0.5, "amount", 1));
        assertEquals(4, e.getDamage(), 1e-9);
        run("REDUCE_DAMAGE", e, "multiplier", 0.1, "amount", 100);
        assertEquals(0, e.getDamage(), 1e-9);
    }

    @Test
    void exceptionsInsideAbilitiesAreContained() {
        // SOUND with an unknown key falls back to the String overload; must never throw out of fire()
        assertDoesNotThrow(() -> run("SOUND", null, "sound", "custom:not_real"));
        assertDoesNotThrow(() -> run("PARTICLE", null, "particle", "DUST"));
    }

    // ---------------------------------------------------------------- regressions

    @Test
    @Tag("regression")
    @DisplayName("NR-020 FLIGHT restores the player's previous flight permission")
    void flightRestoresPreviousState() {
        p.setAllowFlight(true); // e.g. granted by /fly
        assertTrue(run("FLIGHT", null, "duration", 1));
        server.getScheduler().performTicks(25);
        assertTrue(p.getAllowFlight(), "temporary flight must not revoke flight the player already had");
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-020 disabling the plugin reverts temporary effects (flight)")
    void disableRevertsTemporaryFlight() {
        assertTrue(run("FLIGHT", null, "duration", 60));
        server.getPluginManager().disablePlugin(plugin);
        assertFalse(p.getAllowFlight(), "survival player kept permanent flight after plugin disable");
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-029 knockback-style abilities handle an entity at the exact same position")
    void knockbackZeroVector() {
        LivingEntity z = (LivingEntity) world.spawnEntity(p.getLocation(), EntityType.ZOMBIE);
        assertTrue(run("KNOCKBACK", null, "radius", 2));
        assertTrue(Double.isFinite(z.getVelocity().getX()), "velocity must be finite");
        assertTrue(run("PULL", null, "radius", 2));
        assertTrue(run("SHOCKWAVE", null, "radius", 2, "damage", 0));
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-031 CONSUME_RELIC reports failure when nothing was consumed")
    void consumeRelicFailsWhenMissing() {
        assertFalse(run("CONSUME_RELIC", null, "amount", 1));
    }
}
