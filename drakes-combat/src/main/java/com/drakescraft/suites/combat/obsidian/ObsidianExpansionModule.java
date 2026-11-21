package com.drakescraft.suites.combat.obsidian;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ObsidianExpansion & ObsidianArmor Module for DrakesCombat.
 * Provides reinforced volcanic plating, blast damage mitigation, and high-tier defensive armor sets.
 */
public class ObsidianExpansionModule extends AbstractSuiteModule implements Listener {

    private boolean damageReductionMemoization = true;
    private boolean fullSuitBonusCheck = true;
    private boolean blastAbsorptionPlate = true;
    private double trueDamageResistance = 0.25;

    public ObsidianExpansionModule(JavaPlugin plugin) {
        super(plugin, "obsidian_expansion", "ObsidianExpansion & ObsidianArmor T10 Plating");
    }

    static boolean isStandaloneObsidianPluginPresent() {
        var pm = org.bukkit.Bukkit.getPluginManager();
        return pm != null && (pm.getPlugin("ObsidianExpansion") != null || pm.getPlugin("Obsidian-Armor") != null);
    }

    @Override
    public void onEnable() {
        this.damageReductionMemoization = config.getBoolean("performance.damage-reduction-memoization", true);
        this.fullSuitBonusCheck = config.getBoolean("anti-dupe.full-suit-set-bonus-check", true);
        this.blastAbsorptionPlate = config.getBoolean("features.blast-absorption-plate", true);
        this.trueDamageResistance = config.getDouble("features.true-damage-resistance", 0.25);

        if (isStandaloneObsidianPluginPresent()) {
            logInfo("Plugin standalone ObsidianExpansion/Obsidian-Armor detectado; cediendo listener al addon dedicado.");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        logInfo("ObsidianExpansion module enabled (blastAbsorption=" + blastAbsorptionPlate + ", trueDamageRes=" + trueDamageResistance + ").");
    }

    @Override
    public void onDisable() {
        logInfo("ObsidianExpansion module disabled.");
    }

    public boolean isDamageReductionMemoization() {
        return damageReductionMemoization;
    }

    public boolean isFullSuitBonusCheck() {
        return fullSuitBonusCheck;
    }

    public boolean isBlastAbsorptionPlate() {
        return blastAbsorptionPlate;
    }

    public double getTrueDamageResistance() {
        return trueDamageResistance;
    }

    public boolean hasFullObsidianSuit(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        return SuiteItemPdcBridge.hasSlimefunId(helm, ObsidianItems.OBSIDIAN_HELMET) &&
               SuiteItemPdcBridge.hasSlimefunId(chest, ObsidianItems.OBSIDIAN_CHESTPLATE) &&
               SuiteItemPdcBridge.hasSlimefunId(legs, ObsidianItems.OBSIDIAN_LEGGINGS) &&
               SuiteItemPdcBridge.hasSlimefunId(boots, ObsidianItems.OBSIDIAN_BOOTS);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (blastAbsorptionPlate && (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                                     event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            if (hasFullObsidianSuit(player)) {
                // Mitigate 60% of explosion damage when wearing full Obsidian Armor
                event.setDamage(event.getDamage() * 0.40);
            }
        }
    }
}
