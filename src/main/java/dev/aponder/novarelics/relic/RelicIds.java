package dev.aponder.novarelics.relic;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Relic ids double as file names, so they are validated strictly (NR-012): no path
 * separators, no "..", nothing that could escape the relics/exports folders.
 */
public final class RelicIds {

    /** Human-readable rule, shown in error messages. */
    public static final String RULE = "a-z, 0-9, _ and -, 1-64 characters";

    private static final Pattern VALID = Pattern.compile("[a-z0-9_\\-]{1,64}");

    private RelicIds() { }

    public static boolean isValid(String id) {
        return id != null && VALID.matcher(id).matches();
    }

    /**
     * Resolves {@code <folder>/<name>.yml} and guarantees the result is directly inside
     * {@code folder}. Returns null for invalid names or anything that would escape the folder.
     */
    public static File fileIn(File folder, String name) {
        if (!isValid(name)) return null;
        try {
            File base = folder.getCanonicalFile();
            File target = new File(base, name + ".yml").getCanonicalFile();
            return base.equals(target.getParentFile()) ? target : null;
        } catch (IOException e) {
            return null;
        }
    }
}
