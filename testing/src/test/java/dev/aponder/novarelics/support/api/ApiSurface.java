package dev.aponder.novarelics.support.api;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipFile;

/**
 * A server API "surface" built purely from class-file bytes (no class loading),
 * so several API versions can be inspected side by side in one JVM.
 */
public final class ApiSurface implements AutoCloseable {

    /** Owners that belong to the server platform API (everything else is ignored). */
    public static final List<String> PLATFORM_PREFIXES = List.of(
            "org/bukkit/", "io/papermc/", "com/destroystokyo/", "net/kyori/",
            "net/md_5/bungee/", "co/aikar/");

    public static boolean isPlatformOwner(String internalName) {
        for (String p : PLATFORM_PREFIXES) if (internalName.startsWith(p)) return true;
        return false;
    }

    private final String name;
    private final List<Function<String, InputStream>> sources = new ArrayList<>();
    private final List<ZipFile> zips = new ArrayList<>();
    private final Map<String, Optional<ClassNode>> cache = new HashMap<>();

    private ApiSurface(String name) { this.name = name; }

    public String name() { return name; }

    /** Surface made of the given jars, then the JDK. Nothing else from the test classpath leaks in. */
    public static ApiSurface ofJars(String name, List<Path> jars) {
        ApiSurface s = new ApiSurface(name);
        for (Path jar : jars) {
            try {
                ZipFile zip = new ZipFile(jar.toFile());
                s.zips.add(zip);
                s.sources.add(n -> {
                    var e = zip.getEntry(n + ".class");
                    try { return e == null ? null : zip.getInputStream(e); }
                    catch (IOException ex) { throw new UncheckedIOException(ex); }
                });
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot open " + jar, e);
            }
        }
        s.addJdk();
        return s;
    }

    /** Surface from the current test classpath (the paper-api MockBukkit runs against). */
    public static ApiSurface ofClasspath(String name) {
        ApiSurface s = new ApiSurface(name);
        ClassLoader cl = ApiSurface.class.getClassLoader();
        s.sources.add(n -> cl.getResourceAsStream(n + ".class"));
        return s;
    }

    /** Adds net/kyori classes from the test classpath (Paper ships Adventure as separate artifacts). */
    public ApiSurface withClasspathAdventure() {
        ClassLoader cl = ApiSurface.class.getClassLoader();
        sources.add(n -> n.startsWith("net/kyori/") ? cl.getResourceAsStream(n + ".class") : null);
        return this;
    }

    private void addJdk() {
        ClassLoader platform = ClassLoader.getPlatformClassLoader();
        sources.add(n -> n.startsWith("java/") || n.startsWith("javax/") || n.startsWith("jdk/")
                ? platform.getResourceAsStream(n + ".class") : null);
    }

    public Optional<ClassNode> find(String internalName) {
        return cache.computeIfAbsent(internalName, n -> {
            for (var src : sources) {
                try (InputStream in = src.apply(n)) {
                    if (in != null) {
                        ClassNode node = new ClassNode();
                        new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
                        return Optional.of(node);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return Optional.empty();
        });
    }

    public boolean hasClass(String owner) { return find(owner).isPresent(); }

    public boolean isInterface(String owner) {
        return find(owner).map(c -> (c.access & Opcodes.ACC_INTERFACE) != 0).orElse(false);
    }

    /** Field lookup following JVMS 5.4.3.2 (class, superinterfaces, superclasses). */
    public boolean hasField(String owner, String fieldName, String desc) {
        return resolveField(owner, fieldName, desc, new HashSet<>());
    }

    private boolean resolveField(String owner, String n, String d, Set<String> seen) {
        if (owner == null || !seen.add(owner)) return false;
        Optional<ClassNode> c = find(owner);
        if (c.isEmpty()) return false;
        for (FieldNode f : c.get().fields) if (f.name.equals(n) && f.desc.equals(d)) return true;
        for (String i : c.get().interfaces) if (resolveField(i, n, d, seen)) return true;
        return resolveField(c.get().superName, n, d, seen);
    }

    /** Method lookup across the full hierarchy (classes and interfaces). */
    public boolean hasMethod(String owner, String methodName, String desc) {
        return resolveMethod(owner, methodName, desc, new HashSet<>());
    }

    private boolean resolveMethod(String owner, String n, String d, Set<String> seen) {
        if (owner == null || !seen.add(owner)) return false;
        Optional<ClassNode> c = find(owner);
        if (c.isEmpty()) return false;
        for (MethodNode m : c.get().methods) if (m.name.equals(n) && m.desc.equals(d)) return true;
        if (resolveMethod(c.get().superName, n, d, seen)) return true;
        for (String i : c.get().interfaces) if (resolveMethod(i, n, d, seen)) return true;
        // Enum synthetic methods are always present on enums
        return false;
    }

    @Override
    public void close() {
        for (ZipFile z : zips) { try { z.close(); } catch (IOException ignored) { } }
    }

    @Override
    public String toString() { return name; }
}
