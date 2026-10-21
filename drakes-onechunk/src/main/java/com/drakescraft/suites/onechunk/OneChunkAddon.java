package com.drakescraft.suites.onechunk;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class OneChunkAddon extends Addon implements Listener {
    private final Set<ColumnKey> generating = new HashSet<>();
    private boolean enabled;
    private String worldName;
    private int minY;
    private int maxY;
    private int blocksPerTick;
    private ColumnProfile profile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        if (!enabled) {
            log("Disabled by configuration; no worlds or islands will be modified.");
            return;
        }
        if (getAddonByName("AOneBlock").isEmpty()) {
            logError("AOneBlock is required; OneChunk remains inactive.");
            enabled = false;
            return;
        }
        registerListener(this);
        log("Enabled for new or reset islands in world " + worldName + ".");
    }

    @Override
    public void onDisable() {
        generating.clear();
    }

    private void reloadSettings() {
        FileConfiguration config = getConfig();
        enabled = config.getBoolean("enabled", false);
        worldName = config.getString("world", "onechunk_world");
        minY = config.getInt("min-y", -64);
        maxY = config.getInt("max-y", 319);
        blocksPerTick = Math.max(1, config.getInt("blocks-per-tick", 512));
        profile = new ColumnProfile(config.getStringList("progression"));
        if (minY > maxY) {
            throw new IllegalStateException("min-y must not exceed max-y");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandCreatedEvent event) {
        enqueueColumn(event.getLocation().getWorld(), event.getLocation().getChunk().getX(), event.getLocation().getChunk().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandReset(IslandResettedEvent event) {
        enqueueColumn(event.getLocation().getWorld(), event.getLocation().getChunk().getX(), event.getLocation().getChunk().getZ());
    }

    private void enqueueColumn(World world, int chunkX, int chunkZ) {
        if (!enabled || world == null || !world.getName().equals(worldName)) {
            return;
        }
        ColumnKey key = ColumnKey.of(world, chunkX, chunkZ);
        if (!generating.add(key)) {
            return;
        }
        new BukkitRunnable() {
            private int index;
            private final int total = profile.columnBlockCount(minY, maxY);

            @Override
            public void run() {
                for (int processed = 0; processed < blocksPerTick && index < total; processed++, index++) {
                    int y = minY + index / 256;
                    int horizontal = index % 256;
                    Block block = world.getBlockAt((chunkX << 4) + (horizontal & 15), y, (chunkZ << 4) + (horizontal >> 4));
                    block.setType(material(profile.materialAt(index)), false);
                }
                if (index == total) {
                    generating.remove(key);
                    cancel();
                }
            }
        }.runTaskTimer(getPlugin(), 1L, 1L);
    }

    private Material material(String name) {
        Material material = Material.matchMaterial(name.toUpperCase(Locale.ROOT));
        return material == null || !material.isBlock() ? Material.STONE : material;
    }

    private record ColumnKey(String world, int x, int z) {
        private static ColumnKey of(World world, int x, int z) {
            return new ColumnKey(world.getName(), x, z);
        }
    }
}
