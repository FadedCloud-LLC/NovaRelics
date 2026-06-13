package dev.aponder.novarelics.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MiniMsg {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMP =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION =
            LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    private MiniMsg() {}

    public static Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        text = HEX_PATTERN.matcher(text).replaceAll("<#$1>");
        if (text.contains("&")) {
            text = LEGACY_AMP.serialize(LEGACY_AMP.deserialize(text));
        }
        return MM.deserialize(text);
    }

    public static List<Component> parseList(List<String> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(MiniMsg::parse).collect(Collectors.toList());
    }

    public static String serialize(Component component) {
        return MM.serialize(component);
    }

    public static String strip(String text) {
        if (text == null) return "";
        return MM.stripTags(text);
    }

    public static String toLegacy(String text) {
        return LEGACY_SECTION.serialize(parse(text));
    }

    public static Component replace(Component component, String key, String value) {
        return parse(serialize(component).replace(key, value));
    }

    public static String replace(String template, String... pairs) {
        if (template == null) return "";
        String result = template;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            result = result.replace(pairs[i], pairs[i + 1]);
        }
        return result;
    }

    public static void send(CommandSender sender, Component component) {
        sender.sendMessage(LEGACY_SECTION.serialize(component));
    }

    public static void send(CommandSender sender, String miniMessage) {
        send(sender, parse(miniMessage));
    }
}
