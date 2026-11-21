package com.drakescraft.suites.combat.slimefundisc;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Módulo nativo SlimefunDisc integrado en DrakesCombat.
 * Gestiona discos musicales acústicos personalizados y reproducción en tocadiscos (Jukebox).
 */
public class SlimefunDiscModule extends AbstractSuiteModule implements Listener {

    private final DiscRegistry registry = new DiscRegistry();
    private final Map<Location, String> activeJukeboxes = new ConcurrentHashMap<>();

    public SlimefunDiscModule(JavaPlugin plugin) {
        super(plugin, "slimefundisc", "SlimefunDisc Custom Acoustic & Music Resonance");
    }

    static boolean isStandaloneSlimefunDiscPresent() {
        return org.bukkit.Bukkit.getPluginManager() != null
                && org.bukkit.Bukkit.getPluginManager().getPlugin("Slimefun-Disc") != null;
    }

    @Override
    public void onEnable() {
        registerDefaultTracks();
        if (isStandaloneSlimefunDiscPresent()) {
            getPlugin().getLogger().info("[SlimefunDisc] Plugin standalone Slimefun-Disc detectado; cediendo listener al addon dedicado.");
            return;
        }
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());

        getPlugin().getLogger().info("[SlimefunDisc] Módulo acústico habilitado ("
                + registry.getAllTracks().size() + " pistas musicales registradas).");
    }

    @Override
    public void onDisable() {
        activeJukeboxes.clear();
        getPlugin().getLogger().info("[SlimefunDisc] Módulo acústico deshabilitado.");
    }

    private void registerDefaultTracks() {
        registry.registerTrack(new DiscTrack(
                "SF_DISC_BOHEMIAN", "Bohemian Rhapsody", "Queen",
                Material.MUSIC_DISC_WAIT, 355
        ));

        registry.registerTrack(new DiscTrack(
                "SF_DISC_ALBATRAOZ", "I'm an Albatraoz", "AronChupa",
                Material.MUSIC_DISC_PIGSTEP, 166
        ));

        registry.registerTrack(new DiscTrack(
                "SF_DISC_LONELY_DAY", "Lonely Day", "System of a Down",
                Material.MUSIC_DISC_OTHERSIDE, 167
        ));

        registry.registerTrack(new DiscTrack(
                "SF_DISC_MEZMERIZER", "Mezmerizer", "Satsuki",
                Material.MUSIC_DISC_RELIC, 172
        ));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onJukeboxInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.JUKEBOX) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (SuiteItemPdcBridge.isSlimefunItem(hand)) {
            String sfId = SuiteItemPdcBridge.getSlimefunId(hand);
            DiscTrack track = registry.getTrack(sfId);
            if (track != null) {
                activeJukeboxes.put(block.getLocation(), track.id());
                player.sendActionBar(net.kyori.adventure.text.Component.text("♪ Reproduciendo: §e" + track.title() + " - " + track.artist()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.JUKEBOX) {
            activeJukeboxes.remove(event.getBlock().getLocation());
        }
    }

    public DiscRegistry getRegistry() {
        return registry;
    }

    public Map<Location, String> getActiveJukeboxes() {
        return activeJukeboxes;
    }
}
