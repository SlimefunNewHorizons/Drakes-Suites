package com.drakescraft.suites.core;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import com.drakescraft.suites.core.world.SlimefunWorldFilter;
import com.drakescraft.suites.core.world.SlimefunWorldRestrictionListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlimefunWorldFilterTest {

    private static ServerMock server;
    private static DrakesCorePlugin plugin;
    private static SlimefunWorldFilter filter;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesCorePlugin.class);
        filter = plugin.getWorldFilter();
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Slimefun está deshabilitado en world_clasico y habilitado en world")
    void testWorldFiltering() {
        WorldMock normalWorld = server.addSimpleWorld("world");
        WorldMock clasicoWorld = server.addSimpleWorld("world_clasico");

        assertTrue(filter.isSlimefunAllowed(normalWorld), "Slimefun debe estar permitido en world");
        assertFalse(filter.isSlimefunAllowed(clasicoWorld), "Slimefun debe estar prohibido en world_clasico");
    }

    @Test
    @DisplayName("Colocar item de Slimefun en world_clasico es cancelado por el listener")
    void testBlockPlaceCancelledInClasico() {
        WorldMock clasicoWorld = server.addSimpleWorld("world_clasico");
        PlayerMock player = server.addPlayer();
        player.teleport(clasicoWorld.getSpawnLocation());

        ItemStack sfItem = new ItemStack(Material.DISPENSER);
        ItemMeta meta = sfItem.getItemMeta();
        meta.getPersistentDataContainer().set(SuiteItemPdcBridge.SLIMEFUN_ITEM_KEY, PersistentDataType.STRING, "ELECTRIC_FURNACE");
        sfItem.setItemMeta(meta);

        Block block = clasicoWorld.getBlockAt(0, 64, 0);

        BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block, sfItem, player, true, EquipmentSlot.HAND);
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "Colocar máquina Slimefun en world_clasico debe ser cancelado");
    }

    @ParameterizedTest
    @ValueSource(strings = {"clasico", "clasico_nether", "clasico_the_end",
            "world_clasico", "world_clasico_nether", "world_clasico_the_end"})
    void testCurrentAndLegacyClassicDimensions(String worldName) {
        assertFalse(filter.isSlimefunAllowed(worldName), worldName);
    }

    @Test
    @DisplayName("Colocar bloque vanilla en world_clasico NO es cancelado")
    void testVanillaBlockPlaceAllowedInClasico() {
        WorldMock clasicoWorld = server.addSimpleWorld("world_clasico");
        PlayerMock player = server.addPlayer();
        player.teleport(clasicoWorld.getSpawnLocation());

        ItemStack vanillaItem = new ItemStack(Material.OAK_PLANKS);
        Block block = clasicoWorld.getBlockAt(0, 65, 0);

        BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block, vanillaItem, player, true, EquipmentSlot.HAND);
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "Colocar bloque vanilla común no debe ser cancelado");
    }
}
