package com.drakescraft.suites.core.ticker;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Motor de ticking centralizado para todas las Mega-Suites de DrakesCraft.
 * Agrupa tareas periódicas para evitar la dispersión de 70+ runTaskTimer desacoplados
 * y comprueba chunks cargados antes de ejecutar lógica pesada.
 */
public class SuiteTickerEngine {

    private final JavaPlugin plugin;
    private final Map<String, Consumer<Long>> tickListeners = new ConcurrentHashMap<>();
    private final Map<Location, BlockTickerHolder> blockTickers = new ConcurrentHashMap<>();
    private com.drakescraft.suites.core.world.SlimefunWorldFilter worldFilter;
    private BukkitTask mainTask;
    private long currentTick = 0;

    public SuiteTickerEngine(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setWorldFilter(com.drakescraft.suites.core.world.SlimefunWorldFilter worldFilter) {
        this.worldFilter = worldFilter;
    }

    public synchronized void start() {
        if (mainTask != null && !mainTask.isCancelled()) {
            return;
        }

        // Ticker global sincronizado cada tick de servidor (o cada N ticks)
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        plugin.getLogger().info("[SuiteTickerEngine] Motor de Ticker Centralizado activo.");
    }

    public synchronized void stop() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        tickListeners.clear();
        blockTickers.clear();
        plugin.getLogger().info("[SuiteTickerEngine] Motor de Ticker detenido limpiamente.");
    }

    public void registerTickListener(String key, Consumer<Long> listener) {
        tickListeners.put(key, listener);
    }

    public void unregisterTickListener(String key) {
        tickListeners.remove(key);
    }

    public void registerBlockTicker(Location location, Consumer<Location> ticker) {
        registerBlockTicker(location, ticker, 1);
    }

    /**
     * EN: Registers a block ticker that runs at most once every {@code intervalTicks} server ticks.
     * The first execution happens on the next tick after registration. Lets passive machines
     * (idle backoff) avoid the per-tick O(n) scan over every registered block.
     *
     * ES: Registra un ticker de bloque que corre como máximo una vez cada {@code intervalTicks}
     * ticks de servidor. La primera ejecución ocurre en el tick siguiente al registro. Permite
     * que las máquinas pasivas apliquen idle backoff y evita el escaneo O(n) en cada tick.
     */
    public void registerBlockTicker(Location location, Consumer<Location> ticker, int intervalTicks) {
        blockTickers.put(location, new BlockTickerHolder(ticker, intervalTicks, currentTick));
    }

    public void unregisterBlockTicker(Location location) {
        blockTickers.remove(location);
    }

    private void tick() {
        currentTick++;

        // Ejecutar listeners globales registrados (sin iterador cuando no hay ninguno)
        if (!tickListeners.isEmpty()) {
            for (Map.Entry<String, Consumer<Long>> entry : tickListeners.entrySet()) {
                try {
                    entry.getValue().accept(currentTick);
                } catch (Throwable t) {
                    plugin.getLogger().log(Level.SEVERE, "Error en tick listener de suite: " + entry.getKey(), t);
                }
            }
        }

        // Ejecutar tickers de bloques solo si su chunk está cargado
        if (!blockTickers.isEmpty()) {
            for (Map.Entry<Location, BlockTickerHolder> entry : blockTickers.entrySet()) {
                BlockTickerHolder holder = entry.getValue();
                if (holder == null || currentTick < holder.nextRun) {
                    continue; // Intervalo escalonado: esta máquina pasiva descansa este tick
                }
                holder.nextRun = currentTick + holder.interval;
                Location loc = entry.getKey();
                World world = loc == null ? null : loc.getWorld();
                if (world == null) {
                    continue;
                }
                if (worldFilter != null && !worldFilter.isSlimefunAllowed(world)) {
                    continue; // Skip bloques de Slimefun en mundos deshabilitados (ej. Survival Clásico)
                }
                int chunkX = loc.getBlockX() >> 4;
                int chunkZ = loc.getBlockZ() >> 4;
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue; // Skip chunk inactivo sin despertar entidades o desincronizar el servidor
                }
                try {
                    holder.ticker.accept(loc);
                } catch (Throwable t) {
                    plugin.getLogger().log(Level.SEVERE, "Error al procesar block ticker en " + loc, t);
                }
            }
        }
    }

    /**
     * EN: Block ticker plus its stagger interval. Keeps the per-tick loop O(registered) with
     * cheap skips instead of running every machine body on every tick.
     *
     * ES: Ticker de bloque más su intervalo de escalonado. Mantiene el bucle por tick en
     * O(registrados) con saltos baratos en vez de ejecutar cada máquina en cada tick.
     */
    private static final class BlockTickerHolder {
        final Consumer<Location> ticker;
        final int interval;
        long nextRun;

        BlockTickerHolder(Consumer<Location> ticker, int intervalTicks, long now) {
            this.ticker = ticker;
            this.interval = Math.max(1, intervalTicks);
            this.nextRun = now;
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }
}
