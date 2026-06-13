package dev.aponder.novarelics.gui;

import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class GuiUtils {

    private GuiUtils() {}

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(MiniMsg.toLegacy(name));
        if (lore.length > 0) {
            meta.setLore(Arrays.stream(lore)
                    .map(MiniMsg::toLegacy)
                    .collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack placeholder(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack gray() {
        return placeholder(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static ItemStack black() {
        return placeholder(Material.BLACK_STAINED_GLASS_PANE);
    }

    public static int rows(int itemCount) {
        return Math.min(6, Math.max(1, (int) Math.ceil(itemCount / 9.0)));
    }
}
