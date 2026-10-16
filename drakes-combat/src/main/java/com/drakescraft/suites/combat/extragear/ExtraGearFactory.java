package com.drakescraft.suites.combat.extragear;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fábrica de armaduras y armas ExtraGear con preservación de claves PDC idénticas a Slimefun.
 */
public final class ExtraGearFactory {

    public enum GearType {
        SWORD(Material.IRON_SWORD, "SWORD", "Sword"),
        HELMET(Material.IRON_HELMET, "HELMET", "Helmet"),
        CHESTPLATE(Material.IRON_CHESTPLATE, "CHESTPLATE", "Chestplate"),
        LEGGINGS(Material.IRON_LEGGINGS, "LEGGINGS", "Leggings"),
        BOOTS(Material.IRON_BOOTS, "BOOTS", "Boots");

        private final Material vanillaMaterial;
        private final String suffix;
        private final String translatedType;

        GearType(Material vanillaMaterial, String suffix, String translatedType) {
            this.vanillaMaterial = vanillaMaterial;
            this.suffix = suffix;
            this.translatedType = translatedType;
        }

        public Material getVanillaMaterial() {
            return vanillaMaterial;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getTranslatedType() {
            return translatedType;
        }
    }

    private ExtraGearFactory() {}

    public static String getSlimefunId(ExtraGearMaterial material, GearType type) {
        return material.getPrefix() + "_" + type.getSuffix();
    }

    public static ItemStack createGear(ExtraGearMaterial material, GearType type) {
        ItemStack item = new ItemStack(type.getVanillaMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String sfId = getSlimefunId(material, type);
            SuiteItemPdcBridge.setSlimefunId(meta, sfId);

            String displayName = material.getColorCode() + material.getDisplayName() + " " + type.getTranslatedType();
            CrossVersionAdapter.setDisplayName(meta, displayName);

            List<String> lore = new ArrayList<>();
            lore.add("§7High-resistance alloy forged in Slimefun");
            lore.add("§8[StarSuites · ExtraGear]");
            CrossVersionAdapter.setLore(meta, lore);

            Map<Enchantment, Integer> enchants = (type == GearType.SWORD)
                    ? material.getWeaponEnchants()
                    : material.getArmorEnchants();

            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}
