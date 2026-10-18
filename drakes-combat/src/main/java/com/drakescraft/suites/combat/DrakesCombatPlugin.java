package com.drakescraft.suites.combat;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.GenericSuiteModule;
import com.drakescraft.suites.core.module.SuiteModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DrakesCombat - Mega-Suite de Arsenal, SlimeTinker, Armamento Bélico y Exoesqueletos de Combate.
 */
public class DrakesCombatPlugin extends JavaPlugin {

    private static DrakesCombatPlugin instance;
    private SuiteModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        }

        this.moduleManager = new SuiteModuleManager(this);
        // Registro de módulos bélicos y de combate absorbidos
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.tinker.SlimeTinkerModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.warfare.WarfareModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.extragear.ExtraGearModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "luckyblocks", "LuckyBlocks SF Mechanics"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "galaxyfun", "Galaxyfun Exo-Space Combat"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "missilewarfare", "MissileWarfare Ballistics"));
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.mobdrops.MobDropsModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.slimefundisc.SlimefunDiscModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "fn_amplifications", "FNAmplifications Ballistics & Tactical Scopes"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "military_arsenal", "MilitaryArsenal Heavy Weaponry & Turrets"));
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.nukes.SlimefunNukesModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.combat.obsidian.ObsidianExpansionModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "hardcore_combat", "HardcoreSlimefun Lethality & Tactical Survival"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "cringle_bosses", "CringleBosses Seasonal & Nordic Mini-Encounters"));

        this.moduleManager.enableAll();

        // Registrar en DrakesCore
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().registerSuite("combat", this, this.moduleManager);
        }

        getLogger().info("DrakesCombat v" + getPluginMeta().getVersion() + " inicializado con éxito.");
    }

    @Override
    public void onDisable() {
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().unregisterSuite("combat");
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        getLogger().info("DrakesCombat deshabilitado limpiamente.");
        instance = null;
    }

    public static DrakesCombatPlugin getInstance() {
        return instance;
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }
}
