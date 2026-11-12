package dev.aponder.novarelics.support;

/** YAML snippets for synthetic test relics. */
public final class Relics {
    private Relics() { }

    /** A relic whose every listed trigger sends the chat message "fired &lt;TRIGGER&gt;". */
    public static String probe(String id, int cooldownSeconds, String... triggers) {
        StringBuilder sb = new StringBuilder()
                .append("id: ").append(id).append('\n')
                .append("display-name: \"Probe ").append(id).append("\"\n")
                .append("material: STICK\n")
                .append("rarity: common\n")
                .append("triggers:\n");
        for (String t : triggers) {
            sb.append("  ").append(t).append(":\n")
              .append("    - type: MESSAGE\n")
              .append("      message: \"fired ").append(t).append("\"\n");
        }
        sb.append("cooldown:\n  type: PER_PLAYER\n  duration: ").append(cooldownSeconds).append("\n  shared-key: \"\"\n");
        return sb.toString();
    }

    /** One trigger with one ability and arbitrary extra parameter lines (already indented 6 spaces). */
    public static String single(String id, String trigger, String abilityType, String... paramLines) {
        StringBuilder sb = new StringBuilder()
                .append("id: ").append(id).append('\n')
                .append("display-name: \"").append(id).append("\"\n")
                .append("material: STICK\n")
                .append("triggers:\n")
                .append("  ").append(trigger).append(":\n")
                .append("    - type: ").append(abilityType).append('\n');
        for (String p : paramLines) sb.append("      ").append(p).append('\n');
        return sb.toString();
    }
}
