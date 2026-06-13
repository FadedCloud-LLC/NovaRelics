package dev.aponder.novarelics.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumSet;
import java.util.Set;

public final class ItemUtil {

    private static final Set<Material> SMELT_MAP_KEYS = EnumSet.noneOf(Material.class);

    private ItemUtil() {}

    public static boolean isDamageable(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta instanceof Damageable;
    }

    public static boolean isFullyRepaired(ItemStack item) {
        if (!isDamageable(item)) return false;
        Damageable d = (Damageable) item.getItemMeta();
        return d != null && d.getDamage() == 0;
    }

    public static ItemStack fullyRepair(ItemStack item) {
        if (!isDamageable(item)) return item;
        ItemStack copy = item.clone();
        Damageable d = (Damageable) copy.getItemMeta();
        if (d != null) {
            d.setDamage(0);
            copy.setItemMeta(d);
        }
        return copy;
    }

    public static ItemStack repairByPercent(ItemStack item, double percent) {
        if (!isDamageable(item)) return item;
        ItemStack copy = item.clone();
        Damageable d = (Damageable) copy.getItemMeta();
        if (d == null) return copy;
        int maxDurability = item.getType().getMaxDurability();
        int repairAmount = (int) (maxDurability * (percent / 100.0));
        int newDamage = Math.max(0, d.getDamage() - repairAmount);
        d.setDamage(newDamage);
        copy.setItemMeta(d);
        return copy;
    }

    public static Material getSmeltResult(Material raw) {
        return switch (raw) {
            case RAW_IRON -> Material.IRON_INGOT;
            case RAW_GOLD -> Material.GOLD_INGOT;
            case RAW_COPPER -> Material.COPPER_INGOT;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case SAND -> Material.GLASS;
            case COBBLESTONE -> Material.STONE;
            case CLAY_BALL -> Material.BRICK;
            case CLAY -> Material.TERRACOTTA;
            case NETHERRACK -> Material.NETHER_BRICK;
            case WET_SPONGE -> Material.SPONGE;
            case KELP -> Material.DRIED_KELP;
            case CACTUS -> Material.GREEN_DYE;
            case BEEF -> Material.COOKED_BEEF;
            case CHICKEN -> Material.COOKED_CHICKEN;
            case COD -> Material.COOKED_COD;
            case SALMON -> Material.COOKED_SALMON;
            case MUTTON -> Material.COOKED_MUTTON;
            case PORKCHOP -> Material.COOKED_PORKCHOP;
            case RABBIT -> Material.COOKED_RABBIT;
            default -> null;
        };
    }

    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().isAir();
    }

    public static ItemStack ensureAmount(ItemStack item, int amount) {
        ItemStack copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }
}
