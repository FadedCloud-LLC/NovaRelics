package dev.aponder.novarelics.support;

import org.mockbukkit.mockbukkit.ServerMock;

/**
 * ServerMock that can report a chosen Bukkit version string.
 * The default ("1.21.11-R0.1-SNAPSHOT") passes NovaRelics' current version allowlist
 * (NR-002), so behavioural tests are not all masked by that single defect.
 */
public class CompatServerMock extends ServerMock {

    public static final String ALLOWLISTED_VERSION = "1.21.11-R0.1-SNAPSHOT";

    private final String bukkitVersion;

    public CompatServerMock() { this(ALLOWLISTED_VERSION); }

    public CompatServerMock(String bukkitVersion) { this.bukkitVersion = bukkitVersion; }

    @Override
    public String getBukkitVersion() { return bukkitVersion; }
}
