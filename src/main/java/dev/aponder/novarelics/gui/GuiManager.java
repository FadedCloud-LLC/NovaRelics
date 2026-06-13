package dev.aponder.novarelics.gui;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import org.bukkit.entity.Player;

public class GuiManager {

    private final NovaRelics plugin;

    public GuiManager(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public void openEditor(Player player) {
        new RelicListGui(plugin, player, 0).open();
    }

    public void openRelicEditor(Player player, RelicDefinition relic) {
        new RelicEditorGui(plugin, player, relic).open();
    }

    public void openRelicEditor(Player player, String relicId) {
        RelicDefinition relic = plugin.getRelicManager().getRelic(relicId);
        if (relic == null) {
            dev.aponder.novarelics.util.MiniMsg.send(player, dev.aponder.novarelics.util.MiniMsg.parse(
                    plugin.getConfigManager().getMessage("unknown-relic",
                            "<relic>", relicId)));
            return;
        }
        openRelicEditor(player, relic);
    }
}
