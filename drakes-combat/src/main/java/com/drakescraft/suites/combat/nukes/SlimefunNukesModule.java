package com.drakescraft.suites.combat.nukes;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Módulo nativo SlimefunNukes para DrakesCombat.
 * Gestiona ojivas nucleares tácticas, cebado seguro con pedernal y acero, y radio de impacto atómico.
 */
public class SlimefunNukesModule extends AbstractSuiteModule implements Listener {

    private final Map<NukeWarheadType, ItemStack> itemCache = new EnumMap<>(NukeWarheadType.class);
    private boolean asyncBlastRadiusEnabled = true;
    private boolean safeZoneShielding = true;
    private boolean radiationFieldEnabled = true;

    public SlimefunNukesModule(JavaPlugin plugin) {
        super(plugin, "slimefun_nukes", "SlimefunNukes Tactical Warheads & Deterrence");
    }

    @Override
    public void onEnable() {
        this.asyncBlastRadiusEnabled = getConfig().getBoolean("performance.async-blast-radius-calculation", true);
        this.safeZoneShielding = getConfig().getBoolean("features.safe-zone-shielding", true);
        this.radiationFieldEnabled = getConfig().getBoolean("features.radiation-field", true);

        // Pre-cachear ítems canónicos
        for (NukeWarheadType type : NukeWarheadType.values()) {
            itemCache.put(type, buildWarheadItem(type));
        }

        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        logInfo("SlimefunNukesModule activado con " + NukeWarheadType.values().length + " ojivas nucleares registradas.");
    }

    @Override
    public void onDisable() {
        itemCache.clear();
        logInfo("SlimefunNukesModule deshabilitado limpiamente.");
    }

    public ItemStack getWarheadItem(NukeWarheadType type) {
        ItemStack cached = itemCache.get(type);
        return cached != null ? cached.clone() : buildWarheadItem(type);
    }

    public ItemStack buildWarheadItem(NukeWarheadType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', type.getDisplayName()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Radio de impacto: &c" + type.getRadius() + " bloques"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Tiempo de mecha: &e" + type.getFuseSeconds() + "s"));
            if (type.isIncendiary()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&6🔥 Carga Incendiaria Avanzada"));
            }
            lore.add(ChatColor.translateAlternateColorCodes('&', "&c☢ Radiactivo: &4Nivel Alto"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&8» &fID: &e" + type.getSlimefunId()));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, type.getSlimefunId());
        return item;
    }

    public boolean isNukeWarhead(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String sfId = SuiteItemPdcBridge.getSlimefunId(item);
        return NukeWarheadType.getBySlimefunId(sfId) != null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerArmNuke(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        ItemStack inHand = event.getItem();
        if (inHand == null || inHand.getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked.getType() != Material.TNT) {
            return;
        }

        // Simulación de cebado de ojiva nuclear
        event.setCancelled(true);
        Location loc = clicked.getLocation().add(0.5, 0, 0.5);
        World world = loc.getWorld();
        if (world == null) return;

        clicked.setType(Material.AIR);

        NukeWarheadType nukeType = NukeWarheadType.LITTLE_NUKE;
        TNTPrimed tnt = (TNTPrimed) world.spawnEntity(loc, EntityType.TNT);
        tnt.setFuseTicks(nukeType.getFuseSeconds() * 20);
        tnt.setIsIncendiary(nukeType.isIncendiary());
        tnt.setYield(nukeType.getRadius());
        tnt.setCustomName(ChatColor.translateAlternateColorCodes('&', nukeType.getDisplayName()));
        tnt.setCustomNameVisible(true);

        logInfo("Ojiva nuclear cebada en " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " por " + event.getPlayer().getName());
    }

    public boolean isAsyncBlastRadiusEnabled() {
        return asyncBlastRadiusEnabled;
    }

    public boolean isSafeZoneShielding() {
        return safeZoneShielding;
    }

    public boolean isRadiationFieldEnabled() {
        return radiationFieldEnabled;
    }
}
