package com.chagui68.multiversenets.util;

import java.lang.reflect.Method;

import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

/**
 * Interruptores de rendimiento leidos de drakes-data/perf.yml (suites.multiverse.*) via
 * org.purpurmc.purpur.perf.DrakesPerf.cfg() del kernel DrakesPurpur, por reflexion. Sin kernel: todo false.
 */
public final class PerfFlags {

    /** suites.multiverse.no-snapshot: leer el inventario vecino con getState(false) en vez de copiar el tile entity. */
    public static volatile boolean noSnapshot = false;

    private static Method cfgMethod;
    private static Method getBoolean;
    private static boolean resolved;

    private PerfFlags() {}

    /** Una vez por ciclo del ticker. */
    public static void refresh() {
        try {
            if (!resolved) {
                resolved = true;
                Class<?> perf = Class.forName("org.purpurmc.purpur.perf.DrakesPerf");
                cfgMethod = perf.getMethod("cfg");
                getBoolean = cfgMethod.getReturnType().getMethod("getBoolean", String.class, boolean.class);
            }
            if (cfgMethod != null) {
                noSnapshot = (boolean) getBoolean.invoke(cfgMethod.invoke(null), "suites.multiverse.no-snapshot", false);
            }
        } catch (Throwable t) {
            cfgMethod = null;
        }
    }

    /** El InventoryHolder de un bloque contenedor, o null. Con noSnapshot no se copia el tile entity. */
    public static InventoryHolder holderOf(Block target) {
        return (noSnapshot ? target.getState(false) : target.getState()) instanceof InventoryHolder h ? h : null;
    }
}
