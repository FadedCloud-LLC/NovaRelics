package dev.aponder.novarelics.util;

import net.kyori.adventure.text.format.TextColor;

import java.awt.Color;

public final class ColorUtil {

    private ColorUtil() {}

    public static TextColor fromHex(String hex) {
        if (hex == null || hex.isEmpty()) return TextColor.color(0xFFFFFF);
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int rgb = Integer.parseInt(clean, 16);
            return TextColor.color(rgb);
        } catch (NumberFormatException e) {
            return TextColor.color(0xFFFFFF);
        }
    }

    public static TextColor interpolate(TextColor from, TextColor to, float t) {
        int r = lerp(from.red(), to.red(), t);
        int g = lerp(from.green(), to.green(), t);
        int b = lerp(from.blue(), to.blue(), t);
        return TextColor.color(r, g, b);
    }

    private static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }

    public static org.bukkit.Color toBukkitColor(String hex) {
        if (hex == null || hex.isEmpty()) return org.bukkit.Color.WHITE;
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int rgb = Integer.parseInt(clean, 16);
            return org.bukkit.Color.fromRGB(rgb);
        } catch (NumberFormatException e) {
            return org.bukkit.Color.WHITE;
        }
    }

    public static String toHex(org.bukkit.Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static boolean isValidHex(String hex) {
        if (hex == null) return false;
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        return clean.matches("[0-9A-Fa-f]{6}");
    }
}
