package com.drakescraft.suites.utility.portalgun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PortalGunEventListener implements Listener {

    public static final Map<String, List<PortalData>> portals = new ConcurrentHashMap<>();
    private final Set<UUID> doNotTeleport = Collections.synchronizedSet(new HashSet<>());
    private static final Map<UUID, Long> portalCooldowns = new ConcurrentHashMap<>();
    private final Plugin plugin;

    public PortalGunEventListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(ProjectileHitEvent e) {
        Entity projectile = e.getEntity();
        if (projectile instanceof Snowball ball && ball.hasMetadata("portalgunball")) {
            if (e.getHitBlock() == null || e.getHitBlockFace() == null || e.getHitBlock().getType() == Material.AIR) {
                return;
            }

            String playerName = ball.getMetadata("portalgunball").get(0).asString();
            String pgId = ball.getMetadata("portalgunid").get(0).asString();
            Player p = Bukkit.getPlayer(playerName);
            if (p == null) {
                return;
            }
            Location placement = e.getHitBlock().getLocation().clone().add(e.getHitBlockFace().getModX(), e.getHitBlockFace().getModY(), e.getHitBlockFace().getModZ());
            if (!Slimefun.getProtectionManager().hasPermission(p, e.getHitBlock().getLocation(), Interaction.BREAK_BLOCK)
                    || !Slimefun.getProtectionManager().hasPermission(p, placement, Interaction.PLACE_BLOCK)) {
                p.sendMessage("§cYou cannot create a portal in this protected area.");
                return;
            }
            if (!isPortalPlaceable(e.getHitBlock().getLocation(), e.getHitBlockFace())) {
                p.sendMessage("§cCan't place a portal there.");
                return;
            }

            List<PortalData> portalList = portals.computeIfAbsent(pgId, k -> new ArrayList<>());
            if (portalList.size() >= 2) {
                portalList.remove(0);
            }

            boolean isSecond = !portalList.isEmpty() && !portalList.get(0).second;

            String colorStr = ball.getMetadata(isSecond ? "color_2" : "color_1").get(0).asString();
            Color color = parseColor(colorStr, isSecond ? Color.ORANGE : Color.AQUA);

            PortalData pd = new PortalData();
            pd.blockFace = e.getHitBlockFace();
            pd.location = e.getHitBlock().getLocation();
            pd.color = color;
            pd.second = isSecond;

            portalList.add(pd);
            try {
                p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.0f);
            } catch (Exception ignored) {
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location loc = e.getTo();
        if (loc == null) return;

        handleGravityGun(p);

        if (doNotTeleport.contains(p.getUniqueId())) {
            return;
        }

        for (List<PortalData> pair : portals.values()) {
            if (pair.size() == 2) {
                PortalData p1 = pair.get(0);
                PortalData p2 = pair.get(1);

                if (p1.intersects(loc)) {
                    teleportPlayerThroughPortal(p, loc, p1, p2);
                    return;
                } else if (p2.intersects(loc)) {
                    teleportPlayerThroughPortal(p, loc, p2, p1);
                    return;
                }
            }
        }
    }

    private void handleGravityGun(Player p) {
        if (!GravityGunItem.holding.containsKey(p.getName())) {
            return;
        }
        Entity entity = GravityGunItem.holding.get(p.getName());
        if (entity == null || !entity.isValid()) {
            GravityGunItem.holding.remove(p.getName());
            return;
        }

        RayTraceResult rtr = p.rayTraceBlocks(6.0, FluidCollisionMode.NEVER);
        Location targetLoc;
        if (rtr != null && rtr.getHitPosition() != null) {
            targetLoc = rtr.getHitPosition().toLocation(p.getWorld());
        } else {
            targetLoc = p.getLocation().clone().add(p.getLocation().getDirection().multiply(4.0));
        }

        if (entity instanceof BlockDisplay) {
            targetLoc = targetLoc.setDirection(new Vector(0, 0, 0));
        }
        entity.teleport(targetLoc.add(0, 0.8, 0));
        entity.setFallDistance(0);
    }

    private void teleportPlayerThroughPortal(Player p, Location currentLoc, PortalData origin, PortalData destination) {
        if (!canTravel(p, currentLoc, destination.location)) return;
        doNotTeleport.add(p.getUniqueId());
        try {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } catch (Exception ignored) {
        }

        Location targetLoc = destination.location.clone();
        targetLoc.setYaw(currentLoc.getYaw());
        targetLoc.setPitch(currentLoc.getPitch());

        if (destination.blockFace == BlockFace.UP) {
            targetLoc.add(0.5, 1.0, 0.5);
        } else if (destination.blockFace == BlockFace.DOWN) {
            targetLoc.add(0.5, -2.0, 0.5);
        } else if (destination.blockFace == BlockFace.NORTH) {
            targetLoc.add(0.5, 0.0, -0.2);
        } else if (destination.blockFace == BlockFace.SOUTH) {
            targetLoc.add(0.5, 0.0, 1.2);
        } else if (destination.blockFace == BlockFace.EAST) {
            targetLoc.add(1.2, 0.0, 0.5);
        } else if (destination.blockFace == BlockFace.WEST) {
            targetLoc.add(-0.2, 0.0, 0.5);
        }

        p.teleport(targetLoc);

        Bukkit.getScheduler().runTaskLater(plugin, () -> p.setVelocity(exitVelocity(destination.blockFace)), 1L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            doNotTeleport.remove(p.getUniqueId());
        }, 10L);
    }

    public static void cleanup() {
        portals.clear();
        portalCooldowns.clear();
    }

    static boolean canTravel(Player player, Location from, Location destination) {
        long now = System.currentTimeMillis();
        long remaining = 10_000L - (now - portalCooldowns.getOrDefault(player.getUniqueId(), 0L));
        if (remaining > 0) {
            player.sendMessage("§cPortal stabilizing: wait " + ((remaining + 999) / 1000) + " seconds.");
            return false;
        }
        if (!Slimefun.getProtectionManager().hasPermission(player, from, Interaction.INTERACT_BLOCK)
                || !Slimefun.getProtectionManager().hasPermission(player, destination, Interaction.INTERACT_BLOCK)) {
            player.sendMessage("§cYou cannot use a portal across these protected regions.");
            return false;
        }
        portalCooldowns.put(player.getUniqueId(), now);
        return true;
    }

    private static boolean isPortalPlaceable(Location block, BlockFace face) {
        Location target = block.clone().add(face.getModX(), face.getModY(), face.getModZ());
        if (!target.getBlock().isPassable()) return false;
        if (face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST) {
            if (!target.clone().add(0, 1, 0).getBlock().isPassable()) return false;
        }
        return portals.values().stream().flatMap(Collection::stream).noneMatch(portal -> portal.location.getWorld().equals(block.getWorld()) && portal.location.distanceSquared(block) < 4);
    }

    private static Color parseColor(String color, Color fallback) {
        try {
            String[] parts = color.split(";");
            return parts.length == 3 ? Color.fromRGB(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])) : fallback;
        } catch (IllegalArgumentException ignored) { return fallback; }
    }

    private static Vector exitVelocity(BlockFace face) {
        return switch (face) {
            case DOWN -> new Vector(0, -0.3, 0);
            case NORTH -> new Vector(0, 0, -0.3);
            case SOUTH -> new Vector(0, 0, 0.3);
            case EAST -> new Vector(0.3, 0, 0);
            case WEST -> new Vector(-0.3, 0, 0);
            default -> new Vector(0, 0.3, 0);
        };
    }
}
