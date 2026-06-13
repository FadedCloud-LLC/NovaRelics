package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.NovaRelicsCommand;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class HelpSubCommand implements SubCommand {

    private final NovaRelics plugin;
    private final NovaRelicsCommand parent;

    public HelpSubCommand(NovaRelics plugin, NovaRelicsCommand parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getPermission() { return ""; }

    @Override
    public String getUsage() { return "/nr help"; }

    @Override
    public String getDescription() { return "Shows this help menu."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        MiniMsg.send(sender, MiniMsg.parse(
                "<newline><gradient:#AA55FF:#55FFFF><bold>NovaRelics</bold></gradient> <gray>— Commands"));
        parent.getSubCommands().values().forEach(cmd -> {
            if (cmd.getPermission().isEmpty() || sender.hasPermission(cmd.getPermission())) {
                MiniMsg.send(sender, MiniMsg.parse(
                        "  <dark_gray>• <yellow>" + cmd.getUsage()
                                + " <gray>— " + cmd.getDescription()));
            }
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
