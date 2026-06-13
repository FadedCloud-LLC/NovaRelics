package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class BroadcastAbility extends Ability {

    public BroadcastAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String message = config.getString("message", "")
                .replace("{player}", player.getName())
                .replace("{relic}", context.getRelic().getId());

        Component component = MiniMsg.parse(message);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(component);
        }
        return true;
    }

    @Override
    public String getName() { return "Broadcast"; }

    @Override
    public String getDescription() { return "Broadcasts a MiniMessage-formatted message to all online players."; }
}
