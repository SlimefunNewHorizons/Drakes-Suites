package com.drakescraft.suites.bio.treetaps;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Módulo SlimyTreeTaps para DrakesBio.
 * Permite la extracción de resina pegajosa y ámbar a partir de troncos arbóreos,
 * produciendo caucho biológico y plásticos alternativos.
 * Preserva 100% de compatibilidad PDC con Slimefun y compatibilidad dual Paper 1.21.11 / Purpur 26.X.
 */
public class TreeTapsModule extends AbstractSuiteModule implements Listener {

    public static final String ID_TREE_TAP = "TREE_TAP";
    public static final String ID_REINFORCED_TREE_TAP = "REINFORCED_TREE_TAP";
    public static final String ID_DIAMOND_TREE_TAP = "DIAMOND_TREE_TAP";
    public static final String ID_TREE_SCRAPER = "TREE_SCRAPER";
    public static final String ID_STICKY_RESIN = "STICKY_RESIN";
    public static final String ID_RUBBER = "RUBBER";
    public static final String ID_RAW_PLASTIC = "RAW_PLASTIC";
    public static final String ID_AMBER = "AMBER";
    public static final String ID_AMBER_BLOCK = "AMBER_BLOCK";

    private int standardChance = 25;
    private int reinforcedChance = 50;
    private int diamondChance = 75;
    private int scraperChance = 35;

    public TreeTapsModule(JavaPlugin plugin) {
        super(plugin, "slimytreetaps", "SlimyTreeTaps Bio-Resin Harvesting");
    }

    @Override
    public void onEnable() {
        this.standardChance = config.getInt("resin-chance.standard", 25);
        this.reinforcedChance = config.getInt("resin-chance.reinforced", 50);
        this.diamondChance = config.getInt("resin-chance.diamond", 75);
        this.scraperChance = config.getInt("amber-chance", 35);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("SlimyTreeTaps cargado. Extracción de resina: estándar=" + standardChance + "%, reforzado=" + reinforcedChance + "%, diamante=" + diamondChance + "%");
    }

    @Override
    public void onDisable() {
    }

    public ItemStack createTreeTap(String id, Material mat, String name, int chance) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gradient:#ffaa00:#ff5500><bold>" + name + "</bold></gradient>");
            List<String> lore = Arrays.asList(
                    "&7Click Derecho en troncos de madera vivos",
                    "&7para recolectar resina de árbol.",
                    "",
                    "&8» &fProbabilidad de éxito: &e" + chance + "%",
                    "&8» &fID: &e" + id
            );
            CrossVersionAdapter.setLore(meta, lore);
            SuiteItemPdcBridge.setSlimefunId(meta, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createStickyResin() {
        ItemStack item = new ItemStack(Material.BROWN_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gold><bold>Sticky Resin</bold></gold>");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&7Resina vegetal viscosa extraída de troncos.",
                    "&7Puede procesarse en caucho y derivados plásticos.",
                    "",
                    "&8» &fID: &e" + ID_STICKY_RESIN
            ));
            SuiteItemPdcBridge.setSlimefunId(meta, ID_STICKY_RESIN);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createAmber() {
        ItemStack item = new ItemStack(Material.HONEYCOMB);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gold><bold>Amber</bold></gold>");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&7Gema fosilizada de resina endurecida.",
                    "&7Utilizada en joyería y artefactos mágicos.",
                    "",
                    "&8» &fID: &e" + ID_AMBER
            ));
            SuiteItemPdcBridge.setSlimefunId(meta, ID_AMBER);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTreeTapInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null || !Tag.LOGS.isTagged(block.getType())) {
            return;
        }

        // Evitar procesar troncos ya descortezados
        if (block.getType().name().startsWith("STRIPPED_")) {
            return;
        }

        ItemStack inHand = e.getItem();
        if (inHand == null || !SuiteItemPdcBridge.isSlimefunItem(inHand)) {
            return;
        }

        String sfId = SuiteItemPdcBridge.getSlimefunId(inHand);
        int chance = 0;
        boolean isAmberScraper = false;

        if (ID_TREE_TAP.equalsIgnoreCase(sfId)) {
            chance = standardChance;
        } else if (ID_REINFORCED_TREE_TAP.equalsIgnoreCase(sfId)) {
            chance = reinforcedChance;
        } else if (ID_DIAMOND_TREE_TAP.equalsIgnoreCase(sfId)) {
            chance = diamondChance;
        } else if (ID_TREE_SCRAPER.equalsIgnoreCase(sfId)) {
            chance = scraperChance;
            isAmberScraper = true;
        } else {
            return;
        }

        e.setCancelled(true);
        Player player = e.getPlayer();

        // Efecto sonoro y visual al golpear la corteza
        player.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
        player.playSound(block.getLocation(), Sound.ITEM_AXE_STRIP, 1.0f, 1.0f);

        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < chance) {
            // Descortezar el tronco de forma segura
            String strippedName = "STRIPPED_" + block.getType().name();
            try {
                Material strippedMat = Material.valueOf(strippedName);
                block.setType(strippedMat);
            } catch (IllegalArgumentException ignored) {}

            // Soltar la resina o ámbar
            ItemStack drop = isAmberScraper ? createAmber() : createStickyResin();
            BlockFace face = e.getBlockFace();
            Location dropLoc = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
            block.getWorld().dropItemNaturally(dropLoc, drop);

            CrossVersionAdapter.sendActionBar(player, "<green>✔ Successfully extracted " + (isAmberScraper ? "amber" : "resin") + ".</green>");
        } else {
            CrossVersionAdapter.sendActionBar(player, "<gray>The bark does not contain enough resin.</gray>");
        }

        // Aplicar daño a la herramienta
        damageTool(player, inHand);
    }

    private void damageTool(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + 1);
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                item.setAmount(0);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                item.setItemMeta(meta);
            }
        }
    }
}
