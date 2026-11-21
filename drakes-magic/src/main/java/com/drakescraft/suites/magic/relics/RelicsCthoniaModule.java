package com.drakescraft.suites.magic.relics;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Módulo de Relics of Cthonia para DrakesMagic.
 * Integra reliquias arcanas del inframundo divididas en 5 niveles de rareza,
 * obtenibles mediante minería profunda y combate, con soporte de anulación (Relic Voider).
 */
public class RelicsCthoniaModule extends AbstractSuiteModule {

    private final RelicsRegistry registry = new RelicsRegistry();
    private RelicsDropListener dropListener;
    private RelicsInteractListener interactListener;

    private double miningDropChance = 0.005; // 0.5%
    private double mobDropChance = 0.025;    // 2.5%

    public RelicsCthoniaModule(JavaPlugin plugin) {
        super(plugin, "relics_cthonia", "Relics of Cthonia Underworld");
    }

    static boolean isStandaloneRelicsOfCthoniaPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("RelicsOfCthonia") != null;
    }

    @Override
    public void onEnable() {
        if (config != null) {
            this.miningDropChance = config.getDouble("mining-drop-chance", 0.005);
            this.mobDropChance = config.getDouble("mob-drop-chance", 0.025);
        }

        if (isStandaloneRelicsOfCthoniaPresent()) {
            getPlugin().getLogger().info("[DrakesMagic] Plugin standalone RelicsOfCthonia detectado; cediendo listeners al addon dedicado.");
            return;
        }

        this.dropListener = new RelicsDropListener(this, registry);
        this.interactListener = new RelicsInteractListener(registry);

        Bukkit.getPluginManager().registerEvents(this.dropListener, getPlugin());
        Bukkit.getPluginManager().registerEvents(this.interactListener, getPlugin());

        getPlugin().getLogger().info("[DrakesMagic] RelicsCthoniaModule habilitado con "
                + registry.getAllRelics().size() + " reliquias registradas.");
    }

    @Override
    public void onDisable() {
        if (this.dropListener != null) {
            HandlerList.unregisterAll(this.dropListener);
            this.dropListener = null;
        }
        if (this.interactListener != null) {
            HandlerList.unregisterAll(this.interactListener);
            this.interactListener = null;
        }
        getPlugin().getLogger().info("[DrakesMagic] RelicsCthoniaModule deshabilitado limpiamente.");
    }

    public RelicsRegistry getRegistry() {
        return registry;
    }

    public double getMiningDropChance() {
        return miningDropChance;
    }

    public void setMiningDropChance(double chance) {
        this.miningDropChance = chance;
    }

    public double getMobDropChance() {
        return mobDropChance;
    }

    public void setMobDropChance(double chance) {
        this.mobDropChance = chance;
    }
}
