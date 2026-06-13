package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ImportSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public ImportSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "import"; }
    @Override public String getPermission() { return "novarelics.import"; }
    @Override public String getUsage() { return "/nr import <filename>"; }
    @Override public String getDescription() { return "Imports a relic YAML file."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }

        String fileName = args[0].endsWith(".yml") ? args[0] : args[0] + ".yml";
        File file = new File(plugin.getConfigManager().getRelicsFolder(), fileName);

        if (!file.exists()) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("import-failed",
                            "<error>", "File not found: " + fileName)));
            return true;
        }

        try {
            RelicDefinition def = plugin.getRelicManager().getLoader().load(file);
            if (def == null) throw new IllegalArgumentException("Failed to parse relic file.");
            plugin.getRelicManager().register(def);

            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("import-success",
                            "<id>", def.getId(),
                            "<file>", fileName)));
        } catch (Exception e) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("import-failed",
                            "<error>", e.getMessage())));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            File[] files = plugin.getConfigManager().getRelicsFolder()
                    .listFiles((d, n) -> n.endsWith(".yml"));
            if (files == null) return Collections.emptyList();
            List<String> names = new java.util.ArrayList<>();
            for (File f : files) names.add(f.getName().replace(".yml", ""));
            return names.stream()
                    .filter(n -> n.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
