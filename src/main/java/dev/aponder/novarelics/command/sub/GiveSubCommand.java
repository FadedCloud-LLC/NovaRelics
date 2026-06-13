package dev.aponder.novarelics.command.sub;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.command.SubCommand;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveSubCommand implements SubCommand {

    private final NovaRelics plugin;

    public GiveSubCommand(NovaRelics plugin) { this.plugin = plugin; }

    @Override public String getName() { return "give"; }
    @Override public String getPermission() { return "novarelics.give"; }
    @Override public String getUsage() { return "/nr give <player> <relic> [amount]"; }
    @Override public String getDescription() { return "Gives a relic to a player."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MiniMsg.send(sender, MiniMsg.parse("<red>Usage: " + getUsage()));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-player",
                            "<player>", args[0])));
            return true;
        }

        RelicDefinition relic = plugin.getRelicManager().getRelic(args[1]);
        if (relic == null) {
            MiniMsg.send(sender, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-relic",
                            "<relic>", args[1])));
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try { amount = Math.max(1, Integer.parseInt(args[2])); }
            catch (NumberFormatException e) {
                MiniMsg.send(sender, MiniMsg.parse(
                        plugin.getConfigManager().getMessage("invalid-amount")));
                return true;
            }
        }

        ItemStack item = plugin.getRelicManager().buildItem(relic);
        item.setAmount(amount);
        target.getInventory().addItem(item);

        // Mark as discovered
        plugin.getStorageManager().getPlayer(target.getUniqueId()).addDiscovered(relic.getId());

        MiniMsg.send(sender, MiniMsg.parse(
                plugin.getConfigManager().getMessage("give-success",
                        "<amount>", String.valueOf(amount),
                        "<relic>", relic.getDisplayName(),
                        "<player>", target.getName())));
        MiniMsg.send(target, MiniMsg.parse(
                plugin.getConfigManager().getMessage("give-received",
                        "<amount>", String.valueOf(amount),
                        "<relic>", relic.getDisplayName())));
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
