package com.drakescraft.suites.generators.liquid;

import org.bukkit.Material;

/**
 * Metales y fluidos fundidos canónicos absorbidos del addon Liquid.
 */
public enum MoltenFluidType {
    MOLTEN_IRON("MOLTEN_IRON", "&6Molten Iron", 1538, Material.LAVA_BUCKET),
    MOLTEN_GOLD("MOLTEN_GOLD", "&eMolten Gold", 1064, Material.LAVA_BUCKET),
    MOLTEN_COPPER("MOLTEN_COPPER", "&cMolten Copper", 1085, Material.LAVA_BUCKET),
    MOLTEN_TIN("MOLTEN_TIN", "&7Molten Tin", 232, Material.LAVA_BUCKET),
    MOLTEN_ALUMINUM("MOLTEN_ALUMINUM", "&fMolten Aluminum", 660, Material.LAVA_BUCKET),
    MOLTEN_ZINC("MOLTEN_ZINC", "&8Molten Zinc", 420, Material.LAVA_BUCKET),
    MOLTEN_MAGNESIUM("MOLTEN_MAGNESIUM", "&dMolten Magnesium", 650, Material.LAVA_BUCKET),
    MOLTEN_LEAD("MOLTEN_LEAD", "&8Molten Lead", 327, Material.LAVA_BUCKET),
    MOLTEN_SILVER("MOLTEN_SILVER", "&fMolten Silver", 962, Material.LAVA_BUCKET);

    private final String slimefunId;
    private final String displayName;
    private final int meltingPointCelsius;
    private final Material material;

    MoltenFluidType(String slimefunId, String displayName, int meltingPointCelsius, Material material) {
        this.slimefunId = slimefunId;
        this.displayName = displayName;
        this.meltingPointCelsius = meltingPointCelsius;
        this.material = material;
    }

    public String getSlimefunId() {
        return slimefunId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMeltingPointCelsius() {
        return meltingPointCelsius;
    }

    public Material getMaterial() {
        return material;
    }

    public static MoltenFluidType getBySlimefunId(String id) {
        if (id == null) return null;
        for (MoltenFluidType type : values()) {
            if (type.slimefunId.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
