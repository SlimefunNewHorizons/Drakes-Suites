package com.drakescraft.suites.utility.portalgun;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/** Native Paper control panel for the imported Portal Gun state. */
final class PortalGunMenuListener implements Listener {
    private static final String TITLE = "Portal Gun Control";
    private final Plugin plugin;

    PortalGunMenuListener(Plugin plugin) { this.plugin = plugin; }

    void open(Player player, ItemStack gun) {
        Menu menu = new Menu(player.getUniqueId());
        Inventory inventory = plugin.getServer().createInventory(menu, 27, TITLE);
        menu.inventory = inventory;
        render(inventory, player, gun);
        player.openInventory(inventory);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !menu.owner.equals(player.getUniqueId()) || event.getRawSlot() >= event.getInventory().getSize()) return;
        ItemStack gun = findHeldGun(player);
        if (gun == null) {
            player.closeInventory();
            player.sendMessage("§cHold your Portal Gun to configure it.");
            return;
        }
        int slot = event.getRawSlot();
        if (slot == 11) {
            PortalGunState.toggleMode(gun);
            player.sendMessage("§aPortal mode: " + PortalGunState.mode(gun));
        } else if (slot == 13) {
            if (!player.hasPermission("sfportalguns.infinite")) player.sendMessage("§cInfinite mode is staff-only.");
            else PortalGunState.setInfinite(gun, !PortalGunState.isInfinite(gun));
        } else if (slot == 15) {
            if (!PortalGunState.saveLocation(gun, player.getLocation())) player.sendMessage("§cAll eight saved-location slots are full.");
            else player.sendMessage("§aCurrent location saved.");
        } else if (slot >= 18 && slot < 26) {
            int savedSlot = slot - 18;
            if (PortalGunState.savedLocation(gun, savedSlot) == null) player.sendMessage("§cThat location slot is empty.");
            else if (event.isRightClick()) {
                PortalGunState.deleteLocation(gun, savedSlot);
                player.sendMessage("§eSaved location removed.");
            } else {
                PortalGunState.setSelectedSlot(gun, savedSlot);
                player.sendMessage("§bSelected destination " + (savedSlot + 1) + ".");
            }
        }
        render(event.getInventory(), player, gun);
    }

    private ItemStack findHeldGun(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (PortalGunItems.PORTAL_GUN.equals(SuiteItemPdcBridge.getSlimefunId(main))) return main;
        ItemStack offhand = player.getInventory().getItemInOffHand();
        return PortalGunItems.PORTAL_GUN.equals(SuiteItemPdcBridge.getSlimefunId(offhand)) ? offhand : null;
    }

    private void render(Inventory inventory, Player player, ItemStack gun) {
        inventory.clear();
        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, icon(Material.GRAY_STAINED_GLASS_PANE, "§8"));
        inventory.setItem(10, icon(Material.LIME_DYE, "§aPortal charge: " + PortalGunState.charge(gun) + "/" + PortalGunState.MAX_CHARGE, "§7Refill with Portal Fluid in your offhand."));
        inventory.setItem(11, icon(Material.ENDER_EYE, "§bMode: " + PortalGunState.mode(gun), "§7Switch linked portals / saved destination."));
        inventory.setItem(13, icon(Material.NETHER_STAR, "§6Infinite mode: " + PortalGunState.isInfinite(gun), player.hasPermission("sfportalguns.infinite") ? "§7Staff toggle." : "§cStaff permission required."));
        inventory.setItem(15, icon(Material.COMPASS, "§eSave current location", "§7Stores into the next free slot."));
        int selected = PortalGunState.selectedSlot(gun);
        for (int slot = 0; slot < PortalGunState.MAX_SAVED_LOCATIONS; slot++) {
            boolean set = PortalGunState.savedLocation(gun, slot) != null;
            inventory.setItem(18 + slot, icon(set ? Material.ENDER_PEARL : Material.BLACK_STAINED_GLASS_PANE,
                    (set ? "§b" : "§8") + "Location " + (slot + 1) + (selected == slot ? " §a[selected]" : ""),
                    set ? "§7Left click: select. Right click: delete." : "§7Empty."));
        }
    }

    private ItemStack icon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, name);
            CrossVersionAdapter.setLore(meta, List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static final class Menu implements InventoryHolder {
        private final UUID owner;
        private Inventory inventory;
        private Menu(UUID owner) { this.owner = owner; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
