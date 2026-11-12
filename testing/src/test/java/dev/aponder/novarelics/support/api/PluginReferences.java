package dev.aponder.novarelics.support.api;

import dev.aponder.novarelics.NovaRelics;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/** Every platform-API symbol referenced by the production bytecode. */
public final class PluginReferences {

    public enum Kind { CLASS, FIELD, METHOD }

    /** One referenced symbol plus where it is used. */
    public record Ref(Kind kind, String owner, String name, String desc, boolean itf, String usedIn) {
        public String symbol() {
            return switch (kind) {
                case CLASS -> owner.replace('/', '.');
                case FIELD -> owner.replace('/', '.') + "#" + name;
                case METHOD -> owner.replace('/', '.') + "#" + name + desc;
            };
        }
    }

    private PluginReferences() { }

    public static Path productionClasses() {
        try {
            return Path.of(NovaRelics.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Ref> scan() {
        Path root = productionClasses().resolve("dev/aponder/novarelics");
        List<Ref> refs = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path p : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".class"))::iterator) {
                try (InputStream in = Files.newInputStream(p)) {
                    ClassNode cn = new ClassNode();
                    new ClassReader(in).accept(cn, 0);
                    collect(cn, refs);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return refs;
    }

    private static void collect(ClassNode cn, List<Ref> out) {
        String cls = cn.name.replace('/', '.');
        if (cn.superName != null) addClass(out, cn.superName, cls);
        for (String i : cn.interfaces) addClass(out, i, cls);
        for (MethodNode m : cn.methods) {
            String where = cls + "#" + m.name;
            for (AbstractInsnNode insn : m.instructions) {
                if (insn instanceof FieldInsnNode f && ApiSurface.isPlatformOwner(f.owner)) {
                    out.add(new Ref(Kind.FIELD, f.owner, f.name, f.desc, false, where));
                } else if (insn instanceof MethodInsnNode mi) {
                    String owner = mi.owner.startsWith("[") ? null : mi.owner;
                    if (owner != null && ApiSurface.isPlatformOwner(owner)) {
                        out.add(new Ref(Kind.METHOD, owner, mi.name, mi.desc, mi.itf, where));
                    }
                } else if (insn instanceof TypeInsnNode t) {
                    String desc = t.desc.startsWith("[") ? Type.getType(t.desc).getElementType().getInternalName() : t.desc;
                    if (ApiSurface.isPlatformOwner(desc)) addClass(out, desc, where);
                } else if (insn instanceof LdcInsnNode l && l.cst instanceof Type t && t.getSort() == Type.OBJECT) {
                    if (ApiSurface.isPlatformOwner(t.getInternalName())) addClass(out, t.getInternalName(), where);
                }
            }
        }
    }

    private static void addClass(List<Ref> out, String owner, String where) {
        if (ApiSurface.isPlatformOwner(owner)) out.add(new Ref(Kind.CLASS, owner, null, null, false, where));
    }

    /** Problems this surface would raise at link time (NoClassDefFound / NoSuchField / NoSuchMethod / ICCE). */
    public static SortedMap<String, SortedSet<String>> problems(ApiSurface api, List<Ref> refs) {
        SortedMap<String, SortedSet<String>> problems = new TreeMap<>();
        for (Ref r : refs) {
            String problem;
            if (!api.hasClass(r.owner())) {
                problem = "MISSING CLASS     " + r.owner().replace('/', '.');
            } else if (r.kind() == Kind.FIELD && !api.hasField(r.owner(), r.name(), r.desc())) {
                problem = "MISSING FIELD     " + r.symbol();
            } else if (r.kind() == Kind.METHOD && !"<init>".equals(r.name()) && api.isInterface(r.owner()) != r.itf()) {
                problem = "CLASS/INTERFACE   " + r.symbol() + " (compiled as " + (r.itf() ? "interface" : "class") + ")";
            } else if (r.kind() == Kind.METHOD && !api.hasMethod(r.owner(), r.name(), r.desc())
                    && !isEnumSynthetic(api, r)) {
                problem = "MISSING METHOD    " + r.symbol();
            } else {
                continue;
            }
            problems.computeIfAbsent(problem, k -> new TreeSet<>()).add(r.usedIn());
        }
        return problems;
    }

    private static boolean isEnumSynthetic(ApiSurface api, Ref r) {
        return api.find(r.owner()).map(c -> (c.access & Opcodes.ACC_ENUM) != 0).orElse(false)
                && (r.name().equals("values") || r.name().equals("valueOf") || r.name().equals("ordinal"));
    }
}
