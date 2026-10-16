package com.drakescraft.suites.combat.slimefundisc;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Registro de discos acústicos y utilidades de creación de ítems para SlimefunDisc.
 */
public class DiscRegistry {

    private final Map<String, DiscTrack> tracks = new LinkedHashMap<>();

    public void registerTrack(DiscTrack track) {
        tracks.put(track.id().toUpperCase(Locale.ROOT), track);
    }

    public DiscTrack getTrack(String id) {
        return tracks.get(id.toUpperCase(Locale.ROOT));
    }

    public Collection<DiscTrack> getAllTracks() {
        return Collections.unmodifiableCollection(tracks.values());
    }

    public ItemStack createDiscItem(DiscTrack track) {
        ItemStack item = new ItemStack(track.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            SuiteItemPdcBridge.setSlimefunId(meta, track.id());

            CrossVersionAdapter.setDisplayName(meta, "§bMusic Disc: §f" + track.title());

            List<String> lore = new ArrayList<>();
            lore.add("§7Artist: §e" + track.artist());
            lore.add("§7Duration: §f" + (track.durationSeconds() / 60) + ":" + String.format("%02d", track.durationSeconds() % 60));
            lore.add("");
            lore.add("§8[StarSuites · SlimefunDisc]");
            CrossVersionAdapter.setLore(meta, lore);

            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }
}
