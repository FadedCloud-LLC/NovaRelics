package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAbility extends Ability {

    public PotionEffectAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String effectName = config.getString("effect", "SPEED");
        int duration = config.getInt("duration", 200);
        int amplifier = config.getInt("amplifier", 0);
        boolean ambient = config.getBoolean("ambient", true);
        boolean particles = config.getBoolean("particles", true);

        PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
        if (type == null) {
            plugin.getLogger().warning("Unknown potion effect: " + effectName);
            return false;
        }

        player.addPotionEffect(new PotionEffect(type, duration, amplifier, ambient, particles));
        return true;
    }

    @Override
    public String getName() { return "Potion Effect"; }

    @Override
    public String getDescription() { return "Applies a potion effect to the player."; }
}
