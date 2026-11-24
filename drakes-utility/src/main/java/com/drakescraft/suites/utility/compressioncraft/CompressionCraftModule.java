package com.drakescraft.suites.utility.compressioncraft;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.utility.compressioncraft.setup.CompressionCraftItemSetup;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Módulo integrado de CompressionCraft en DrakesUtility.
 */
public class CompressionCraftModule extends AbstractSuiteModule implements SlimefunAddon {

    public CompressionCraftModule(@Nonnull JavaPlugin plugin) {
        super(plugin, "compressioncraft", "CompressionCraft Block Compression & Decompression");
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager() != null && Bukkit.getPluginManager().getPlugin("CompressionCraft") != null) {
            logInfo("Plugin standalone CompressionCraft detectado; cediendo registro al addon dedicado.");
            return;
        }
        if (Bukkit.getPluginManager() == null || Bukkit.getPluginManager().getPlugin("Slimefun") == null) {
            logInfo("Slimefun no detectado; omitiendo registro de ítems de CompressionCraft.");
            return;
        }
        CompressionCraftItemSetup.setup(this);
        logInfo("Módulo CompressionCraft habilitado e ítems registrados en Slimefun.");
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
