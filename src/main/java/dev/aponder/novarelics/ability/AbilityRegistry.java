package dev.aponder.novarelics.ability;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.impl.*;

import java.util.*;

public class AbilityRegistry {

    private final NovaRelics plugin;
    private final Map<String, Ability> registry = new LinkedHashMap<>();

    public AbilityRegistry(NovaRelics plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    private void registerDefaults() {
        register("FULL_REPAIR",          new FullRepairAbility(plugin));
        register("REPAIR_PERCENT",       new RepairPercentAbility(plugin));
        register("HEAL",                 new HealAbility(plugin));
        register("FEED",                 new FeedAbility(plugin));
        register("TELEPORT",             new TeleportAbility(plugin));
        register("COMMAND",              new CommandAbility(plugin));
        register("GIVE_MONEY",           new GiveMoneyAbility(plugin));
        register("TAKE_MONEY",           new TakeMoneyAbility(plugin));
        register("GIVE_XP",              new GiveXpAbility(plugin));
        register("DOUBLE_DROPS",         new DoubleDropsAbility(plugin));
        register("AUTO_SMELT",           new AutoSmeltAbility(plugin));
        register("VEIN_MINE",            new VeinMineAbility(plugin));
        register("TREE_FELLER",          new TreeFellerAbility(plugin));
        register("LIGHTNING",            new LightningAbility(plugin));
        register("FIREBALL",             new FireballAbility(plugin));
        register("POTION_EFFECT",        new PotionEffectAbility(plugin));
        register("SPAWN_ENTITY",         new SpawnEntityAbility(plugin));
        register("PARTICLE",             new ParticleAbility(plugin));
        register("SOUND",                new SoundAbility(plugin));
        register("MESSAGE",              new MessageAbility(plugin));
        register("TITLE",                new TitleAbility(plugin));
        register("ACTIONBAR",            new ActionBarAbility(plugin));
        register("BOOST_DAMAGE",         new BoostDamageAbility(plugin));
        register("REDUCE_DAMAGE",        new ReduceDamageAbility(plugin));
        register("GAIN_HEALTH_ON_KILL",  new GainHealthOnKillAbility(plugin));
        register("PREVENT_DEATH",        new PreventDeathAbility(plugin));
        register("KEEP_ITEM",            new KeepItemAbility(plugin));
        register("COOLDOWN_REDUCTION",   new CooldownReductionAbility(plugin));
        register("AUTO_REPLANT",         new AutoReplantAbility(plugin));
        register("CUSTOM_RECIPE_UNLOCK", new CustomRecipeUnlockAbility(plugin));
        register("EXPLOSION",            new ExplosionAbility(plugin));
        register("FLIGHT",               new FlightAbility(plugin));
        register("SET_ON_FIRE",          new SetOnFireAbility(plugin));
        register("KNOCKBACK",            new KnockbackAbility(plugin));
        register("BROADCAST",            new BroadcastAbility(plugin));
        register("GIVE_ITEM",            new GiveItemAbility(plugin));
        register("LOOK_TELEPORT",        new LookTeleportAbility(plugin));
        register("INVINCIBILITY",        new InvincibilityAbility(plugin));
        register("FIREWORK",             new FireworkAbility(plugin));
        register("STEAL_HEALTH",         new StealHealthAbility(plugin));
        register("PULL",                 new PullAbility(plugin));
        register("LAUNCH",               new LaunchAbility(plugin));
        register("CURE",                 new CureAbility(plugin));
        register("SHOCKWAVE",            new ShockwaveAbility(plugin));
        register("GLOW",                 new GlowAbility(plugin));
        register("CONSUME_RELIC",        new ConsumeRelicAbility(plugin));
        register("PHANTOM_STRIKE",       new PhantomStrikeAbility(plugin));
        register("CHARGE",               new ChargeAbility(plugin));
        register("WEB_TRAP",             new WebTrapAbility(plugin));
        register("NEARBY_HEAL",          new NearbyHealAbility(plugin));
    }

    public void register(String type, Ability ability) {
        registry.put(type.toUpperCase(), ability);
    }

    public Ability get(String type) {
        return type == null ? null : registry.get(type.toUpperCase());
    }

    public boolean has(String type) {
        return type != null && registry.containsKey(type.toUpperCase());
    }

    public Map<String, Ability> getAll() {
        return Collections.unmodifiableMap(registry);
    }

    public Set<String> getTypes() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * Fire an ability from a config entry against the provided context.
     * Handles cooldowns and cancellation.
     *
     * @return true if execution succeeded
     */
    public boolean fire(AbilityConfig config, AbilityContext context) {
        Ability ability = get(config.getType());
        if (ability == null) {
            plugin.getLogger().warning("Unknown ability type: " + config.getType());
            return false;
        }
        try {
            return ability.execute(config, context);
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing ability " + config.getType()
                    + " for relic " + context.getRelic().getId() + ": " + e.getMessage());
            if (plugin.getConfigManager().isDebug()) e.printStackTrace();
            return false;
        }
    }

    public boolean firePrepare(AbilityConfig config, AbilityContext context) {
        Ability ability = get(config.getType());
        if (ability == null) return false;
        try {
            return ability.prepare(config, context);
        } catch (Exception e) {
            plugin.getLogger().warning("Error in prepare phase of " + config.getType() + ": " + e.getMessage());
            return false;
        }
    }
}
