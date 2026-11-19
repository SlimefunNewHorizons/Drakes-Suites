package com.drakescraft.suites.bio;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.GenericSuiteModule;
import com.drakescraft.suites.core.module.SuiteModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DrakesBio - Mega-Suite de Biotecnología, Genética (Tiers 0 a 9), Botánica y Apicultura Slime.
 */
public class DrakesBioPlugin extends JavaPlugin {

    private static DrakesBioPlugin instance;
    private SuiteModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        }

        this.moduleManager = new SuiteModuleManager(this);
        // Registro de módulos biológicos absorbidos
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.geneticchickens.GeneticChickensModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.geneticwoolies.GeneticWooliesModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.exoticgarden.ExoticGardenModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.cultivation.CultivationModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.slimybees.SlimyBeesModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.mobcapturer.MobCapturerModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.treetaps.TreeTapsModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "gastronomicon", "Gastronomicon Advanced Culinary"));
        this.moduleManager.registerModule(new com.drakescraft.suites.bio.flowerpower.FlowerPowerModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "drugfun", "Drugfun Phytochemical & Botanical Synthesis"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "globalwarming", "GlobalWarming Eco-Thermodynamics & Carbon Capture"));

        this.moduleManager.enableAll();

        // Registrar en DrakesCore
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().registerSuite("bio", this, this.moduleManager);
        }

        getLogger().info("DrakesBio v" + getPluginMeta().getVersion() + " inicializado con éxito.");
    }

    @Override
    public void onDisable() {
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().unregisterSuite("bio");
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        getLogger().info("DrakesBio deshabilitado limpiamente.");
        instance = null;
    }

    public static DrakesBioPlugin getInstance() {
        return instance;
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }
}
