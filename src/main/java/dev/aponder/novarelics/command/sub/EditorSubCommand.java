package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class EditorSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public EditorSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "editor"; }
    @Override public String getPermission() { return "novarelics.editor"; }
    @Override public String getUsage() { return "/nr editor [relicId]"; }
    @Override public String getDescription() { return "Opens the relic editor GUI."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("player-only")));
            return true;
        }
        if (args.length > 0) {
            plugin.getGuiManager().openRelicEditor(player, args[0]);
        } else {
            plugin.getGuiManager().openEditor(player);
        }
        MiniMsg.send(player, MiniMsg.parse(
                plugin.getConfigManager().getMessage("editor-open")));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getRelicManager().getRelicIds().stream()
                    .filter(id -> id.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
