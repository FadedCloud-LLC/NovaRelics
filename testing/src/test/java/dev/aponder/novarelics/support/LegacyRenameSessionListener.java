package dev.aponder.novarelics.support;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/** Installs {@link LegacyRenameAgent} once, before any test class (and any plugin class) loads. */
public class LegacyRenameSessionListener implements LauncherSessionListener {
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        LegacyRenameAgent.install();
    }
}
