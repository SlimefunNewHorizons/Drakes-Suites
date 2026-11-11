package com.drakescraft.suites.server;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.GenericSuiteModule;
import com.drakescraft.suites.core.module.SuiteModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DrakesServer - Mega-Suite de Servidor, Motor Odysseia (Rust/Java), Aislamiento de Inventarios y Persistencia.
 */
public class DrakesServerPlugin extends JavaPlugin {

    private static DrakesServerPlugin instance;
    private SuiteModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        }

        this.moduleManager = new SuiteModuleManager(this);
        // Registro de módulos de servidor absorbidos (Star/Odysseia, InvSwitcher, PlayerVaultZ, AxGraves, BreweryX, Market)
        this.moduleManager.registerModule(new GenericSuiteModule(this, "star", "Star Server Engine (Odysseia Evolution), Native Bridge & Tebex"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "odysseia", "Odysseia Rust Native Engine & Mythics (Legacy Alias)"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "invswitcher", "InvSwitcher 5-Modality Airtight Isolation"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "playervaultz", "PlayerVaultZ Anti-Dupe Vaults"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "axgraves", "AxGraves Zero-Item-Loss Graves"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "breweryx", "BreweryX Custom Beverage Engine"));
        this.moduleManager.registerModule(new com.drakescraft.suites.server.economy.DrakesEconomyModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "display", "DrakesDisplayEngine Native Embedded TAB & Scoreboard"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "brewery_menu", "BreweryMenu Interactive Beverage & Recipe UI"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "random_expansion", "RandomExpansion Dynamic Server Events & Procedural Loot"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "bump", "Bump System & Network Loyalty Telemetry"));
        this.moduleManager.registerModule(new com.drakescraft.suites.server.cosmetics.PhantomArmorModule(this));

        this.moduleManager.enableAll();

        // Registrar en DrakesCore
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().registerSuite("server", this, this.moduleManager);
        }

        getLogger().info("DrakesServer v" + getPluginMeta().getVersion() + " inicializado con éxito.");
    }

    @Override
    public void onDisable() {
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().unregisterSuite("server");
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        getLogger().info("DrakesServer deshabilitado limpiamente.");
        instance = null;
    }

    public static DrakesServerPlugin getInstance() {
        return instance;
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }
}
