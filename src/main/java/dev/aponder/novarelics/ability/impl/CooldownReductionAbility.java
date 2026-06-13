package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.relic.RelicDefinition;
import org.bukkit.entity.Player;

public class CooldownReductionAbility extends Ability {

    public CooldownReductionAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String targetRelicId = config.getString("target-relic", "");
        double reductionPercent = config.getDouble("reduction", 50.0);

        if (targetRelicId.isEmpty()) return false;

        RelicDefinition target = plugin.getRelicManager().getRelic(targetRelicId);
        if (target == null) return false;

        long remaining = plugin.getCooldownManager()
                .getRemainingSeconds(player, target) * 1000L;
        if (remaining <= 0) return false;

        long reduced = (long) (remaining * (1.0 - reductionPercent / 100.0));
        long newExpiry = System.currentTimeMillis() + reduced;
        plugin.getStorageManager().setCooldown(player.getUniqueId(),
                "relic:" + targetRelicId, reduced);
        return true;
    }

    @Override
    public String getName() { return "Cooldown Reduction"; }

    @Override
    public String getDescription() { return "Reduces the cooldown of another relic by a percentage."; }
}
