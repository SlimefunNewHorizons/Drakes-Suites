package com.drakescraft.suites.combat.tinker;

/**
 * Rasgos y afinidades elementales aplicables a armas y herramientas de SlimeTinker.
 */
public enum TinkerTrait {
    LIFESTEAL("Lifesteal", 0.15, "Restores a percentage of inflicted damage"),
    LIGHTNING_STRIKE("Celestial Strike", 0.25, "Summons lightning on hit"),
    VOID_EDGE("Void Edge", 0.20, "Ignores conventional physical armor"),
    DIVINE_SLAYER("Divine Slayer", 0.35, "Increases damage against DrakesBosses divine entities"),
    MOMENTUM("Cosmic Momentum", 0.10, "Increases attack speed with each consecutive strike"),
    REINFORCED("Primordial Reinforcement", 0.30, "Chance to avoid consuming durability"),
    COSMIC_DECAY("Cosmic Decay", 0.40, "Applies irreducible damage over time"),
    SINGULARITY_INFUSED("Singularity Infused", 0.50, "Applies massive true damage while consuming quantum density"),
    PRIMORDIAL_WRATH("Primordial Wrath", 0.60, "Breaks infinite invulnerability and absorbs Tier 10 divine essences");

    private final String displayName;
    private final double baseFactor;
    private final String description;

    TinkerTrait(String displayName, double baseFactor, String description) {
        this.displayName = displayName;
        this.baseFactor = baseFactor;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseFactor() {
        return baseFactor;
    }

    public String getDescription() {
        return description;
    }
}
