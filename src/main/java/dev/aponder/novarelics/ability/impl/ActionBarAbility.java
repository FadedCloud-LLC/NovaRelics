package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;

public class ActionBarAbility extends Ability {

    public ActionBarAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        String message = config.getString("message", "");
        if (message.isEmpty()) return false;
        String resolved = message
                .replace("%player%", context.getPlayer().getName())
                .replace("%relic%", context.getRelic().getDisplayName());
        context.getPlayer().sendActionBar(MiniMsg.toLegacy(resolved));
        return true;
    }

    @Override
    public String getName() { return "Action Bar"; }

    @Override
    public String getDescription() { return "Shows a message in the player's action bar."; }
}
