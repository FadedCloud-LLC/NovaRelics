package dev.aponder.novarelics.texture;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.UUID;

public final class SkullTextureUtil {

    private SkullTextureUtil() {}

    public static ItemStack fromBase64(String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (base64 == null || base64.isEmpty()) return skull;
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        try {
            // Build a GameProfile via reflection — avoids compile-time authlib dependency
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");

            Constructor<?> profileCtor = gameProfileClass.getConstructor(UUID.class, String.class);
            Object profile = profileCtor.newInstance(UUID.randomUUID(), "novarelics");

            Constructor<?> propertyCtor = propertyClass.getConstructor(String.class, String.class);
            Object property = propertyCtor.newInstance("textures", encodeIfNeeded(base64));

            Method getProperties = gameProfileClass.getMethod("getProperties");
            Object propertyMap = getProperties.invoke(profile);
            Method put = propertyMap.getClass().getMethod("put", Object.class, Object.class);
            put.invoke(propertyMap, "textures", property);

            // Try direct field injection first, then Paper's setProfile method
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (NoSuchFieldException e) {
                Method setProfile = meta.getClass().getDeclaredMethod("setProfile", gameProfileClass);
                setProfile.setAccessible(true);
                setProfile.invoke(meta, profile);
            }
        } catch (Exception e) {
            dev.aponder.novarelics.NovaRelics.getInstance()
                    .getLogger().warning("Failed to set skull texture: " + e.getMessage());
        }

        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack fromUrl(String url) {
        if (url == null || url.isEmpty()) return new ItemStack(Material.PLAYER_HEAD);
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes());
        return fromBase64(base64);
    }

    private static String encodeIfNeeded(String value) {
        try {
            String decoded = new String(Base64.getDecoder().decode(value));
            if (decoded.contains("textures")) return value;
        } catch (Exception ignored) {}
        return value;
    }
}
