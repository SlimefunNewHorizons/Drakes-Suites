package com.drakescraft.suites.combat.extragear;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;

/**
 * Metales y aleaciones de Slimefun utilizados para la forja de armaduras y armas ExtraGear.
 */
public enum ExtraGearMaterial {
    COPPER("COPPER", "Copper", "§6",
            Map.of(Enchantment.SMITE, 2),
            Map.of(Enchantment.BLAST_PROTECTION, 2)),

    TIN("TIN", "Tin", "§7",
            Map.of(Enchantment.SHARPNESS, 1),
            Map.of(Enchantment.BLAST_PROTECTION, 3)),

    SILVER("SILVER", "Silver", "§f",
            Map.of(Enchantment.SHARPNESS, 2),
            Map.of(Enchantment.PROTECTION, 2)),

    ALUMINUM("ALUMINUM", "Aluminum", "§b",
            Map.of(Enchantment.BANE_OF_ARTHROPODS, 3),
            Map.of(Enchantment.BLAST_PROTECTION, 2, Enchantment.UNBREAKING, 2)),

    LEAD("LEAD", "Lead", "§8",
            Map.of(Enchantment.SHARPNESS, 3, Enchantment.UNBREAKING, 8),
            Map.of(Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 8)),

    ZINC("ZINC", "Zinc", "§7",
            Map.of(Enchantment.SHARPNESS, 2),
            Map.of(Enchantment.PROTECTION, 3)),

    MAGNESIUM("MAGNESIUM", "Magnesium", "§e",
            Map.of(Enchantment.SHARPNESS, 2, Enchantment.UNBREAKING, 5),
            Map.of(Enchantment.PROTECTION, 2, Enchantment.UNBREAKING, 5)),

    STEEL("STEEL", "Steel", "§8",
            Map.of(Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 6),
            Map.of(Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 4)),

    DAMASCUS_STEEL("DAMASCUS_STEEL", "Damascus Steel", "§4",
            Map.of(Enchantment.SHARPNESS, 6, Enchantment.UNBREAKING, 7),
            Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 6)),

    HARDENED_METAL("HARDENED_METAL", "Hardened Metal", "§3",
            Map.of(Enchantment.SHARPNESS, 7, Enchantment.UNBREAKING, 10),
            Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 8)),

    REINFORCED_ALLOY("REINFORCED_ALLOY", "Reinforced Alloy", "§b",
            Map.of(Enchantment.SHARPNESS, 8, Enchantment.UNBREAKING, 8),
            Map.of(Enchantment.PROTECTION, 5, Enchantment.UNBREAKING, 8)),

    OBSIDIAN("OBSIDIAN", "Reinforced Obsidian", "§5",
            Map.of(Enchantment.SHARPNESS, 8, Enchantment.FIRE_ASPECT, 2),
            Map.of(Enchantment.PROTECTION, 5, Enchantment.FIRE_PROTECTION, 4, Enchantment.UNBREAKING, 10));

    private final String prefix;
    private final String displayName;
    private final String colorCode;
    private final Map<Enchantment, Integer> weaponEnchants;
    private final Map<Enchantment, Integer> armorEnchants;

    ExtraGearMaterial(String prefix, String displayName, String colorCode,
                      Map<Enchantment, Integer> weaponEnchants,
                      Map<Enchantment, Integer> armorEnchants) {
        this.prefix = prefix;
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.weaponEnchants = weaponEnchants;
        this.armorEnchants = armorEnchants;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Map<Enchantment, Integer> getWeaponEnchants() {
        return weaponEnchants;
    }

    public Map<Enchantment, Integer> getArmorEnchants() {
        return armorEnchants;
    }
}
