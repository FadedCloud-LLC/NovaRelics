package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public ListSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "list"; }
    @Override public String getPermission() { return ""; }
    @Override public String getUsage() { return "/nr list"; }
    @Override public String getDescription() { return "Lists all loaded relics."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Collection<RelicDefinition> relics = plugin.getRelicManager().getAllRelics();
        if (relics.isEmpty()) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("relic-list-empty")));
            return true;
        }

        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("relic-list-header",
                        "<count>", String.valueOf(relics.size()))));
        for (RelicDefinition relic : relics) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("relic-list-entry",
                            "<id>", relic.getId(),
                            "<name>", relic.getDisplayName())));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
