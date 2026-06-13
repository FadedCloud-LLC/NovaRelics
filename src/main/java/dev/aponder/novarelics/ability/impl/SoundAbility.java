package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundAbility extends Ability {

    public SoundAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String soundName = config.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) config.getDouble("volume", 1.0);
        float pitch = (float) config.getDouble("pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Custom sound key (Resource Pack)
            player.playSound(player.getLocation(), soundName, volume, pitch);
        }
        return true;
    }

    @Override
    public String getName() { return "Sound"; }

    @Override
    public String getDescription() { return "Plays a sound at the player's location."; }
}
