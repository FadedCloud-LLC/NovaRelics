package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class CureAbility extends Ability {

    private static final Set<PotionEffectType> NEGATIVE = buildNegativeSet();

    @SuppressWarnings("deprecation")
    private static Set<PotionEffectType> buildNegativeSet() {
        java.util.HashSet<PotionEffectType> set = new java.util.HashSet<>();
        set.add(PotionEffectType.BLINDNESS);
        set.add(PotionEffectType.CONFUSION);    // NAUSEA in 1.20+
        set.add(PotionEffectType.HARM);         // INSTANT_DAMAGE in 1.20+
        set.add(PotionEffectType.HUNGER);
        set.add(PotionEffectType.LEVITATION);
        set.add(PotionEffectType.SLOW_DIGGING); // MINING_FATIGUE in 1.20+
        set.add(PotionEffectType.POISON);
        set.add(PotionEffectType.SLOW);         // SLOWNESS in 1.20+
        set.add(PotionEffectType.UNLUCK);
        set.add(PotionEffectType.WEAKNESS);
        set.add(PotionEffectType.WITHER);
        PotionEffectType darkness = PotionEffectType.getByName("DARKNESS");
        if (darkness != null) set.add(darkness);
        return java.util.Collections.unmodifiableSet(set);
    }

    public CureAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        boolean negativeOnly = config.getBoolean("negative-only", true);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (!negativeOnly || NEGATIVE.contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
        return true;
    }

    @Override
    public String getName() { return "Cure"; }

    @Override
    public String getDescription() { return "Removes negative (or all) potion effects from the player."; }
}
