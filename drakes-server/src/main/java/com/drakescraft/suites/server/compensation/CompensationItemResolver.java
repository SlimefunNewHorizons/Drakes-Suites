package com.drakescraft.suites.server.compensation;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resuelve identificadores de ítems Slimefun (por ID) o vanilla (Material)
 * y los divide en stacks respetando el tamaño máximo de pila.
 */
public final class CompensationItemResolver {

    @FunctionalInterface
    public interface ItemLookup {
        @Nullable
        ItemStack resolve(@Nonnull String rawId);
    }

    private static volatile boolean reflectionChecked = false;
    private static volatile Method sfGetByIdMethod = null;
    private static volatile Method sfGetItemMethod = null;

    private CompensationItemResolver() {}

    /**
     * Parsea una especificación de lote como "FM_FAST_SMELTERY:2,AUTO_ENCHANTER:1".
     * Valida la sintaxis de todas las entradas de forma atómica antes de devolver el mapa.
     */
    @Nonnull
    public static Map<String, Integer> parseBatchSpec(@Nonnull String spec) {
        if (spec == null || spec.isBlank()) {
            throw new IllegalArgumentException("La lista de ítems no puede estar vacía.");
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        String[] parts = spec.split(",");
        for (String rawPart : parts) {
            String part = rawPart.trim();
            if (part.isEmpty()) {
                continue;
            }
            String id;
            int qty = 1;
            int sep = part.lastIndexOf(':');
            if (sep >= 0) {
                id = part.substring(0, sep).trim();
                String qtyStr = part.substring(sep + 1).trim();
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Cantidad inválida en '" + part + "'.");
                }
            } else {
                id = part;
            }
            if (id.isEmpty()) {
                throw new IllegalArgumentException("ID vacío en '" + part + "'.");
            }
            if (qty <= 0 || qty > 2304) {
                throw new IllegalArgumentException("Cantidad fuera de rango (1..2304) en '" + part + "'.");
            }
            String key = id.toUpperCase(Locale.ROOT);
            result.merge(key, qty, Integer::sum);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron ítems válidos en el lote.");
        }
        return result;
    }

    /**
     * Resuelve un ítem individual (Slimefun primero, luego Material vanilla).
     */
    @Nullable
    public static ItemStack resolveSingle(@Nonnull String rawId) {
        String id = rawId.trim().toUpperCase(Locale.ROOT);
        if (id.isEmpty()) {
            return null;
        }
        ItemStack sf = resolveSlimefun(id);
        if (sf != null) {
            return sf;
        }
        Material mat = Material.matchMaterial(id);
        if (mat != null && mat.isItem() && !mat.isAir()) {
            return new ItemStack(mat, 1);
        }
        return null;
    }

    /**
     * Resuelve y expande un lote completo de forma atómica (si falla 1 ID, lanza excepción sin devolver nada parcial).
     */
    @Nonnull
    public static List<ItemStack> resolveBatch(@Nonnull Map<String, Integer> spec, @Nonnull ItemLookup lookup) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : spec.entrySet()) {
            ItemStack template = lookup.resolve(entry.getKey());
            if (template == null || template.getType().isAir()) {
                throw new IllegalArgumentException("Ítem desconocido: '" + entry.getKey() + "'.");
            }
            stacks.addAll(splitIntoStacks(template, entry.getValue()));
        }
        return stacks;
    }

    @Nonnull
    public static List<ItemStack> splitIntoStacks(@Nonnull ItemStack template, int totalAmount) {
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }
        int maxStack = Math.max(1, template.getMaxStackSize());
        List<ItemStack> result = new ArrayList<>();
        int remaining = totalAmount;
        while (remaining > 0) {
            int count = Math.min(maxStack, remaining);
            ItemStack copy = template.clone();
            copy.setAmount(count);
            result.add(copy);
            remaining -= count;
        }
        return result;
    }

    @Nullable
    private static ItemStack resolveSlimefun(@Nonnull String id) {
        initReflection();
        if (sfGetByIdMethod == null || sfGetItemMethod == null) {
            return null;
        }
        try {
            Object sfItem = sfGetByIdMethod.invoke(null, id);
            if (sfItem == null) {
                return null;
            }
            Object raw = sfGetItemMethod.invoke(sfItem);
            if (raw instanceof ItemStack stack && !stack.getType().isAir()) {
                ItemStack clone = stack.clone();
                clone.setAmount(1);
                return clone;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }

    private static void initReflection() {
        if (reflectionChecked) {
            return;
        }
        reflectionChecked = true;
        String[] candidates = {
            "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem",
            "com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem"
        };
        for (String clsName : candidates) {
            try {
                Class<?> cls = Class.forName(clsName);
                sfGetByIdMethod = cls.getMethod("getById", String.class);
                sfGetItemMethod = cls.getMethod("getItem");
                return;
            } catch (ReflectiveOperationException ignored) {
                // try next
            }
        }
    }
}
