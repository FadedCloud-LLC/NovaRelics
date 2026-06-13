package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CreateSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public CreateSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "create"; }
    @Override public String getPermission() { return "novarelics.create"; }
    @Override public String getUsage() { return "/nr create <id>"; }
    @Override public String getDescription() { return "Creates a new blank relic."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }

        String id = args[0].toLowerCase().replace(" ", "_");
        if (plugin.getRelicManager().getRelic(id) != null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    "<red>A relic with id <yellow>" + id + "</yellow> already exists."));
            return true;
        }

        RelicDefinition def = new RelicDefinition(id);
        def.setDisplayName("<white>" + id);
        plugin.getRelicManager().register(def);
        plugin.getRelicManager().save(def);

        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("editor-created", "<id>", id)));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
