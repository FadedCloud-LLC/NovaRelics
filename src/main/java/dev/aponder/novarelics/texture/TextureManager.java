package dev.aponder.novarelics.texture;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TextureManager {

    private final NovaRelics plugin;

    public TextureManager(NovaRelics plugin) {
        this.plugin = plugin;
    }

    /**
     * Resolves the base ItemStack for a relic, handling all texture source types.
     * Priority: ItemsAdder > Oraxen > Nexo > HeadDatabase > Base64/URL > Material
     */
    public ItemStack resolve(RelicDefinition def) {
        // ItemsAdder
        if (def.getItemsAdderId() != null && !def.getItemsAdderId().isEmpty()) {
            ItemStack ia = plugin.getHookManager().getItemsAdderHook().buildItem(def.getItemsAdderId());
            if (ia != null) return ia;
        }

        // Oraxen
        if (def.getOraxenId() != null && !def.getOraxenId().isEmpty()) {
            ItemStack oraxen = plugin.getHookManager().getOraxenHook().buildItem(def.getOraxenId());
            if (oraxen != null) return oraxen;
        }

        // Nexo
        if (def.getNexoId() != null && !def.getNexoId().isEmpty()) {
            ItemStack nexo = plugin.getHookManager().getNexoHook().buildItem(def.getNexoId());
            if (nexo != null) return nexo;
        }

        // HeadDatabase
        if (def.getHeadDatabaseId() != null && !def.getHeadDatabaseId().isEmpty()) {
            ItemStack hdb = plugin.getHookManager().getHeadDatabaseHook().getHead(def.getHeadDatabaseId());
            if (hdb != null) return hdb;
        }

        // Base64 skull texture
        if (def.getSkullTexture() != null && !def.getSkullTexture().isEmpty()) {
            String texture = def.getSkullTexture();
            if (texture.startsWith("http://") || texture.startsWith("https://")) {
                return SkullTextureUtil.fromUrl(texture);
            }
            return SkullTextureUtil.fromBase64(texture);
        }

        // Fallback to material
        Material mat = def.getMaterial();
        if (mat == null || mat.isAir()) mat = Material.PAPER;
        return new ItemStack(mat);
    }
}
