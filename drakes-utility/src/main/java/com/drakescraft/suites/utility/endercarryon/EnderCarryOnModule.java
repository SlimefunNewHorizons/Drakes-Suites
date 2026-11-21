package com.drakescraft.suites.utility.endercarryon;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * EnderCarryOn Module for DrakesUtility.
 * Allows players to pick up and transport chests, barrels, and peaceful mobs.
 */
public class EnderCarryOnModule extends AbstractSuiteModule implements Listener {

    private boolean allowEntities = true;
    private boolean slowDownPlayer = true;

    public EnderCarryOnModule(JavaPlugin plugin) {
        super(plugin, "endercarryon", "EnderCarryOn Block & Entity Carry Mechanics");
    }

    @Override
    public void onEnable() {
        this.allowEntities = config.getBoolean("allow-entities", true);
        this.slowDownPlayer = config.getBoolean("slow-down-player", true);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("EnderCarryOn module enabled (allowEntities=" + allowEntities + ", slowDown=" + slowDownPlayer + ").");
    }

    @Override
    public void onDisable() {
        logInfo("EnderCarryOn module disabled.");
    }

    public boolean isAllowEntities() {
        return allowEntities;
    }

    public boolean isSlowDownPlayer() {
        return slowDownPlayer;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is carrying an entity
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(CarryKeys.CARRY, PersistentDataType.BYTE)) {
            if (item.getItemMeta().getPersistentDataContainer().has(CarryKeys.ENTITY_DATA, PersistentDataType.STRING)) {
                if (event.getAction().isRightClick() && event.getClickedBlock() != null) {
                    String snapshotData = item.getItemMeta().getPersistentDataContainer().get(CarryKeys.ENTITY_DATA, PersistentDataType.STRING);
                    if (snapshotData != null) {
                        try {
                            EntitySnapshot snapshot = Bukkit.getEntityFactory().createEntitySnapshot(snapshotData);
                            Location location = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
                            snapshot.createEntity(location);
                        } catch (Exception ex) {
                            logWarn("Could not deserialize carried entity snapshot: " + ex.getMessage());
                        }
                    }
                    player.getInventory().setItemInMainHand(null);
                    event.setCancelled(true);
                }
                return;
            }
        }

        // Check if player wants to pick up a block
        if (CarryHelper.isValidCarryAttempt(event, plugin)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                ItemStack carryBlock = CarryHelper.getCarryBlock(clickedBlock);
                int selectedSlot = player.getInventory().getHeldItemSlot();
                player.getInventory().setItem(selectedSlot, carryBlock);
                clickedBlock.setType(Material.AIR);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!allowEntities) return;

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (player.isSneaking() && player.getInventory().getItemInMainHand().getType().isAir() && CarryHelper.isAllowedEntity(entity)) {
            ItemStack carryItem = CarryHelper.getCarryEntityItem(entity);
            int selectedSlot = player.getInventory().getHeldItemSlot();

            player.getInventory().setItem(selectedSlot, carryItem);
            entity.remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isCarriedItem(droppedItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (isCarriedItem(event.getCurrentItem()) || isCarriedItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }
        int hotbar = event.getHotbarButton();
        if (hotbar >= 0 && isCarriedItem(event.getWhoClicked().getInventory().getItem(hotbar))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isCarriedItem(event.getOldCursor())) {
            event.setCancelled(true);
            return;
        }
        for (ItemStack item : event.getNewItems().values()) {
            if (isCarriedItem(item)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        ItemStack currentItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        if (isCarriedItem(currentItem)) {
            event.setCancelled(true);
        }
    }

    public static boolean isCarriedItem(ItemStack item) {
        return item != null &&
               item.hasItemMeta() &&
               item.getItemMeta().getPersistentDataContainer().has(CarryKeys.CARRY, PersistentDataType.BYTE);
    }
}
