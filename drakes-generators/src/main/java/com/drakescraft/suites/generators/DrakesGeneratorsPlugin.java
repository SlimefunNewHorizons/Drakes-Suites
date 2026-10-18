package com.drakescraft.suites.generators;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.GenericSuiteModule;
import com.drakescraft.suites.core.module.SuiteModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DrakesGenerators - Mega-Suite de Matriz Energética, Reactores Nucleares, Generación Solar y Síntesis Mineral.
 */
public class DrakesGeneratorsPlugin extends JavaPlugin {

    private static DrakesGeneratorsPlugin instance;
    private SuiteModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        }

        this.moduleManager = new SuiteModuleManager(this);
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.litexpansion.LiteXpansionModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.smg.SMGModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.ultimategenerators.UltimateGeneratorsModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.ecopower.EcoPowerModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.orechunks.OreChunksModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.betterreactors.BetterReactorsModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.generators.liquid.LiquidModule(this));

        this.moduleManager.enableAll();

        // Registrar en DrakesCore
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().registerSuite("generators", this, this.moduleManager);
        }

        getLogger().info("DrakesGenerators v" + getPluginMeta().getVersion() + " inicializado con éxito.");
    }

    @Override
    public void onDisable() {
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().unregisterSuite("generators");
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        getLogger().info("DrakesGenerators deshabilitado limpiamente.");
        instance = null;
    }

    public static DrakesGeneratorsPlugin getInstance() {
        return instance;
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }
}
