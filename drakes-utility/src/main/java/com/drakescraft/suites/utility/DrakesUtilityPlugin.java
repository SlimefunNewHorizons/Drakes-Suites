package com.drakescraft.suites.utility;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.GenericSuiteModule;
import com.drakescraft.suites.core.module.SuiteModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DrakesUtility - Mega-Suite de Almacenamiento, Mochilas Tintadas, Terminales Inalámbricas y QoL.
 */
public class DrakesUtilityPlugin extends JavaPlugin {

    private static DrakesUtilityPlugin instance;
    private SuiteModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        }

        this.moduleManager = new SuiteModuleManager(this);
        // Registro de módulos de utilidad y almacenamiento absorbidos
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.backpacks.BackpacksModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.enderchests.ColoredEnderChestsModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.chestterminal.ChestTerminalModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.sfcalc.SFCalcModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.slimehud.SlimeHUDModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.soundmuffler.SoundMufflerModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.simpleutils.SimpleUtilsModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.trash.TrashModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "slimeframe", "SlimeFrame Item Displays"));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.extratools.ExtraToolsModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "extrautils", "ExtraUtils Compression & QoL Blocks"));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.portalgun.PortalGunModule(this));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "smallspace", "SmallSpace Compact Dimensional Pocket Capsules"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "guidetools", "JustEnoughGuide & SlimefunAdvancements Visuals"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "worldedit_sf", "WorldEditSlimefun Schematic & Paste Engine"));
        this.moduleManager.registerModule(new GenericSuiteModule(this, "geyser_heads", "GeyserHeads Bedrock Texture Mapping"));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.endercarryon.EnderCarryOnModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.vanillapatpat.VanillaPatPatModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.notenoughaddons.NotEnoughAddonsModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.compressioncraft.CompressionCraftModule(this));
        this.moduleManager.registerModule(new com.drakescraft.suites.utility.dyebench.DyeBenchModule(this));

        this.moduleManager.enableAll();

        // Registrar en DrakesCore
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().registerSuite("utility", this, this.moduleManager);
        }

        getLogger().info("DrakesUtility v" + getPluginMeta().getVersion() + " inicializado con éxito.");
    }

    @Override
    public void onDisable() {
        if (DrakesCorePlugin.getInstance() != null && DrakesCorePlugin.getInstance().getSuiteRegistry() != null) {
            DrakesCorePlugin.getInstance().getSuiteRegistry().unregisterSuite("utility");
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        getLogger().info("DrakesUtility deshabilitado limpiamente.");
        instance = null;
    }

    public static DrakesUtilityPlugin getInstance() {
        return instance;
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }
}
