package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public InfoSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "info"; }
    @Override public String getPermission() { return ""; }
    @Override public String getUsage() { return "/nr info <relicId>"; }
    @Override public String getDescription() { return "Shows detailed info about a relic."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }

        RelicDefinition relic = plugin.getRelicManager().getRelic(args[0]);
        if (relic == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-relic", "<relic>", args[0])));
            return true;
        }

        String triggers = relic.getTriggers().keySet().stream()
                .map(TriggerType::name)
                .collect(Collectors.joining(", "));

        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("relic-info-header",
                        "<id>", relic.getId())));
        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("relic-info-name",
                        "<name>", relic.getDisplayName())));
        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("relic-info-rarity",
                        "<rarity>", relic.getRarity())));
        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("relic-info-trigger",
                        "<trigger>", triggers.isEmpty() ? "None" : triggers)));
        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("relic-info-cooldown",
                        "<cooldown>", String.valueOf(relic.getCooldownSeconds()))));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getRelicManager().getRelicIds().stream()
                    .filter(id -> id.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
