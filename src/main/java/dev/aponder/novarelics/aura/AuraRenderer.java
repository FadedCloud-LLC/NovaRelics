package dev.aponder.novarelics.aura;

import dev.aponder.novarelics.NovaRelics;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface AuraRenderer {
    void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin);
}
