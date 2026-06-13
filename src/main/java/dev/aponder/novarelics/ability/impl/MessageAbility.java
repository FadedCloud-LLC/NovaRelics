package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;

public class MessageAbility extends Ability {

    public MessageAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        String message = config.getString("message", "");
        if (message.isEmpty()) return false;
        String resolved = message
                .replace("%player%", context.getPlayer().getName())
                .replace("%relic%", context.getRelic().getDisplayName());
        MiniMsg.send(context.getPlayer(), MiniMsg.parse(resolved));
        return true;
    }

    @Override
    public String getName() { return "Message"; }

    @Override
    public String getDescription() { return "Sends a MiniMessage-formatted chat message to the player."; }
}
