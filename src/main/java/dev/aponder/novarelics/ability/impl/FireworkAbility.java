package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.ColorUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkAbility extends Ability {

    public FireworkAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();

        String colorStr = config.getString("color", "#FF0000");
        String fadeStr = config.getString("fade-color", "#FFFFFF");
        int power = Math.max(0, Math.min(config.getInt("power", 1), 3));
        boolean flicker = config.getBoolean("flicker", true);
        boolean trail = config.getBoolean("trail", false);

        FireworkEffect.Type type;
        try {
            type = FireworkEffect.Type.valueOf(config.getString("effect", "BALL_LARGE").toUpperCase());
        } catch (IllegalArgumentException e) {
            type = FireworkEffect.Type.BALL_LARGE;
        }

        FireworkEffect effect = FireworkEffect.builder()
                .withColor(ColorUtil.toBukkitColor(colorStr))
                .withFade(ColorUtil.toBukkitColor(fadeStr))
                .with(type)
                .flicker(flicker)
                .trail(trail)
                .build();

        Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(power);
        fw.setFireworkMeta(meta);
        return true;
    }

    @Override
    public String getName() { return "Firework"; }

    @Override
    public String getDescription() { return "Launches a configurable firework at the player's location."; }
}
