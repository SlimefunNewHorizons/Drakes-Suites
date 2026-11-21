package com.drakescraft.suites.utility;

import com.drakescraft.suites.utility.endercarryon.CarryHelper;
import com.drakescraft.suites.utility.endercarryon.CarryKeys;
import com.drakescraft.suites.utility.endercarryon.EnderCarryOnModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

class EnderCarryOnModuleTest {

    private ServerMock server;
    private DrakesUtilityPlugin plugin;
    private EnderCarryOnModule module;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesUtilityPlugin.class);
        module = (EnderCarryOnModule) plugin.getModuleManager().getModule("endercarryon");
        assertNotNull(module, "EnderCarryOnModule must be registered in ModuleManager");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("EnderCarryOn module must initialize and be enabled")
    void testModuleEnabled() {
        assertEquals("endercarryon", module.getId());
        assertTrue(module.isEnabled());
        assertTrue(module.isAllowEntities());
        assertTrue(module.isSlowDownPlayer());
    }

    @Test
    @DisplayName("Carried block must contain PDC tag and custom model data")
    void testCarryBlockItemCreation() {
        WorldMock world = server.addSimpleWorld("test_world");
        Block chestBlock = world.getBlockAt(0, 64, 0);
        chestBlock.setType(Material.CHEST);

        ItemStack carryItem = CarryHelper.getCarryBlock(chestBlock);
        assertNotNull(carryItem);
        assertEquals(Material.CHEST, carryItem.getType());
        assertTrue(carryItem.hasItemMeta());
        assertTrue(carryItem.getItemMeta().getPersistentDataContainer().has(CarryKeys.CARRY, PersistentDataType.BYTE));
        assertTrue(EnderCarryOnModule.isCarriedItem(carryItem));
    }

    @Test
    @DisplayName("EnderCarryOn rejects left-click, custom PDC containers, and chests currently viewed by another player")
    void testCarrySafetyGuards() {
        WorldMock world = server.addSimpleWorld("carry_safety_world");
        var playerA = server.addPlayer("CarryPlayerA");
        var playerB = server.addPlayer("CarryPlayerB");
        playerB.setSneaking(true);

        Block chestBlock = world.getBlockAt(10, 64, 10);
        chestBlock.setType(Material.CHEST);

        // 1. Left-click must NOT be a valid carry attempt
        var leftClick = new org.bukkit.event.player.PlayerInteractEvent(
                playerB,
                org.bukkit.event.block.Action.LEFT_CLICK_BLOCK,
                new ItemStack(Material.AIR),
                chestBlock,
                org.bukkit.block.BlockFace.UP,
                org.bukkit.inventory.EquipmentSlot.HAND
        );
        assertFalse(CarryHelper.isValidCarryAttempt(leftClick, plugin),
                "LEFT_CLICK_BLOCK nunca debe recoger un cofre");

        // 2. Chest with an active viewer must NOT be safe to carry (prevents 2-player dupe)
        org.bukkit.block.Chest chestState = (org.bukkit.block.Chest) chestBlock.getState();
        playerA.openInventory(chestState.getInventory());
        assertFalse(CarryHelper.isSafeContainerToCarry(chestBlock),
                "Un cofre con jugadores viéndolo no debe poder recogerse");
        playerA.closeInventory();
        assertTrue(CarryHelper.isSafeContainerToCarry(chestBlock));

        // 3. TileState with custom PDC keys must NOT be carried
        chestState = (org.bukkit.block.Chest) chestBlock.getState();
        chestState.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "custom_tag"),
                PersistentDataType.STRING,
                "protected"
        );
        chestState.update();
        assertTrue(CarryHelper.isSlimefunOrCustomTileBlock(chestBlock),
                "Un bloque con PDC personalizado debe ser rechazado por CarryHelper");
    }

    @Test
    @DisplayName("Picking up a chest clears live block inventory before removal to prevent drop+carry duplication")
    void testCarryClearsLiveContainerBeforeAirRemoval() {
        WorldMock world = server.addSimpleWorld("carry_nodup_world");
        var player = server.addPlayer("CarryNoDupPlayer");
        player.setSneaking(true);
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        Block chestBlock = world.getBlockAt(20, 64, 20);
        chestBlock.setType(Material.CHEST);
        org.bukkit.block.Chest chestState = (org.bukkit.block.Chest) chestBlock.getState();
        chestState.getInventory().setItem(0, new ItemStack(Material.DIAMOND, 16));
        chestState.update();

        var rightClick = new org.bukkit.event.player.PlayerInteractEvent(
                player,
                org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.AIR),
                chestBlock,
                org.bukkit.block.BlockFace.UP,
                org.bukkit.inventory.EquipmentSlot.HAND
        );
        module.onPlayerInteract(rightClick);

        assertTrue(rightClick.isCancelled(), "Carry pickup must cancel the interact event");
        assertEquals(Material.AIR, chestBlock.getType(), "Original chest block must be replaced with AIR");
        assertTrue(chestState.getInventory().isEmpty(),
                "Live container inventory must be cleared before setType(AIR) so vanilla does not drop a duplicate copy");

        ItemStack held = player.getInventory().getItemInMainHand();
        assertTrue(EnderCarryOnModule.isCarriedItem(held), "Player must hold the carried chest item");
    }
}
