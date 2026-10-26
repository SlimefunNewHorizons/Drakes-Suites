package com.drakescraft.suites.core.pdc;

import com.drakescraft.suites.core.DrakesCorePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.regex.Pattern;

/**
 * Bridge unificado para PersistentDataContainer (PDC), preservacion de Item IDs
 * y deteccion de duplicaciones / anomalias de items en Paper 1.20.5 - 1.21.11+.
 * 
 * Garantiza que las claves canonicas como slimefun:slimefun_item se preserven
 * al 100% sin perdida de datos ni incompatibilidades de NBT vs Data Components.
 */
public final class SuiteItemPdcBridge {

    public static final NamespacedKey SLIMEFUN_ITEM_KEY = new NamespacedKey("slimefun", "slimefun_item");
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_.:-]+$");

    private SuiteItemPdcBridge() {}

    /**
     * Obtiene el ID canonico de Slimefun almacenado en el PDC del item.
     * Retorna null si el item es nulo, aire o no posee la clave PDC.
     */
    public static String getSlimefunId(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(SLIMEFUN_ITEM_KEY, PersistentDataType.STRING);
    }

    /**
     * Comprueba de manera rapida y sin asignaciones excesivas si el item es de Slimefun.
     */
    public static boolean isSlimefunItem(ItemStack item) {
        return getSlimefunId(item) != null;
    }

    /**
     * Comprueba si un item tiene un ID especifico de Slimefun en su PDC.
     */
    public static boolean hasSlimefunId(ItemStack item, String expectedId) {
        if (item == null || expectedId == null) {
            return false;
        }
        String id = getSlimefunId(item);
        return expectedId.equalsIgnoreCase(id);
    }

    /**
     * Asigna el ID canonico de Slimefun directamente a un ItemMeta.
     */
    public static boolean setSlimefunId(ItemMeta meta, String slimefunId) {
        if (meta == null || slimefunId == null || slimefunId.trim().isEmpty()) {
            return false;
        }
        meta.getPersistentDataContainer().set(SLIMEFUN_ITEM_KEY, PersistentDataType.STRING, slimefunId.trim());
        return true;
    }

    /**
     * Asigna el ID canonico de Slimefun al PDC del item de forma segura.
     */
    public static boolean setSlimefunId(ItemStack item, String slimefunId) {
        if (item == null || item.getType().isAir() || slimefunId == null || slimefunId.trim().isEmpty()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        setSlimefunId(meta, slimefunId);
        item.setItemMeta(meta);
        return true;
    }

    /**
     * Almacena una cadena personalizada en el PDC bajo un namespace especifico.
     */
    public static void setCustomString(ItemStack item, String namespace, String key, String value) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey namespacedKey = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    /**
     * Obtiene una cadena personalizada del PDC.
     */
    public static String getCustomString(ItemStack item, String namespace, String key) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * Almacena un entero en el PDC.
     */
    public static void setCustomInt(ItemStack item, String namespace, String key, int value) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey namespacedKey = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
        item.setItemMeta(meta);
    }

    /**
     * Obtiene un entero del PDC.
     */
    public static Integer getCustomInt(ItemStack item, String namespace, String key) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
    }

    /**
     * Almacena un long en el PDC.
     */
    public static void setCustomLong(ItemStack item, String namespace, String key, long value) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey namespacedKey = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.LONG, value);
        item.setItemMeta(meta);
    }

    /**
     * Obtiene un long del PDC.
     */
    public static Long getCustomLong(ItemStack item, String namespace, String key) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.LONG);
    }

    /**
     * Valida la integridad fisica y metadatos del item contra exploits de duplicacion (dupes)
     * e inyeccion de datos corruptos en el PDC.
     */
    public static boolean validateItemIntegrity(ItemStack item, String player, String location) {
        if (item == null || item.getType().isAir()) {
            return true;
        }

        int amount = item.getAmount();
        // Comprobacion 1: Cantidades imposibles o negativas (dupe por overflow)
        if (amount <= 0 || amount > 127) {
            logDupe(player, location, item.getType().name(), amount, "Cantidad de stack fuera de limites legales (1-127)");
            return false;
        }

        // Comprobacion 2: Validacion del Slimefun ID
        String sfId = getSlimefunId(item);
        if (sfId != null) {
            if (sfId.length() > 128) {
                logDupe(player, location, sfId, amount, "Slimefun ID excede tamano maximo permitido (>128 caracteres)");
                return false;
            }
            if (!VALID_ID_PATTERN.matcher(sfId).matches()) {
                logDupe(player, location, sfId, amount, "Slimefun ID contiene caracteres ilegales o sospechosos de inyeccion");
                return false;
            }
        }

        return true;
    }

    private static void logDupe(String player, String location, String item, int amount, String reason) {
        DrakesCorePlugin core = DrakesCorePlugin.getInstance();
        if (core != null && core.getAuditLogger() != null) {
            core.getAuditLogger().dupeAttempt("DrakesCore", "PDCBridge",
                    player != null ? player : "UNKNOWN",
                    location != null ? location : "UNKNOWN",
                    item, amount, reason);
        }
    }
}
