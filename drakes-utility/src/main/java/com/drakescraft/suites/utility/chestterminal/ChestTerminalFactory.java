package com.drakescraft.suites.utility.chestterminal;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Fábrica y validación de items para el sistema ChestTerminal de DrakesUtility.
 * Preserva 100% de compatibilidad con los IDs canónicos de Slimefun:
 * - MILKY_QUARTZ
 * - CT_PANEL
 * - CHEST_TERMINAL
 * - CT_IMPORT_BUS
 * - CT_EXPORT_BUS
 * - CT_WIRELESS_ACCESS_TERMINAL_16
 * - CT_WIRELESS_ACCESS_TERMINAL_64
 * - CT_WIRELESS_ACCESS_TERMINAL_128
 * - CT_WIRELESS_ACCESS_TERMINAL_TRANSDIMENSIONAL
 */
public final class ChestTerminalFactory {

    public static final String ID_MILKY_QUARTZ = "MILKY_QUARTZ";
    public static final String ID_PANEL = "CT_PANEL";
    public static final String ID_TERMINAL = "CHEST_TERMINAL";
    public static final String ID_IMPORT_BUS = "CT_IMPORT_BUS";
    public static final String ID_EXPORT_BUS = "CT_EXPORT_BUS";
    public static final String ID_WIRELESS_16 = "CT_WIRELESS_ACCESS_TERMINAL_16";
    public static final String ID_WIRELESS_64 = "CT_WIRELESS_ACCESS_TERMINAL_64";
    public static final String ID_WIRELESS_128 = "CT_WIRELESS_ACCESS_TERMINAL_128";
    public static final String ID_WIRELESS_TRANSDIMENSIONAL = "CT_WIRELESS_ACCESS_TERMINAL_TRANSDIMENSIONAL";

    public static final String PDC_NAMESPACE = "drakesutility";
    public static final String PDC_KEY_RANGE = "wireless_range";

    private ChestTerminalFactory() {}

    public static ItemStack createMilkyQuartz(int amount) {
        ItemStack item = new ItemStack(Material.QUARTZ, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&fMilky Quartz");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesUtility",
                    "&7Componente místico obtenido de recursos GEO.",
                    "&eMateria prima para la matriz de terminales."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, ID_MILKY_QUARTZ);
        return item;
    }

    public static ItemStack createAccessTerminal() {
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&3CT Access Terminal");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesUtility",
                    "&7Terminal de acceso centralizado a cofres.",
                    "&7Permite inspeccionar, filtrar y extraer items",
                    "&7conectados a la red de almacenamiento.",
                    "",
                    "&eDigital storage & network terminal."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, ID_TERMINAL);
        return item;
    }

    public static ItemStack createWirelessTerminal(int range) {
        ItemStack item = new ItemStack(Material.ITEM_FRAME);
        String id;
        String rangeStr;
        if (range <= 16) {
            id = ID_WIRELESS_16;
            rangeStr = "16 Bloques";
        } else if (range <= 64) {
            id = ID_WIRELESS_64;
            rangeStr = "64 Blocks";
        } else if (range <= 128) {
            id = ID_WIRELESS_128;
            rangeStr = "128 Blocks";
        } else {
            id = ID_WIRELESS_TRANSDIMENSIONAL;
            rangeStr = "Unlimited / Transdimensional";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&3CT Wireless Access Terminal &b(" + (range > 128 ? "Transdimensional" : range) + ")");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesUtility",
                    "&8\u21E8 &7Range: &e" + rangeStr,
                    "&8\u21E8 &7Linked: &cNot linked",
                    "",
                    "&7Right click an Access Terminal",
                    "&7to link the quantum frequency.",
                    "&eRight click terminal to link."
            ));
            item.setItemMeta(meta);
        }
        int effectiveRange = (range > 128) ? Integer.MAX_VALUE : range;
        SuiteItemPdcBridge.setSlimefunId(item, id);
        SuiteItemPdcBridge.setCustomInt(item, PDC_NAMESPACE, PDC_KEY_RANGE, effectiveRange);
        return item;
    }

    public static ItemStack createImportBus() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&3CT Import Bus");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesUtility",
                    "&7Extrae items automáticamente del contenedor",
                    "&7conectado hacia la red central de terminales.",
                    "",
                    "&eAuto-imports into CT network."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, ID_IMPORT_BUS);
        return item;
    }

    public static ItemStack createExportBus() {
        ItemStack item = new ItemStack(Material.DISPENSER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&3CT Export Bus");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesUtility",
                    "&7Envía items automáticamente desde la red",
                    "&7hacia el inventario objetivo conectado.",
                    "",
                    "&eAuto-exports from CT network."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, ID_EXPORT_BUS);
        return item;
    }

    public static boolean isAccessTerminal(ItemStack item) {
        return SuiteItemPdcBridge.hasSlimefunId(item, ID_TERMINAL);
    }

    public static boolean isWirelessTerminal(ItemStack item) {
        String id = SuiteItemPdcBridge.getSlimefunId(item);
        return id != null && id.startsWith("CT_WIRELESS_ACCESS_TERMINAL");
    }

    public static int getWirelessRange(ItemStack item) {
        Integer range = SuiteItemPdcBridge.getCustomInt(item, PDC_NAMESPACE, PDC_KEY_RANGE);
        if (range != null) return range;

        String id = SuiteItemPdcBridge.getSlimefunId(item);
        if (id == null) return 0;
        if (id.endsWith("_16")) return 16;
        if (id.endsWith("_64")) return 64;
        if (id.endsWith("_128")) return 128;
        if (id.endsWith("_TRANSDIMENSIONAL")) return Integer.MAX_VALUE;
        return 16;
    }
}
