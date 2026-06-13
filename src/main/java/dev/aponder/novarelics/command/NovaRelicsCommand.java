package dev.aponder.novarelics.command;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.sub.*;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class NovaRelicsCommand implements CommandExecutor, TabCompleter {

    private final NovaRelics plugin;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public NovaRelicsCommand(NovaRelics plugin) {
        this.plugin = plugin;
        register(new HelpSubCommand(plugin, this));
        register(new ReloadSubCommand(plugin));
        register(new GiveSubCommand(plugin));
        register(new TakeSubCommand(plugin));
        register(new EditorSubCommand(plugin));
        register(new CreateSubCommand(plugin));
        register(new DeleteSubCommand(plugin));
        register(new ListSubCommand(plugin));
        register(new InfoSubCommand(plugin));
        register(new ImportSubCommand(plugin));
        register(new ExportSubCommand(plugin));
    }

    private void register(SubCommand cmd) {
        subCommands.put(cmd.getName().toLowerCase(), cmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length == 0) {
            subCommands.get("help").execute(sender, args);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-command")));
            return true;
        }

        if (!sub.getPermission().isEmpty() && !sender.hasPermission(sub.getPermission())) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        sub.execute(sender, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.entrySet().stream()
                    .filter(e -> sender.hasPermission(e.getValue().getPermission())
                            || e.getValue().getPermission().isEmpty())
                    .map(Map.Entry::getKey)
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return sub.tabComplete(sender, subArgs);
        }
        return Collections.emptyList();
    }

    public Map<String, SubCommand> getSubCommands() { return subCommands; }
}
