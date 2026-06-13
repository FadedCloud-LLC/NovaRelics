package dev.aponder.novarelics.ability;

import java.util.HashMap;
import java.util.Map;

public class AbilityConfig {

    private final String type;
    private final Map<String, Object> params;

    public AbilityConfig(String type, Map<String, Object> params) {
        this.type = type.toUpperCase();
        this.params = new HashMap<>(params);
    }

    public String getType() { return type; }

    public Map<String, Object> getParams() { return params; }

    public String getString(String key, String def) {
        Object v = params.get(key);
        return v != null ? v.toString() : def;
    }

    public int getInt(String key, int def) {
        Object v = params.get(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException e) { return def; }
    }

    public double getDouble(String key, double def) {
        Object v = params.get(key);
        if (v == null) return def;
        try { return Double.parseDouble(v.toString()); }
        catch (NumberFormatException e) { return def; }
    }

    public boolean getBoolean(String key, boolean def) {
        Object v = params.get(key);
        if (v == null) return def;
        return Boolean.parseBoolean(v.toString());
    }

    public boolean has(String key) { return params.containsKey(key); }

    @Override
    public String toString() {
        return "AbilityConfig{type=" + type + ", params=" + params + "}";
    }
}
