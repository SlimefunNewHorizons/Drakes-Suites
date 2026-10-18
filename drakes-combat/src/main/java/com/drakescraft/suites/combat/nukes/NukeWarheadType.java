package com.drakescraft.suites.combat.nukes;

import org.bukkit.Material;

/**
 * Tipos canónicos de cabezas nucleares absorbidas de SlimefunNukes.
 */
public enum NukeWarheadType {
    LITTLE_NUKE("LITTLE_NUKE", "&cNuclear Warhead", 16, 30, false, Material.TNT),
    MEDIUM_NUKE("MEDIUM_NUKE", "&cMedium Nuclear Warhead", 32, 45, true, Material.TNT);

    private final String slimefunId;
    private final String displayName;
    private final int radius;
    private final int fuseSeconds;
    private final boolean incendiary;
    private final Material material;

    NukeWarheadType(String slimefunId, String displayName, int radius, int fuseSeconds, boolean incendiary, Material material) {
        this.slimefunId = slimefunId;
        this.displayName = displayName;
        this.radius = radius;
        this.fuseSeconds = fuseSeconds;
        this.incendiary = incendiary;
        this.material = material;
    }

    public String getSlimefunId() {
        return slimefunId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRadius() {
        return radius;
    }

    public int getFuseSeconds() {
        return fuseSeconds;
    }

    public boolean isIncendiary() {
        return incendiary;
    }

    public Material getMaterial() {
        return material;
    }

    public static NukeWarheadType getBySlimefunId(String id) {
        if (id == null) return null;
        for (NukeWarheadType type : values()) {
            if (type.slimefunId.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
