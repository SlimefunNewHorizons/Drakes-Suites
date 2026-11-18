package com.drakescraft.suites.utility;

import com.drakescraft.suites.utility.endercarryon.CarryHelper;
import com.drakescraft.suites.utility.endercarryon.CarryKeys;
import com.drakescraft.suites.utility.endercarryon.EnderCarryOnModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityFactory;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        server = MockBukkit.mock(new SnapshotServerMock());
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
    void failedEntityRestoreKeepsLegacyCarrier() {
        assertFailedRestoreKeepsCarrier(false);
    }

    @Test
    void failedEntityRestoreKeepsBinaryCarrier() {
        assertFailedRestoreKeepsCarrier(true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void consumesCarrierOnlyWhenReturnedEntityIsValid(boolean rejected) {
        WorldMock world = server.addSimpleWorld("spawn_world");
        Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.PIG);
        if (rejected) entity.remove();
        ((SnapshotServerMock) server).entityFactory = input -> new EntitySnapshot() {
            public Entity createEntity(World target) { return entity; }
            public Entity createEntity(Location target) { return entity; }
            public EntityType getEntityType() { return EntityType.PIG; }
            public String getAsString() { return input; }
        };
        Player player = server.addPlayer();
        ItemStack carrier = new ItemStack(Material.CHEST);
        var meta = carrier.getItemMeta();
        meta.getPersistentDataContainer().set(CarryKeys.CARRY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(CarryKeys.ENTITY_DATA, PersistentDataType.STRING, "{id:\"minecraft:pig\"}");
        carrier.setItemMeta(meta);
        player.getInventory().setItemInMainHand(carrier);
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, carrier,
                world.getBlockAt(0, 64, 0), BlockFace.UP, EquipmentSlot.HAND);
        module.onPlayerInteract(event);
        assertTrue(event.isCancelled());
        if (rejected) assertEquals(carrier, player.getInventory().getItemInMainHand());
        else assertTrue(player.getInventory().getItemInMainHand().getType().isAir());
    }

    private void assertFailedRestoreKeepsCarrier(boolean binary) {
        WorldMock world = server.addSimpleWorld("restore_world");
        Player player = server.addPlayer();
        ItemStack carrier = new ItemStack(Material.CHEST);
        var meta = carrier.getItemMeta();
        meta.getPersistentDataContainer().set(CarryKeys.CARRY, PersistentDataType.BYTE, (byte) 1);
        if (binary) {
            meta.getPersistentDataContainer().set(CarryKeys.ENTITY_DATA, PersistentDataType.BYTE_ARRAY, new byte[]{(byte) 0xC3, 0x28});
        } else {
            meta.getPersistentDataContainer().set(CarryKeys.ENTITY_DATA, PersistentDataType.STRING, "invalid snapshot");
        }
        carrier.setItemMeta(meta);
        player.getInventory().setItemInMainHand(carrier);
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, carrier,
                world.getBlockAt(0, 64, 0), BlockFace.UP, EquipmentSlot.HAND);
        module.onPlayerInteract(event);
        assertTrue(event.isCancelled());
        assertEquals(carrier, player.getInventory().getItemInMainHand());
    }

    private static class SnapshotServerMock extends ServerMock {
        private EntityFactory entityFactory;

        @Override
        public EntityFactory getEntityFactory() {
            return entityFactory != null ? entityFactory : super.getEntityFactory();
        }
    }
}
