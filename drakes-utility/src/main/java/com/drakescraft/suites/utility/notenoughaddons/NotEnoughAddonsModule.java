package com.drakescraft.suites.utility.notenoughaddons;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

/**
 * NotEnoughAddonsModule - Integration of BudgetDustFabricator, FlyingBubble, AngelBlock, MinerBackpack, and Terraria Shortswords.
 */
public class NotEnoughAddonsModule extends AbstractSuiteModule implements Listener {

    private MinerBackpackListener minerBackpackListener;
    private TerrariaShortswordListener shortswordListener;
    private AngelBlock angelBlock;
    private FlyingBubble flyingBubble;

    public NotEnoughAddonsModule(@Nonnull JavaPlugin plugin) {
        super(plugin, "notenoughaddons", "NotEnoughAddons Expansion & Terraria Tools");
    }

    static boolean isStandaloneNotEnoughAddonsPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("NotEnoughAddons") != null;
    }

    @Override
    public void onEnable() {
        this.angelBlock = new AngelBlock(getPlugin());
        this.flyingBubble = new FlyingBubble();

        if (isStandaloneNotEnoughAddonsPresent()) {
            logInfo("Plugin standalone NotEnoughAddons detectado; cediendo listeners al addon dedicado.");
            return;
        }

        this.minerBackpackListener = new MinerBackpackListener(getPlugin());
        this.shortswordListener = new TerrariaShortswordListener();

        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        Bukkit.getPluginManager().registerEvents(minerBackpackListener, getPlugin());
        Bukkit.getPluginManager().registerEvents(shortswordListener, getPlugin());

        logInfo("Module enabled with all machines, items, and weapons (" + NEAItems.getAllItems().size() + " items).");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled() || isStandaloneNotEnoughAddonsPresent()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return;

        String sfId = SuiteItemPdcBridge.getSlimefunId(item);
        if (sfId == null) return;

        Player player = event.getPlayer();
        if (NEAItems.ANGEL_BLOCK.equals(sfId)) {
            if (angelBlock != null && angelBlock.use(player)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (minerBackpackListener != null) {
            HandlerList.unregisterAll(minerBackpackListener);
            minerBackpackListener = null;
        }
        if (shortswordListener != null) {
            HandlerList.unregisterAll(shortswordListener);
            shortswordListener = null;
        }
        HandlerList.unregisterAll(this);
        AngelBlock.cleanup();
        FlyingBubble.cleanup();
        logInfo("Module disabled cleanly.");
    }

    public AngelBlock getAngelBlock() {
        return angelBlock;
    }

    public FlyingBubble getFlyingBubble() {
        return flyingBubble;
    }
}
