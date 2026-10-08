package com.drakescraft.suites.tech.nanotech.protection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.UUID;

/** Fail-closed WorldGuard/ProtectionStones gate for every large-area ability. */
public final class ProtectionGate {
    public record OwnedRegion(Location minimum, Location maximum) {
        public boolean contains(Location location) {
            return location.getWorld() != null && minimum.getWorld() != null
                    && location.getWorld().equals(minimum.getWorld())
                    && location.getX() >= minimum.getX() && location.getX() <= maximum.getX()
                    && location.getY() >= minimum.getY() && location.getY() <= maximum.getY()
                    && location.getZ() >= minimum.getZ() && location.getZ() <= maximum.getZ();
        }

        public long volume() {
            return Math.max(1L, (maximum.getBlockX() - minimum.getBlockX() + 1L)
                    * (maximum.getBlockY() - minimum.getBlockY() + 1L)
                    * (maximum.getBlockZ() - minimum.getBlockZ() + 1L));
        }
    }
    private final org.bukkit.plugin.Plugin plugin;
    private final Method protectionStoneLookup;
    private boolean warned;

    public ProtectionGate(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
        this.protectionStoneLookup = findProtectionStoneLookup();
    }

    /** Blocks the ability when any sampled point intersects any region, including the actor's own. */
    public boolean allowLargeAbility(Player actor, Location center, double effectRadius) {
        int buffer = Math.max(0, plugin.getConfig().getInt("safety.protected-region-buffer", 24));
        double radius = effectRadius + buffer;
        for (Location sample : samples(center, radius)) {
            if (isProtected(sample)) {
                String raw = plugin.getConfig().getString("safety.protected-message",
                        "&6DrakesNanotech &8· &cYou are inside or near a protected region. &7This ability cannot be used here, even inside your own protection.");
                actor.sendMessage(ChatColor.translateAlternateColorCodes('&', raw));
                return false;
            }
        }
        return true;
    }

    /** Resolves an owner-managed ProtectionStones cuboid; uncertainty denies activation. */
    public OwnedRegion ownedProtection(Location location, UUID owner) {
        if (location == null || location.getWorld() == null) return null;
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return null;
        if (protectionStoneLookup == null) return null;
        try {
            Object psRegion = protectionStoneLookup.invoke(null, location);
            if (psRegion == null) return null;
            Object region = psRegion.getClass().getMethod("getRegion").invoke(psRegion);
            Object owners = region.getClass().getMethod("getOwners").invoke(region);
            boolean owns = (boolean) owners.getClass().getMethod("contains", UUID.class).invoke(owners, owner);
            if (!owns) return null;
            Object min = region.getClass().getMethod("getMinimumPoint").invoke(region);
            Object max = region.getClass().getMethod("getMaximumPoint").invoke(region);
            int minX = ((Number) min.getClass().getMethod("x").invoke(min)).intValue();
            int minY = ((Number) min.getClass().getMethod("y").invoke(min)).intValue();
            int minZ = ((Number) min.getClass().getMethod("z").invoke(min)).intValue();
            int maxX = ((Number) max.getClass().getMethod("x").invoke(max)).intValue();
            int maxY = ((Number) max.getClass().getMethod("y").invoke(max)).intValue();
            int maxZ = ((Number) max.getClass().getMethod("z").invoke(max)).intValue();
            return new OwnedRegion(new Location(location.getWorld(), minX, minY, minZ),
                    new Location(location.getWorld(), maxX, maxY, maxZ));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            if (!warned) {
                warned = true;
                plugin.getLogger().log(Level.WARNING, "ProtectionStone ownership lookup failed; prison fields fail closed.", error);
            }
            return null;
        }
    }

    private List<Location> samples(Location center, double radius) {
        List<Location> result = new ArrayList<>();
        result.add(center);
        for (int index = 0; index < 16; index++) {
            double angle = Math.PI * 2D * index / 16D;
            result.add(center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
            result.add(center.clone().add(Math.cos(angle) * radius * 0.5D, 0, Math.sin(angle) * radius * 0.5D));
        }
        return result;
    }

    /** Queries ProtectionStones first and WorldGuard second. Any integration failure denies use. */
    private boolean isProtected(Location location) {
        if (location == null || location.getWorld() == null) return true;
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return true; // Fail closed: unloaded chunks are assumed protected to avoid synchronous loading
        }
        try {
            if (protectionStoneLookup != null && protectionStoneLookup.invoke(null, location) != null) return true;
            if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) return protectionStoneLookup == null;
            Class<?> worldGuardType = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object worldGuard = worldGuardType.getMethod("getInstance").invoke(null);
            Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
            Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = container.getClass().getMethod("createQuery").invoke(container);
            Class<?> adapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object adapted = adapter.getMethod("adapt", Location.class).invoke(null, location);
            Class<?> worldEditLocation = Class.forName("com.sk89q.worldedit.util.Location");
            Object regions = query.getClass().getMethod("getApplicableRegions", worldEditLocation).invoke(query, adapted);
            return ((Number) regions.getClass().getMethod("size").invoke(regions)).intValue() > 0;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            if (!warned) {
                warned = true;
                plugin.getLogger().log(Level.WARNING, "Protection lookup failed; large abilities will fail closed.", error);
            }
            return true;
        }
    }

    private Method findProtectionStoneLookup() {
        try {
            return Class.forName("dev.espi.protectionstones.PSRegion")
                    .getMethod("fromLocation", Location.class);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}
