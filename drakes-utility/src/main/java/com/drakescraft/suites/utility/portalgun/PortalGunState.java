package com.drakescraft.suites.utility.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
import java.util.UUID;

/**
 * Stable Portal Gun state. The namespace intentionally remains the standalone
 * addon namespace so Season 1 items retain every configurable setting in S2.
 */
final class PortalGunState {
    static final int MAX_CHARGE = 100;
    static final int SHOT_COST = 5;
    static final int TRAVEL_COST = 25;
    static final int MAX_SAVED_LOCATIONS = 8;
    private static final String NAMESPACE = "sfportalgunsextension";
    private static final NamespacedKey ID = key("unique_portals_id");
    private static final NamespacedKey CHARGE = key("portal_charge");
    private static final NamespacedKey MODE = key("portal_mode");
    private static final NamespacedKey INFINITE = key("infinite_mode");
    private static final NamespacedKey SELECTED = key("selected_destination");
    private static final NamespacedKey NAME = key("portal_name");
    private static final NamespacedKey TINT = key("portal_tint");

    private PortalGunState() {}

    static String gunId(ItemStack item) {
        String id = pdc(item).get(ID, PersistentDataType.STRING);
        if (id == null) {
            id = UUID.randomUUID().toString();
            put(item, ID, PersistentDataType.STRING, id);
        }
        return id;
    }

    static int charge(ItemStack item) {
        Integer value = pdc(item).get(CHARGE, PersistentDataType.INTEGER);
        return value == null ? MAX_CHARGE : Math.clamp(value, 0, MAX_CHARGE);
    }

    static void setCharge(ItemStack item, int value) { put(item, CHARGE, PersistentDataType.INTEGER, Math.clamp(value, 0, MAX_CHARGE)); }
    static boolean consume(ItemStack item, int amount) {
        if (isInfinite(item)) return true;
        int current = charge(item);
        if (current < amount) return false;
        setCharge(item, current - amount);
        return true;
    }
    static void refill(ItemStack item) { setCharge(item, MAX_CHARGE); }

    static String mode(ItemStack item) { return "SAVED".equals(pdc(item).get(MODE, PersistentDataType.STRING)) ? "SAVED" : "LINKED"; }
    static void toggleMode(ItemStack item) { put(item, MODE, PersistentDataType.STRING, "LINKED".equals(mode(item)) ? "SAVED" : "LINKED"); }
    static boolean isInfinite(ItemStack item) { Byte value = pdc(item).get(INFINITE, PersistentDataType.BYTE); return value != null && value == (byte) 1; }
    static void setInfinite(ItemStack item, boolean value) { put(item, INFINITE, PersistentDataType.BYTE, (byte) (value ? 1 : 0)); }
    static int selectedSlot(ItemStack item) { Integer value = pdc(item).get(SELECTED, PersistentDataType.INTEGER); return value == null ? -1 : value; }
    static void setSelectedSlot(ItemStack item, int slot) { put(item, SELECTED, PersistentDataType.INTEGER, slot); }
    static String portalName(ItemStack item) { String value = pdc(item).get(NAME, PersistentDataType.STRING); return value == null || value.isBlank() ? "Portal" : value; }
    static String portalTint(ItemStack item) { String value = pdc(item).get(TINT, PersistentDataType.STRING); return value == null || value.isBlank() ? "LIME" : value; }

    static Location savedLocation(ItemStack item, int slot) {
        if (slot < 0 || slot >= MAX_SAVED_LOCATIONS) return null;
        String encoded = pdc(item).get(savedKey(slot), PersistentDataType.STRING);
        if (encoded == null) return null;
        String[] parts = encoded.split(";", -1);
        if (parts.length != 6) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
        } catch (NumberFormatException ignored) { return null; }
    }

    static boolean saveLocation(ItemStack item, Location location) {
        if (location.getWorld() == null) return false;
        for (int slot = 0; slot < MAX_SAVED_LOCATIONS; slot++) {
            if (savedLocation(item, slot) == null) {
                put(item, savedKey(slot), PersistentDataType.STRING, String.format(Locale.ROOT, "%s;%.3f;%.3f;%.3f;%.2f;%.2f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
                if (selectedSlot(item) < 0) setSelectedSlot(item, slot);
                return true;
            }
        }
        return false;
    }

    static void deleteLocation(ItemStack item, int slot) {
        remove(item, savedKey(slot));
        if (selectedSlot(item) == slot) remove(item, SELECTED);
    }

    private static NamespacedKey key(String value) { return new NamespacedKey(NAMESPACE, value); }
    private static NamespacedKey savedKey(int slot) { return key("saved_location_" + slot); }
    private static PersistentDataContainer pdc(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("Portal Gun must have item meta");
        return meta.getPersistentDataContainer();
    }
    private static <P, C> void put(ItemStack item, NamespacedKey key, PersistentDataType<P, C> type, C value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("Portal Gun must have item meta");
        meta.getPersistentDataContainer().set(key, type, value);
        item.setItemMeta(meta);
    }
    private static void remove(ItemStack item, NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().remove(key);
        item.setItemMeta(meta);
    }
}
