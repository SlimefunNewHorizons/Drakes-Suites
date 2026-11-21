package com.drakescraft.suites.utility;

import com.drakescraft.suites.utility.soundmuffler.SoundMufflerModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

class SoundMufflerModuleTest {

    private ServerMock server;
    private DrakesUtilityPlugin plugin;
    private SoundMufflerModule module;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesUtilityPlugin.class);
        module = (SoundMufflerModule) plugin.getModuleManager().getModule("soundmuffler");
        assertNotNull(module, "SoundMufflerModule must be registered");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Closing SoundMuffler GUI must release inventory click lock for other inventories")
    void testClosingGuiReleasesInventoryLock() {
        WorldMock world = server.addSimpleWorld("muffler_world");
        PlayerMock player = server.addPlayer("MufflerUser");
        Block block = world.getBlockAt(0, 64, 0);
        block.setType(Material.WHITE_CONCRETE);

        ItemStack mufflerItem = module.createMufflerItem();
        BlockPlaceEvent placeEvent = new BlockPlaceEvent(
                block, block.getState(), world.getBlockAt(0, 63, 0),
                mufflerItem, player, true, EquipmentSlot.HAND
        );
        module.onBlockPlace(placeEvent);

        // Open muffler GUI
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR),
                block, BlockFace.UP, EquipmentSlot.HAND
        );
        module.onInteract(interactEvent);
        assertTrue(interactEvent.isCancelled());

        // Close muffler GUI
        player.closeInventory();

        // Open an unrelated chest inventory and click inside it
        Inventory chest = server.createInventory(null, 27);
        chest.setItem(0, new ItemStack(Material.DIAMOND, 1));
        InventoryView view = player.openInventory(chest);

        InventoryClickEvent clickEvent = new InventoryClickEvent(
                view, InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL
        );
        module.onInventoryClick(clickEvent);

        assertFalse(clickEvent.isCancelled(),
                "Tras cerrar el GUI de SoundMuffler, los clicks en otros inventarios NO deben quedar bloqueados");
    }
}
