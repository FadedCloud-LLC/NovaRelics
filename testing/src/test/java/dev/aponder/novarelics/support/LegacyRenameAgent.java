package dev.aponder.novarelics.support;

import dev.aponder.novarelics.support.api.ApiSurface;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TEST DOUBLE for CraftBukkit's legacy plugin rewriting ("Commodore"/"FieldRename").
 *
 * <p>Real Spigot/Paper servers rewrite the bytecode of plugins whose plugin.yml declares an
 * old {@code api-version} (NovaRelics declares 1.18): renamed enum constants are redirected
 * and calls on types that became interfaces are fixed. MockBukkit does not do this, so the
 * unchanged production classes cannot even enable under MockBukkit (NoSuchFieldError on
 * {@code PotionEffectType.CONFUSION}).
 *
 * <p>This agent reproduces <b>only</b> the renames NovaRelics needs (taken from
 * {@code ApiCompatibilityTest} output) so the rest of the behaviour can be tested. It is not
 * proof that real servers behave the same way — that is what the real-server matrix is for
 * (NR-016). Disable with {@code -Dnovarelics.legacyRename=false} to see the raw behaviour.
 */
public final class LegacyRenameAgent {

    /** owner#oldName -> newName. Only applied when the old field is missing in the running API. */
    static final Map<String, String> FIELD_RENAMES = Map.of(
            "org/bukkit/potion/PotionEffectType#CONFUSION", "NAUSEA",
            "org/bukkit/potion/PotionEffectType#HARM", "INSTANT_DAMAGE",
            "org/bukkit/potion/PotionEffectType#SLOW", "SLOWNESS",
            "org/bukkit/potion/PotionEffectType#SLOW_DIGGING", "MINING_FATIGUE",
            "org/bukkit/Particle#CRIT_MAGIC", "ENCHANTED_HIT",
            "org/bukkit/Particle#SPELL_WITCH", "WITCH",
            "org/bukkit/attribute/Attribute#GENERIC_MAX_HEALTH", "MAX_HEALTH");

    private static final AtomicBoolean INSTALLED = new AtomicBoolean();
    private static final ApiSurface API = ApiSurface.ofClasspath("runtime");

    private LegacyRenameAgent() { }

    public static synchronized void install() {
        if (!Boolean.parseBoolean(System.getProperty("novarelics.legacyRename", "true"))) return;
        if (!INSTALLED.compareAndSet(false, true)) return;
        Instrumentation inst = ByteBuddyAgent.install();
        inst.addTransformer(new Transformer(), false);
    }

    public static boolean isInstalled() { return INSTALLED.get(); }

    static byte[] rewrite(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, 0);
        boolean[] changed = {false};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, sig, ex)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String fname, String fdesc) {
                        String renamed = FIELD_RENAMES.get(owner + "#" + fname);
                        if (renamed != null && !API.hasField(owner, fname, fdesc) && API.hasField(owner, renamed, fdesc)) {
                            fname = renamed;
                            changed[0] = true;
                        }
                        super.visitFieldInsn(opcode, owner, fname, fdesc);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String mname, String mdesc, boolean itf) {
                        // enum -> interface conversions (e.g. org.bukkit.Sound in 1.21.3+)
                        if (ApiSurface.isPlatformOwner(owner) && !owner.startsWith("[") && API.hasClass(owner)) {
                            boolean actual = API.isInterface(owner);
                            if (actual != itf) {
                                if (opcode == Opcodes.INVOKEVIRTUAL && actual) opcode = Opcodes.INVOKEINTERFACE;
                                else if (opcode == Opcodes.INVOKEINTERFACE && !actual) opcode = Opcodes.INVOKEVIRTUAL;
                                itf = actual;
                                changed[0] = true;
                            }
                        }
                        super.visitMethodInsn(opcode, owner, mname, mdesc, itf);
                    }
                };
            }
        }, 0);
        return changed[0] ? writer.toByteArray() : null;
    }

    private static final class Transformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> redefined,
                                ProtectionDomain pd, byte[] bytes) {
            if (className == null || !className.startsWith("dev/aponder/novarelics/")
                    || className.startsWith("dev/aponder/novarelics/support/")) return null;
            try {
                return rewrite(bytes);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }
}
