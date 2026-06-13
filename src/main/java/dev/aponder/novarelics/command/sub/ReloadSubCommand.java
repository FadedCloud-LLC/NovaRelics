package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ReloadSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public ReloadSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "reload"; }
    @Override public String getPermission() { return "novarelics.reload"; }
    @Override public String getUsage() { return "/nr reload"; }
    @Override public String getDescription() { return "Reloads all configs and relics."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.reload();
        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("reload-success")));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
