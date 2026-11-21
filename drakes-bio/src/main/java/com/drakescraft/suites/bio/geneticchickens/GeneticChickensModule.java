package com.drakescraft.suites.bio.geneticchickens;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

/**
 * Módulo nativo GeneticChickengineering para DrakesBio.
 * Administra el genoma cromosómico de 6 loci, cruces genéticos,
 * tasas de mutación y persistencia de 64 especies de pollos de recursos (T0-T9).
 */
public class GeneticChickensModule extends AbstractSuiteModule implements Listener {

    private int maxTier = 9;
    private int incubationTimeSeconds = 20;
    private boolean allowSplicing = true;
    private double mutationChance = 0.30;
    private boolean antiCrossWorldDrop = true;

    public GeneticChickensModule(JavaPlugin plugin) {
        super(plugin, "genetic_chickens", "GeneticChickengineering Tiers 0-9");
    }

    static boolean isStandaloneGeneticChickensPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("GeneticChickengineering") != null;
    }

    @Override
    public void onEnable() {
        this.maxTier = config.getInt("max-tier", 9);
        this.incubationTimeSeconds = config.getInt("incubation-time-seconds", 20);
        this.allowSplicing = config.getBoolean("allow-genetic-splicing", true);
        this.mutationChance = config.getDouble("mutation-chance", 0.30);
        this.antiCrossWorldDrop = config.getBoolean("anti-cross-world-drop", true);

        ChickenEggItem.initializeKeys(plugin);
        if (isStandaloneGeneticChickensPresent()) {
            logInfo("Plugin standalone GeneticChickengineering detectado; cediendo listener al addon dedicado.");
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);

        logInfo("GeneticChickengineering habilitado con éxito. Especies registradas: "
                + ChickenSpeciesRegistry.count()
                + " | Tasa de mutación: " + String.format("%.1f", mutationChance * 100) + "%"
                + " | Tiempo de incubación: " + incubationTimeSeconds + "s.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        logInfo("GeneticChickengineering deshabilitado limpiamente.");
    }

    /**
     * Ejecuta el cruce biológico entre dos secuencias de ADN de pollos parentales.
     */
    @Nonnull
    public ChickenDNA breed(@Nonnull ChickenDNA parentA, @Nonnull ChickenDNA parentB) {
        return parentA.breed(parentB, this.mutationChance, 2);
    }

    /**
     * Genera un huevo genético listo para incubar.
     */
    @Nonnull
    public ItemStack createEgg(@Nonnull ChickenDNA dna) {
        return ChickenEggItem.createEgg(dna, plugin);
    }

    /**
     * Genera un huevo genético a partir de su tipo numérico (0 a 63).
     */
    @Nonnull
    public ItemStack createEgg(int typing) {
        return createEgg(new ChickenDNA(typing));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEggThrow(PlayerEggThrowEvent event) {
        ItemStack eggItem = event.getEgg().getItem();
        ChickenDNA dna = ChickenEggItem.extractDna(eggItem);
        if (dna != null) {
            // Evitar que el huevo de Slimefun genere pollos vanilla normales al arrojarse a mano
            event.setHatching(false);
            event.getPlayer().sendMessage("§e[GeneticChickens] §7Este huevo fértil requiere una incubadora eléctrica para eclosionar.");
        }
    }

    public int getMaxTier() {
        return maxTier;
    }

    public int getIncubationTimeSeconds() {
        return incubationTimeSeconds;
    }

    public boolean isAllowSplicing() {
        return allowSplicing;
    }

    public double getMutationChance() {
        return mutationChance;
    }

    public boolean isAntiCrossWorldDrop() {
        return antiCrossWorldDrop;
    }
}
