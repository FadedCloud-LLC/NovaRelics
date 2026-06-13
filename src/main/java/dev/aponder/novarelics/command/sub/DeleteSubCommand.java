package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public DeleteSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "delete"; }
    @Override public String getPermission() { return "novarelics.delete"; }
    @Override public String getUsage() { return "/nr delete <relicId>"; }
    @Override public String getDescription() { return "Permanently deletes a relic."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }
        String id = args[0].toLowerCase();
        if (plugin.getRelicManager().getRelic(id) == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-relic", "<relic>", id)));
            return true;
        }
        plugin.getRelicManager().delete(id);
        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("editor-deleted", "<id>", id)));
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
