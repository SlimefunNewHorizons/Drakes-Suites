package com.drakescraft.suites.bio.exoticgarden;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Módulo ExoticGarden para DrakesBio.
 * Administra la botánica avanzada, cultivos, arbustos de bayas, árboles frutales y herramientas culinarias.
 */
@Getter
public class ExoticGardenModule extends AbstractSuiteModule {

    private final ExoticGardenRegistry registry = new ExoticGardenRegistry();
    private ExoticGardenListener listener;
    private boolean bonemealEnabled = true;

    public ExoticGardenModule(JavaPlugin plugin) {
        super(plugin, "exotic_garden", "Exotic Garden & Culinary Arts");
    }

    static boolean isStandaloneExoticGardenPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("ExoticGarden") != null;
    }

    @Override
    public void onEnable() {
        this.bonemealEnabled = config.getBoolean("bonemeal-growth", true);
        int crookDropChance = config.getInt("crook-leaf-drop-chance", 25);
        boolean preventUnnatural = config.getBoolean("anti-dupe.prevent-unnatural-leaf-drops", true);

        if (isStandaloneExoticGardenPresent()) {
            getPlugin().getLogger().info("[DrakesBio] Plugin standalone ExoticGarden detectado; cediendo listener al addon dedicado.");
            return;
        }

        this.listener = new ExoticGardenListener(this, this.registry, crookDropChance, preventUnnatural);
        Bukkit.getPluginManager().registerEvents(this.listener, getPlugin());

        getPlugin().getLogger().info("[DrakesBio] ExoticGardenModule habilitado (Bonemeal: "
                + bonemealEnabled + ", Crook chance: " + crookDropChance + "%).");
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            HandlerList.unregisterAll(this.listener);
            this.listener = null;
        }
        getPlugin().getLogger().info("[DrakesBio] ExoticGardenModule deshabilitado limpiamente.");
    }

    public ExoticGardenRegistry getRegistry() {
        return registry;
    }
}
