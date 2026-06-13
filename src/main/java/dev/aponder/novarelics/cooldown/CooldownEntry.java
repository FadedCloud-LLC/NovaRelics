package dev.aponder.novarelics.cooldown;

public class CooldownEntry {

    private final String key;
    private final long expiresAt;

    public CooldownEntry(String key, long expiresAt) {
        this.key = key;
        this.expiresAt = expiresAt;
    }

    public String getKey() { return key; }

    public long getExpiresAt() { return expiresAt; }

    public boolean isExpired() { return System.currentTimeMillis() >= expiresAt; }

    public long getRemainingMs() { return Math.max(0, expiresAt - System.currentTimeMillis()); }

    public long getRemainingSeconds() { return getRemainingMs() / 1000L; }
}
