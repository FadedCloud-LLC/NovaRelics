package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicBuilder;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TakeSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public TakeSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "take"; }
    @Override public String getPermission() { return "novarelics.take"; }
    @Override public String getUsage() { return "/nr take <player> <relic>"; }
    @Override public String getDescription() { return "Removes a relic from a player's inventory."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-player", "<player>", args[0])));
            return true;
        }

        RelicDefinition relic = plugin.getRelicManager().getRelic(args[1]);
        if (relic == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-relic", "<relic>", args[1])));
            return true;
        }

        boolean removed = false;
        for (int i = 0; i < target.getInventory().getSize(); i++) {
            ItemStack item = target.getInventory().getItem(i);
            if (item == null) continue;
            String id = RelicBuilder.getRelicId(item);
            if (relic.getId().equals(id)) {
                target.getInventory().setItem(i, null);
                removed = true;
                break;
            }
        }

        if (removed) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("take-success",
                            "<amount>", "1",
                            "<relic>", relic.getDisplayName(),
                            "<player>", target.getName())));
        } else {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("take-not-found",
                            "<player>", target.getName(),
                            "<relic>", relic.getId())));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return plugin.getRelicManager().getRelicIds().stream()
                    .filter(id -> id.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
