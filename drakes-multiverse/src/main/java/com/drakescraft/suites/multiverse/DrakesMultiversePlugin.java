package com.drakescraft.suites.multiverse;

import com.chagui68.multiversenets.MultiverseNets;
import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.GenericSuiteModule;
import com.drakescraft.suites.core.module.SuiteModuleManager;

/**
 * DrakesMultiverse - Suite Oficial del Multiverso concebida y creada por Chagui68.
 * Absorbe y consolida de forma modular:
 * - MultiverseCreatures (criaturas del multiverso, bosses, rituales y dimensiones)
 * - MultiverseNets (redes digitales standalone de almacenamiento y transporte sin Slimefun)
 *
 * Autor Canónico: Chagui68
 */
public class DrakesMultiversePlugin extends MultiverseNets {

    private SuiteModuleManager moduleManager;
    private boolean embeddedNetsActive;

    static boolean isStandaloneMultiverseNetsPresent() {
        return org.bukkit.Bukkit.getPluginManager() != null
                && org.bukkit.Bukkit.getPluginManager().getPlugin("MultiverseNets") != null;
    }

    @Override
    public void onEnable() {
        // Solo inicializar el motor embebido de MultiverseNets si el plugin standalone NO está instalado
        if (isStandaloneMultiverseNetsPresent()) {
            this.embeddedNetsActive = false;
            getLogger().info("[DrakesMultiverse] Plugin standalone MultiverseNets detectado; cediendo listeners, ticker y almacenamiento al plugin dedicado.");
        } else {
            super.onEnable();
            this.embeddedNetsActive = true;
        }

        this.moduleManager = new SuiteModuleManager(this);

        // Módulos soberanos de la Suite de Chagui
        this.moduleManager.registerModule(new GenericSuiteModule(this, "creatures", "Multiverse Creatures & Bosses (por Chagui68)"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "nets", "Multiverse Standalone Digital Networks (por Chagui68)"));

        this.moduleManager.enableAll();

        DrakesCorePlugin core = DrakesCorePlugin.getInstance();
        if (core != null && core.getSuiteRegistry() != null) {
            core.getSuiteRegistry().registerSuite("multiverse", this, this.moduleManager);
        }

        getLogger().info("§5[DrakesMultiverse] §dSuite Multiverse de Chagui68 v" + getPluginMeta().getVersion() + " inicializada con exito.");
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        DrakesCorePlugin core = DrakesCorePlugin.getInstance();
        if (core != null && core.getSuiteRegistry() != null) {
            core.getSuiteRegistry().unregisterSuite("multiverse");
        }
        if (embeddedNetsActive) {
            super.onDisable();
            embeddedNetsActive = false;
        }
        getLogger().info("[DrakesMultiverse] Suite de Chagui68 deshabilitada limpiamente.");
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }
}
