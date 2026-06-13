package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class CommandAbility extends Ability {

    public CommandAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        Object rawCmds = config.getParams().get("commands");
        List<String> commands;

        if (rawCmds instanceof List) {
            commands = (List<String>) rawCmds;
        } else if (rawCmds instanceof String) {
            commands = List.of((String) rawCmds);
        } else {
            return false;
        }

        String executor = config.getString("executor", "CONSOLE").toUpperCase();
        int delayTicks = config.getInt("delay", 0);

        Runnable run = () -> {
            for (String cmd : commands) {
                String resolved = cmd
                        .replace("%player%", player.getName())
                        .replace("%relic%", context.getRelic().getId())
                        .replace("%world%", player.getWorld().getName());

                if ("PLAYER".equals(executor)) {
                    player.performCommand(resolved.startsWith("/")
                            ? resolved.substring(1) : resolved);
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved.startsWith("/")
                            ? resolved.substring(1) : resolved);
                }
            }
        };

        if (delayTicks > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, run, delayTicks);
        } else {
            run.run();
        }
        return true;
    }

    @Override
    public String getName() { return "Command"; }

    @Override
    public String getDescription() { return "Executes one or more commands as console or player."; }
}
