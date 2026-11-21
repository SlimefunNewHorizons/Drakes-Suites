package com.drakescraft.suites.utility.simpleutils;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Módulo SimpleUtils para DrakesUtility.
 * Consolida utilidades esenciales de Slimefun:
 * 1. Simple Elevator (salto para subir, sneak para bajar).
 * 2. Simple Workbench (mesa portátil / unificada).
 * 3. Simple Wrench (herramienta de desmontaje rápido de máquinas).
 * Claves PDC idénticas a upstream: SIMPLE_ELEVATOR, SIMPLE_WORKBENCH, SIMPLE_WRENCH.
 */
public class SimpleUtilsModule extends AbstractSuiteModule implements Listener {

    public static final String ID_ELEVATOR = "SIMPLE_ELEVATOR";
    public static final String ID_WORKBENCH = "SIMPLE_WORKBENCH";
    public static final String ID_WRENCH = "SIMPLE_WRENCH";

    private int elevatorMaxDistance = 64;
    private boolean elevatorSoundEnabled = true;

    public SimpleUtilsModule(JavaPlugin plugin) {
        super(plugin, "simpleutils", "SimpleUtils Utility Blocks & Tools");
    }

    @Override
    public void onEnable() {
        this.elevatorMaxDistance = config.getInt("elevator.max-distance", 64);
        this.elevatorSoundEnabled = config.getBoolean("elevator.enable-sound", true);

        if (isStandaloneSimpleUtilsPresent()) {
            logInfo("Plugin standalone SimpleUtils detectado; cediendo eventos de Simple Workbench y elevadores al addon dedicado.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("SimpleUtils cargado con elevadores de distancia máxima " + elevatorMaxDistance + " bloques.");
    }

    static boolean isStandaloneSimpleUtilsPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("SimpleUtils") != null;
    }

    @Override
    public void onDisable() {
    }

    public ItemStack createElevatorItem() {
        ItemStack item = new ItemStack(Material.QUARTZ_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gradient:#ffffff:#aaaaaa><bold>Simple Elevator</bold></gradient>");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&7Colócalo en pisos alineados verticalmente.",
                    "&aSalto &7para subir al piso superior.",
                    "&eAgacharse (Sneak) &7para bajar al piso inferior.",
                    "",
                    "&8» &fID: &e" + ID_ELEVATOR
            ));
            SuiteItemPdcBridge.setSlimefunId(meta, ID_ELEVATOR);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWorkbenchItem() {
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gradient:#ffaa00:#ff5500><bold>Simple Workbench</bold></gradient>");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&7Mesa de trabajo avanzada.",
                    "&7Abre la interfaz de crafteo directamente.",
                    "",
                    "&8» &fID: &e" + ID_WORKBENCH
            ));
            SuiteItemPdcBridge.setSlimefunId(meta, ID_WORKBENCH);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createWrenchItem() {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gradient:#55ffff:#00aaff><bold>Simple Wrench</bold></gradient>");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&7Click Derecho para desmontar instantáneamente",
                    "&7máquinas de Slimefun y componentes de energía.",
                    "",
                    "&8» &fID: &e" + ID_WRENCH
            ));
            SuiteItemPdcBridge.setSlimefunId(meta, ID_WRENCH);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJump(PlayerMoveEvent e) {
        if (e.getTo() == null || e.getTo().getY() <= e.getFrom().getY()) {
            return;
        }
        // Verificar que el movimiento vertical es un salto (no subida de escaleras continua)
        if (e.getFrom().getY() - e.getFrom().getBlockY() > 0.05) {
            return;
        }

        Player player = e.getPlayer();
        Location under = player.getLocation().clone().subtract(0, 1, 0);
        Block blockUnder = under.getBlock();

        if (blockUnder.getType() == Material.QUARTZ_BLOCK) {
            // Buscar elevador hacia arriba
            Location target = findElevatorUp(under);
            if (target != null) {
                teleportElevator(player, target);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) {
            return;
        }

        Player player = e.getPlayer();
        Location under = player.getLocation().clone().subtract(0, 1, 0);
        Block blockUnder = under.getBlock();

        if (blockUnder.getType() == Material.QUARTZ_BLOCK) {
            // Buscar elevador hacia abajo
            Location target = findElevatorDown(under);
            if (target != null) {
                teleportElevator(player, target);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorkbenchInteract(PlayerInteractEvent e) {
        if (isStandaloneSimpleUtilsPresent()) {
            return;
        }
        // Only open portable workbench on RIGHT_CLICK_AIR; never intercept RIGHT_CLICK_BLOCK so the block can be placed.
        if (e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = e.getItem();
        if (item != null && SuiteItemPdcBridge.hasSlimefunId(item, ID_WORKBENCH)) {
            e.setCancelled(true);
            e.getPlayer().openWorkbench(null, true);
        }
    }

    private Location findElevatorUp(Location start) {
        World world = start.getWorld();
        if (world == null) return null;

        int currentY = start.getBlockY();
        int maxY = Math.min(world.getMaxHeight() - 1, currentY + elevatorMaxDistance);

        for (int y = currentY + 3; y <= maxY; y++) {
            Block b = world.getBlockAt(start.getBlockX(), y, start.getBlockZ());
            if (b.getType() == Material.QUARTZ_BLOCK) {
                // Verificar espacio libre para el jugador
                Block above1 = world.getBlockAt(start.getBlockX(), y + 1, start.getBlockZ());
                Block above2 = world.getBlockAt(start.getBlockX(), y + 2, start.getBlockZ());
                if (!above1.getType().isSolid() && !above2.getType().isSolid()) {
                    return b.getLocation().add(0.5, 1.0, 0.5);
                }
            }
        }
        return null;
    }

    private Location findElevatorDown(Location start) {
        World world = start.getWorld();
        if (world == null) return null;

        int currentY = start.getBlockY();
        int minY = Math.max(world.getMinHeight(), currentY - elevatorMaxDistance);

        for (int y = currentY - 3; y >= minY; y--) {
            Block b = world.getBlockAt(start.getBlockX(), y, start.getBlockZ());
            if (b.getType() == Material.QUARTZ_BLOCK) {
                // Verificar espacio libre para el jugador
                Block above1 = world.getBlockAt(start.getBlockX(), y + 1, start.getBlockZ());
                Block above2 = world.getBlockAt(start.getBlockX(), y + 2, start.getBlockZ());
                if (!above1.getType().isSolid() && !above2.getType().isSolid()) {
                    return b.getLocation().add(0.5, 1.0, 0.5);
                }
            }
        }
        return null;
    }

    private void teleportElevator(Player player, Location target) {
        target.setYaw(player.getLocation().getYaw());
        target.setPitch(player.getLocation().getPitch());
        player.teleport(target);

        if (elevatorSoundEnabled) {
            player.playSound(target, Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.8f);
        }
        CrossVersionAdapter.sendActionBar(player, "<gradient:#55ffff:#ffffff>Piso alcanzado</gradient>");
    }
}
