package com.drakescraft.suites.utility;

import com.drakescraft.suites.utility.simpleutils.SimpleUtilsModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
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

class SimpleUtilsModuleTest {

    private ServerMock server;
    private DrakesUtilityPlugin plugin;
    private SimpleUtilsModule module;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesUtilityPlugin.class);
        module = (SimpleUtilsModule) plugin.getModuleManager().getModule("simpleutils");
        assertNotNull(module, "SimpleUtilsModule must be registered in ModuleManager");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Right-clicking a block with SIMPLE_WORKBENCH must NOT be cancelled so the block can be placed")
    void testWorkbenchCanBePlacedOnBlock() {
        WorldMock world = server.addSimpleWorld("world");
        PlayerMock player = server.addPlayer();
        Block block = world.getBlockAt(0, 64, 0);
        block.setType(Material.STONE);

        ItemStack workbench = module.createWorkbenchItem();
        PlayerInteractEvent event = new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                workbench,
                block,
                BlockFace.UP,
                EquipmentSlot.HAND
        );

        module.onWorkbenchInteract(event);

        assertFalse(event.isCancelled(), "RIGHT_CLICK_BLOCK must not be cancelled so SIMPLE_WORKBENCH can be placed");
    }
}
