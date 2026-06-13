package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExportSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public ExportSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "export"; }
    @Override public String getPermission() { return "novarelics.export"; }
    @Override public String getUsage() { return "/nr export <relicId>"; }
    @Override public String getDescription() { return "Exports a relic to a shareable YAML file."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }

        String id = args[0].toLowerCase();
        RelicDefinition relic = plugin.getRelicManager().getRelic(id);
        if (relic == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-relic", "<relic>", id)));
            return true;
        }

        String fileName = id + ".yml";
        File exportFile = new File(plugin.getConfigManager().getRelicsFolder(), fileName);
        boolean saved = plugin.getRelicManager().getLoader().save(relic, exportFile);

        if (saved) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("export-success",
                            "<id>", id, "<file>", fileName)));
        } else {
            MiniMsg.send(sender, MiniMsg.parse("<red>Export failed. Check console for details."));
        }
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
