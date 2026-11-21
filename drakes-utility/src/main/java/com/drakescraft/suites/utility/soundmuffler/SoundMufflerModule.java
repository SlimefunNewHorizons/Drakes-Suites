package com.drakescraft.suites.utility.soundmuffler;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Módulo SoundMuffler para DrakesUtility.
 * Amortigua y silencia ruidos mecánicos y ambientales en un radio de bloques configurable.
 * Preserva compatibilidad con Slimefun ("SOUND_MUFFLER") y compatibilidad dual Paper 1.21.11 / Purpur 26.X.
 */
public class SoundMufflerModule extends AbstractSuiteModule implements Listener {

    public static final String ITEM_ID = "SOUND_MUFFLER";
    private static final String GUI_TITLE = "Sound Muffler Settings";

    private final Map<Location, MufflerData> mufflers = new ConcurrentHashMap<>();
    private final Map<UUID, Location> openMufflerGuis = new ConcurrentHashMap<>();
    private final Map<UUID, Inventory> openMufflerInventories = new ConcurrentHashMap<>();

    private int defaultRadius = 8;
    private int defaultVolume = 10;
    private boolean energyRequired = false;

    public SoundMufflerModule(JavaPlugin plugin) {
        super(plugin, "soundmuffler", "SoundMuffler Machine Noise Dampener");
    }

    public static class MufflerData {
        private boolean enabled;
        private int volume; // 0 to 100
        private int radius;

        public MufflerData(boolean enabled, int volume, int radius) {
            this.enabled = enabled;
            this.volume = Math.max(0, Math.min(100, volume));
            this.radius = radius;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getVolume() { return volume; }
        public void setVolume(int volume) { this.volume = Math.max(0, Math.min(100, volume)); }
        public int getRadius() { return radius; }
        public void setRadius(int radius) { this.radius = radius; }
    }

    @Override
    public void onEnable() {
        this.defaultRadius = config.getInt("radius", 8);
        this.defaultVolume = config.getInt("default-volume", 10);
        this.energyRequired = config.getBoolean("requires-power", false);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("SoundMuffler cargado con radio base de " + defaultRadius + " bloques.");
    }

    @Override
    public void onDisable() {
        mufflers.clear();
        openMufflerGuis.clear();
        openMufflerInventories.clear();
    }

    public ItemStack createMufflerItem() {
        ItemStack item = new ItemStack(Material.WHITE_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gradient:#00ffcc:#0077ff><bold>Sound Muffler</bold></gradient>");
            List<String> lore = Arrays.asList(
                    "&7Muffles machine and mob noises",
                    "&7in an &b" + defaultRadius + " block &7radius.",
                    "",
                    "&8» &eRight-Click &7to configure volume",
                    "&8» &fID: &e" + ITEM_ID
            );
            CrossVersionAdapter.setLore(meta, lore);
            SuiteItemPdcBridge.setSlimefunId(meta, ITEM_ID);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (SuiteItemPdcBridge.hasSlimefunId(item, ITEM_ID)) {
            Location loc = e.getBlock().getLocation();
            mufflers.put(loc, new MufflerData(true, defaultVolume, defaultRadius));
            CrossVersionAdapter.sendActionBar(e.getPlayer(), "<green>✔ Sound Muffler colocado y activado.</green>");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        mufflers.remove(loc);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block b = e.getClickedBlock();
        if (b == null || b.getType() != Material.WHITE_CONCRETE) {
            return;
        }

        Location loc = b.getLocation();
        MufflerData data = mufflers.get(loc);
        if (data == null) {
            return;
        }

        e.setCancelled(true);
        openMufflerGui(e.getPlayer(), loc, data);
    }

    private void openMufflerGui(Player player, Location loc, MufflerData data) {
        Inventory inv = Bukkit.createInventory(null, 9, CrossVersionAdapter.parseComponent("<dark_aqua>" + GUI_TITLE + "</dark_aqua>"));

        // Slot 0: Control de volumen
        ItemStack volumeItem = new ItemStack(Material.PAPER);
        ItemMeta vMeta = volumeItem.getItemMeta();
        if (vMeta != null) {
            CrossVersionAdapter.setDisplayName(vMeta, "<yellow><bold>Volumen: </bold><aqua>" + data.getVolume() + "%</aqua></yellow>");
            CrossVersionAdapter.setLore(vMeta, Arrays.asList(
                    "&7Rango válido: 0% - 100%",
                    "&eClick Izquierdo: &a+10%",
                    "&eClick Derecho: &c-10%",
                    "&eShift + Click: &b+/- 1%"
            ));
            volumeItem.setItemMeta(vMeta);
        }
        inv.setItem(0, volumeItem);

        // Borde decorativo
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) {
            CrossVersionAdapter.setDisplayName(gMeta, " ");
            glass.setItemMeta(gMeta);
        }
        for (int i = 1; i <= 7; i++) {
            inv.setItem(i, glass);
        }

        // Slot 8: Estado Encendido / Apagado
        Material toggleMat = data.isEnabled() ? Material.REDSTONE : Material.GUNPOWDER;
        ItemStack toggleItem = new ItemStack(toggleMat);
        ItemMeta tMeta = toggleItem.getItemMeta();
        if (tMeta != null) {
            String status = data.isEnabled() ? "<green><bold>Activado</bold></green>" : "<red><bold>Desactivado</bold></red>";
            CrossVersionAdapter.setDisplayName(tMeta, "<gray>Estado: </gray>" + status);
            CrossVersionAdapter.setLore(tMeta, Collections.singletonList("&eClick para alternar encendido/apagado"));
            toggleItem.setItemMeta(tMeta);
        }
        inv.setItem(8, toggleItem);

        // Abrir ANTES de registrar en los mapas para evitar que un InventoryCloseEvent previo limpie el nuevo GUI
        player.openInventory(inv);
        openMufflerGuis.put(player.getUniqueId(), loc);
        openMufflerInventories.put(player.getUniqueId(), inv);
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        Inventory expected = openMufflerInventories.get(uuid);
        if (expected == null || e.getView().getTopInventory() != expected) {
            return;
        }
        Location loc = openMufflerGuis.get(uuid);
        if (loc == null) {
            return;
        }

        e.setCancelled(true);
        MufflerData data = mufflers.get(loc);
        if (data == null) {
            player.closeInventory();
            return;
        }

        int slot = e.getRawSlot();
        if (slot == 0) {
            // Cambiar volumen
            int delta = e.isShiftClick() ? 1 : 10;
            if (e.isLeftClick()) {
                data.setVolume(Math.min(100, data.getVolume() + delta));
            } else if (e.isRightClick()) {
                data.setVolume(Math.max(0, data.getVolume() - delta));
            }
            openMufflerGui(player, loc, data);
        } else if (slot == 8) {
            // Alternar estado
            data.setEnabled(!data.isEnabled());
            player.playSound(loc, Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            openMufflerGui(player, loc, data);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        Inventory expected = openMufflerInventories.get(uuid);
        if (expected != null && e.getInventory() == expected) {
            openMufflerInventories.remove(uuid);
            openMufflerGuis.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        openMufflerInventories.remove(uuid);
        openMufflerGuis.remove(uuid);
    }

    public MufflerData getMufflerAt(Location loc) {
        return mufflers.get(loc);
    }

    public Map<Location, MufflerData> getActiveMufflers() {
        return Collections.unmodifiableMap(mufflers);
    }

    public boolean isMuffled(Location soundLoc) {
        for (Map.Entry<Location, MufflerData> entry : mufflers.entrySet()) {
            Location mLoc = entry.getKey();
            MufflerData data = entry.getValue();
            if (data.isEnabled() && mLoc.getWorld() != null && mLoc.getWorld().equals(soundLoc.getWorld())) {
                if (mLoc.distanceSquared(soundLoc) <= (long) data.getRadius() * data.getRadius()) {
                    return true;
                }
            }
        }
        return false;
    }
}
