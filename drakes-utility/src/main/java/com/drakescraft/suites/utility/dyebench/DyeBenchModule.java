package com.drakescraft.suites.utility.dyebench;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.utility.dyebench.setup.DyeBenchItemSetup;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Módulo integrado de DyeBench en DrakesUtility.
 */
public class DyeBenchModule extends AbstractSuiteModule implements SlimefunAddon {

    public DyeBenchModule(@Nonnull JavaPlugin plugin) {
        super(plugin, "dyebench", "DyeBench Automated Dye & Pigment Crafting");
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager() != null && Bukkit.getPluginManager().getPlugin("DyeBench") != null) {
            logInfo("Plugin standalone DyeBench detectado; cediendo registro al addon dedicado.");
            return;
        }
        if (Bukkit.getPluginManager() == null || Bukkit.getPluginManager().getPlugin("Slimefun") == null) {
            logInfo("Slimefun no detectado; omitiendo registro de ítems de DyeBench.");
            return;
        }
        DyeBenchItemSetup.setup(this);
        logInfo("Módulo DyeBench habilitado e ítems registrados en Slimefun.");
    }

    @Override
    public void onDisable() {
        // Los ítems de Slimefun se limpian al apagar el servidor.
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return getPlugin();
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunNewHorizons/Drakes-Suites/issues";
    }
}
