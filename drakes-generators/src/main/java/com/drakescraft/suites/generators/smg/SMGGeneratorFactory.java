package com.drakescraft.suites.generators.smg;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Fábrica de items para generadores de materiales en SMG (DrakesGenerators).
 */
public final class SMGGeneratorFactory {

    public static final String ID_MULTIBLOCK = "SMG_GENERATOR_MULTIBLOCK";
    public static final String PDC_NAMESPACE = "drakesgenerators";
    public static final String PDC_KEY_TYPE = "smg_type";
    public static final String PDC_KEY_RATE = "smg_rate";

    private SMGGeneratorFactory() {}

    public static ItemStack createGenerator(SMGMaterialType type, int rateTicks) {
        ItemStack item = new ItemStack(type.getBlockIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&9" + type.getDisplayName() + " Generator");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesGenerators",
                    "&6Velocidad de generación: &e" + rateTicks + " ticks",
                    "&7Produce: &f" + type.getDisplayName(),
                    "",
                    "&7Coloca un cofre directamente encima para",
                    "&7recolectar la producción automática.",
                    "&eOutputs automatically to chest above."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, type.getCanonicalId());
        SuiteItemPdcBridge.setCustomString(item, PDC_NAMESPACE, PDC_KEY_TYPE, type.name());
        SuiteItemPdcBridge.setCustomInt(item, PDC_NAMESPACE, PDC_KEY_RATE, rateTicks);
        return item;
    }

    public static ItemStack createBrokenGenerator(SMGMaterialType type) {
        ItemStack item = new ItemStack(type.getBlockIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&7" + type.getDisplayName() + " Generator &8(Broken)");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesGenerators",
                    "&cEstructura dañada o descalibrada.",
                    "&7Requiere reparación en mesa de trabajo.",
                    "",
                    "&8Needs to be repaired."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, type.getBrokenId());
        SuiteItemPdcBridge.setCustomString(item, PDC_NAMESPACE, PDC_KEY_TYPE, type.name());
        return item;
    }

    public static ItemStack createMultiblockGuide() {
        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "&9Generator Multiblock");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&8StarSuites · DrakesGenerators",
                    "&7Assembly guide for SMG generators.",
                    "&7Only outputs to the chest directly above.",
                    "",
                    "&ePlace chest directly above the generator."
            ));
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, ID_MULTIBLOCK);
        return item;
    }

    public static boolean isMaterialGenerator(ItemStack item) {
        String id = SuiteItemPdcBridge.getSlimefunId(item);
        return id != null && id.startsWith("SMG_GENERATOR_") && !id.endsWith("_BROKEN") && !id.equals(ID_MULTIBLOCK);
    }

    public static boolean isBrokenGenerator(ItemStack item) {
        String id = SuiteItemPdcBridge.getSlimefunId(item);
        return id != null && id.startsWith("SMG_GENERATOR_") && id.endsWith("_BROKEN");
    }

    public static SMGMaterialType getMaterialType(ItemStack item) {
        String typeStr = SuiteItemPdcBridge.getCustomString(item, PDC_NAMESPACE, PDC_KEY_TYPE);
        if (typeStr != null) {
            try {
                return SMGMaterialType.valueOf(typeStr);
            } catch (IllegalArgumentException ignored) {}
        }
        String id = SuiteItemPdcBridge.getSlimefunId(item);
        if (id == null) return null;
        for (SMGMaterialType type : SMGMaterialType.values()) {
            if (id.equals(type.getCanonicalId()) || id.equals(type.getBrokenId())) {
                return type;
            }
        }
        return null;
    }
}
