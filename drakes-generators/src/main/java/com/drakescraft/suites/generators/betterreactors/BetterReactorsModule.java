package com.drakescraft.suites.generators.betterreactors;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BetterReactors Module for DrakesGenerators.
 * Provides high-efficiency nuclear fission reactors, cryocooling systems,
 * and automated scram safety protocols.
 */
public class BetterReactorsModule extends AbstractSuiteModule implements Listener {

    private final Map<Location, ReactorCoreData> activeReactors = new ConcurrentHashMap<>();

    private int threadPoolSize = 2;
    private boolean fuelRodLock = true;
    private boolean cryoCoolantLoops = true;
    private boolean uraniumEnrichment = true;
    private boolean meltdownContainment = true;

    public BetterReactorsModule(JavaPlugin plugin) {
        super(plugin, "better_reactors", "BetterNuclearReactor Fission & Cryocooling");
    }

    public static class ReactorCoreData {
        private boolean online;
        private int currentTemperature;
        private int storedEnergy;
        private int coolantLevel;

        public ReactorCoreData(boolean online) {
            this.online = online;
            this.currentTemperature = 20; // Ambient 20C
            this.storedEnergy = 0;
            this.coolantLevel = 100;
        }

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
        public int getCurrentTemperature() { return currentTemperature; }
        public void setCurrentTemperature(int currentTemperature) { this.currentTemperature = currentTemperature; }
        public int getStoredEnergy() { return storedEnergy; }
        public void setStoredEnergy(int storedEnergy) { this.storedEnergy = storedEnergy; }
        public int getCoolantLevel() { return coolantLevel; }
        public void setCoolantLevel(int coolantLevel) { this.coolantLevel = coolantLevel; }
    }

    static boolean isStandaloneBetterReactorPresent() {
        var pm = org.bukkit.Bukkit.getPluginManager();
        return pm != null && (pm.getPlugin("BetterReactor") != null || pm.getPlugin("BetterNuclearReactor") != null);
    }

    @Override
    public void onEnable() {
        this.threadPoolSize = config.getInt("performance.reactor-core-thread-pool", 2);
        this.fuelRodLock = config.getBoolean("anti-dupe.fuel-rod-consumption-lock", true);
        this.cryoCoolantLoops = config.getBoolean("features.cryo-coolant-loops", true);
        this.uraniumEnrichment = config.getBoolean("features.uranium-enrichment", true);
        this.meltdownContainment = config.getBoolean("features.meltdown-containment-protocol", true);

        if (isStandaloneBetterReactorPresent()) {
            logInfo("Plugin standalone BetterReactor detectado; cediendo listener al addon dedicado.");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        logInfo("BetterReactors loaded (threads=" + threadPoolSize + ", cryoLoops=" + cryoCoolantLoops + ", antiDupeLock=" + fuelRodLock + ").");
    }

    @Override
    public void onDisable() {
        activeReactors.clear();
        logInfo("BetterReactors disabled cleanly.");
    }

    public Map<Location, ReactorCoreData> getActiveReactors() {
        return activeReactors;
    }

    public boolean isFuelRodLock() {
        return fuelRodLock;
    }

    public boolean isCryoCoolantLoops() {
        return cryoCoolantLoops;
    }

    public boolean isUraniumEnrichment() {
        return uraniumEnrichment;
    }

    public boolean isMeltdownContainment() {
        return meltdownContainment;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (com.drakescraft.suites.core.pdc.SuiteItemPdcBridge.hasSlimefunId(item, BetterReactorsItems.REACTOR_CORE)) {
            Location loc = event.getBlock().getLocation();
            activeReactors.put(loc, new ReactorCoreData(false));
            logInfo("Reactor core placed at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (activeReactors.remove(loc) != null) {
            logInfo("Reactor core dismantled at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
    }
}
