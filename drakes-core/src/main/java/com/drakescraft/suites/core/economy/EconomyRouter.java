package com.drakescraft.suites.core.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Proveedor Vault "router": se registra con prioridad Highest y en CADA llamada
 * delega al mejor proveedor real (InvSwitcher/BentoBox si existe; si no, Essentials u otro).
 * Asi los plugins que fijan el proveedor en su onEnable siguen la economia por modalidad.
 */
public final class EconomyRouter {

    /**
     * Nombre que Essentials reconoce como propio: si fuera otro, EssentialsX lo toma como
     * "capa Vault" de almacenamiento y se produce un bucle infinito Essentials -> router -> Essentials.
     */
    static final String NAME = "Essentials Economy";
    static final String OWNER_PLUGIN = "DrakesCore";

    private EconomyRouter() {
    }

    /** {@code resolver} devuelve el proveedor real actual (o null); se invoca en cada llamada. */
    public static Economy create(Supplier<Economy> resolver, Logger log) {
        return (Economy) Proxy.newProxyInstance(Economy.class.getClassLoader(), new Class<?>[] { Economy.class },
                (proxy, m, args) -> {
                    String n = m.getName();
                    switch (n) {
                        case "getName": return NAME;
                        case "hasBankSupport": return false;
                        case "getBanks": return List.of();
                        case "toString": return "EconomyRouter";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: break;
                    }
                    if (n.toLowerCase(Locale.ROOT).contains("bank")) {
                        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bancos no soportados");
                    }
                    Economy real = null;
                    try {
                        real = resolver.get();
                    } catch (RuntimeException e) {
                        log.warning("[EconomyRouter] fallo resolviendo proveedor: " + e);
                    }
                    if (real == null) {
                        return fallback(m.getReturnType(), n, args);
                    }
                    try {
                        return m.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause() != null ? e.getCause() : e;
                    }
                });
    }

    private static Object fallback(Class<?> rt, String name, Object[] args) {
        if (rt == boolean.class) return false;
        if (rt == double.class) return 0.0D;
        if (rt == int.class) return 2;
        if (rt == String.class) {
            return name.equals("format") ? String.format(Locale.US, "%.2f", (double) args[0]) : "";
        }
        if (rt == EconomyResponse.class) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Sin proveedor de economia");
        }
        return null;
    }

    /** Resolucion real contra el ServicesManager de Bukkit (excluye siempre el propio router). */
    public static Supplier<Economy> bukkitResolver() {
        return () -> pick(Bukkit.getServicesManager().getRegistrations(Economy.class).stream()
                .map(r -> new Candidate(r.getPlugin().getName(), r.getProvider())).toList());
    }

    record Candidate(String plugin, Economy eco) {
    }

    /** Orden: BentoBox/InvSwitcher > Essentials > cualquier otro. Ignora el router (registrado por DrakesCore). */
    static Economy pick(List<Candidate> cands) {
        Economy essentials = null;
        Economy other = null;
        for (Candidate c : cands) {
            if (c.eco() == null || OWNER_PLUGIN.equals(c.plugin())) {
                continue;
            }
            String p = c.plugin() == null ? "" : c.plugin().toLowerCase(Locale.ROOT);
            String k = c.eco().getClass().getName().toLowerCase(Locale.ROOT);
            if (p.contains("invswitcher") || p.contains("bentobox") || k.contains("invswitcher")) {
                return c.eco();
            }
            if (essentials == null && p.contains("essentials")) essentials = c.eco();
            else if (other == null) other = c.eco();
        }
        return essentials != null ? essentials : other;
    }
}
