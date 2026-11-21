package com.drakescraft.suites.combat.warfare;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Módulo SlimefunWarfare para DrakesCombat.
 * Gestiona balística, armas de energía, exoesqueletos modulares y bombas de choque.
 */
@Getter
public class WarfareModule extends AbstractSuiteModule {

    private final WarfareRegistry registry = new WarfareRegistry();
    private WarfareListener listener;
    private boolean atomicAmmoConsumption = true;

    public WarfareModule(JavaPlugin plugin) {
        super(plugin, "warfare", "SlimefunWarfare Ballistics & Exoskeletons");
    }

    static boolean isStandaloneSlimefunWarfarePresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("SlimefunWarfare") != null;
    }

    @Override
    public void onEnable() {
        this.atomicAmmoConsumption = config.getBoolean("anti-dupe.ammo-consumption-atomic", true);
        boolean asyncRaytrace = config.getBoolean("performance.bullet-raytrace-async", true);

        if (isStandaloneSlimefunWarfarePresent()) {
            getPlugin().getLogger().info("[DrakesCombat] Plugin standalone SlimefunWarfare detectado; cediendo listener al addon dedicado.");
            return;
        }

        this.listener = new WarfareListener(this, this.registry);
        Bukkit.getPluginManager().registerEvents(this.listener, getPlugin());

        getPlugin().getLogger().info("[DrakesCombat] WarfareModule habilitado (Atomic ammo: "
                + atomicAmmoConsumption + ", Async raytrace: " + asyncRaytrace + ").");
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            HandlerList.unregisterAll(this.listener);
            this.listener = null;
        }
        getPlugin().getLogger().info("[DrakesCombat] WarfareModule deshabilitado limpiamente.");
    }

    public WarfareRegistry getRegistry() {
        return registry;
    }
}
