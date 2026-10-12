package com.drakescraft.suites.utility.portalgun;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.*;

/**
 * PortalGun items registry with 100% Universal English canon.
 */
public final class PortalGunItems {

    public static final String PORTAL_GUN = "PORTAL_GUN";
    public static final String PORTAL_FLUID = "PORTAL_FLUID";
    public static final String GRAVITY_GUN = "GRAVITY_GUN";

    private static final Map<String, ItemStack> ITEMS = new HashMap<>();

    static {
        register(PORTAL_FLUID, Material.SLIME_BALL,
                "<gradient:#74ff73:#00b894><bold>Portal Fluid</bold></gradient>",
                Arrays.asList("&7Refills a Portal Gun to 100 charge.", "", "&8» &fID: &e" + PORTAL_FLUID));
        register(PORTAL_GUN, Material.NETHERITE_HOE,
                "<gradient:#00ffcc:#0077ff><bold>Portal Gun</bold></gradient>",
                Arrays.asList(
                        "&7The Cake is a Lie.",
                        "&7Fires interdimensional quantum wormholes.",
                        "",
                        "&eRight-click: &fFire Portal",
                        "&eShift + Right-click: &fFire Secondary Portal",
                        "&eShift + Drop: &fRandomize Portal Colors",
                        "",
                        "&8» &fID: &e" + PORTAL_GUN
                ));

        register(GRAVITY_GUN, Material.GOLDEN_HOE,
                "<gradient:#ffaa00:#ff5500><bold>Gravity Gun</bold></gradient>",
                Arrays.asList(
                        "&7Manipulates zero-point energy fields.",
                        "&7Picks up and launches entities or blocks.",
                        "",
                        "&eRight-click Entity: &fGrab / Release",
                        "&eRight-click Air: &fLaunch Held Entity",
                        "&eDrop: &fSafely Release",
                        "",
                        "&8» &fID: &e" + GRAVITY_GUN
                ));
    }

    private static void register(String id, Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, name);
            CrossVersionAdapter.setLore(meta, lore);
            SuiteItemPdcBridge.setSlimefunId(meta, id);
            item.setItemMeta(meta);
        }
        ITEMS.put(id, item);
    }

    @Nullable
    public static ItemStack getItem(String id) {
        ItemStack item = ITEMS.get(id);
        return item != null ? item.clone() : null;
    }

    public static Map<String, ItemStack> getAllItems() {
        return Collections.unmodifiableMap(ITEMS);
    }

    private PortalGunItems() {}
}
