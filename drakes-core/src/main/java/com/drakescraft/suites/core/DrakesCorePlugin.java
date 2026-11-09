package com.drakescraft.suites.core;

import com.drakescraft.suites.core.command.DrakesSuitesCommand;
import com.drakescraft.suites.core.module.SuiteModuleManager;
import com.drakescraft.suites.core.registry.SuiteRegistry;
import com.drakescraft.suites.core.ticker.SuiteTickerEngine;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DrakesCore - Kernel del Ecosistema DrakesCraft: Core Engine, API, Modular Loader y Ticker Centralizado.
 */
public class DrakesCorePlugin extends JavaPlugin {

    private static DrakesCorePlugin instance;
    private SuiteTickerEngine tickerEngine;
    private SuiteModuleManager moduleManager;
    private SuiteRegistry suiteRegistry;
    private com.drakescraft.suites.core.database.SuiteDatabaseEngine databaseEngine;
    private com.drakescraft.suites.core.logging.SuiteAuditLogger auditLogger;
    private com.drakescraft.suites.core.world.SlimefunWorldFilter worldFilter;
    private com.drakescraft.suites.core.command.CommandModalityGate commandGate;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        registerEconomyRouter();

        // 1. Inicializar base de datos centralizada SQLite WAL
        this.databaseEngine = new com.drakescraft.suites.core.database.SuiteDatabaseEngine(this);
        this.databaseEngine.start();

        // 2. Inicializar sistema de logs aislados por modulo y auditoria forense
        this.auditLogger = new com.drakescraft.suites.core.logging.SuiteAuditLogger(this);

        // 3. Inicializar filtro de restricción de mundos para Slimefun (ej. Survival Clásico)
        this.worldFilter = new com.drakescraft.suites.core.world.SlimefunWorldFilter();
        this.worldFilter.reload(getConfig());
        getServer().getPluginManager().registerEvents(new com.drakescraft.suites.core.world.SlimefunWorldRestrictionListener(this.worldFilter), this);

        // 4. Inicializar Gateway de filtrado de comandos por modalidad
        this.commandGate = new com.drakescraft.suites.core.command.CommandModalityGate();
        this.commandGate.reload(getConfig());
        getServer().getPluginManager().registerEvents(this.commandGate, this);

        this.suiteRegistry = new SuiteRegistry();

        this.tickerEngine = new SuiteTickerEngine(this);
        this.tickerEngine.setWorldFilter(this.worldFilter);
        this.tickerEngine.start();

        this.moduleManager = new SuiteModuleManager(this);
        this.moduleManager.enableAll();

        // Inicializar aceleracion nativa Rust (Slimefun-Rust y Odysseia-Rust)
        com.drakescraft.suites.core.nativeengine.NativeEngineBridge.initialize(getDataFolder().toPath());

        // Registrar Suite 0 en el registro global
        this.suiteRegistry.registerSuite("core", this, this.moduleManager);

        // Registrar comando maestro
        PluginCommand cmd = getCommand("drakessuites");
        if (cmd != null) {
            DrakesSuitesCommand executor = new DrakesSuitesCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Diagnostico de plataforma (Purpur 26.2 vs Paper)
        com.drakescraft.suites.core.runtime.PurpurRuntimeProvider.logRuntimeDiagnostics(getLogger());

        getLogger().info("DrakesCore v" + getPluginMeta().getVersion() + " (Kernel, DB WAL, Audit Logs, WorldFilter & CommandGate) inicializado con exito.");
    }

    @Override
    public void onDisable() {
        if (suiteRegistry != null) {
            suiteRegistry.unregisterSuite("core");
        }
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        if (tickerEngine != null) {
            tickerEngine.stop();
        }
        if (auditLogger != null) {
            auditLogger.close();
        }
        if (databaseEngine != null) {
            databaseEngine.stop();
        }
        getLogger().info("DrakesCore deshabilitado limpiamente.");
        instance = null;
    }

    public static DrakesCorePlugin getInstance() {
        return instance;
    }

    public SuiteTickerEngine getTickerEngine() {
        return tickerEngine;
    }

    public SuiteModuleManager getModuleManager() {
        return moduleManager;
    }

    public SuiteRegistry getSuiteRegistry() {
        return suiteRegistry;
    }

    public com.drakescraft.suites.core.database.SuiteDatabaseEngine getDatabaseEngine() {
        return databaseEngine;
    }

    public com.drakescraft.suites.core.logging.SuiteAuditLogger getAuditLogger() {
        return auditLogger;
    }

    public com.drakescraft.suites.core.world.SlimefunWorldFilter getWorldFilter() {
        return worldFilter;
    }

    public com.drakescraft.suites.core.command.CommandModalityGate getCommandGate() {
        return commandGate;
    }

    /** Router Vault: ver {@link com.drakescraft.suites.core.economy.EconomyRouter}. */
    private void registerEconomyRouter() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class,
                com.drakescraft.suites.core.economy.EconomyRouter.create(
                        com.drakescraft.suites.core.economy.EconomyRouter.bukkitResolver(), getLogger()),
                this, org.bukkit.plugin.ServicePriority.Highest);
        getLogger().info("[Economy] Router Vault registrado (InvSwitcher > Essentials).");
    }
}
