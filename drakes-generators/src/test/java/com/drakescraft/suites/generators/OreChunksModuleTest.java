package com.drakescraft.suites.generators;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import com.drakescraft.suites.generators.orechunks.OreChunkType;
import com.drakescraft.suites.generators.orechunks.OreChunksModule;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class OreChunksModuleTest {

    private static ServerMock server;
    private static DrakesGeneratorsPlugin plugin;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesGeneratorsPlugin.class);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("OreChunksModule genera ítems con PDC canónico y material de cabeza")
    void testOreChunkItemCreation() {
        OreChunksModule module = (OreChunksModule) plugin.getModuleManager().getModule("ore_chunks");
        assertNotNull(module, "El módulo ore_chunks debe estar registrado");
        assertTrue(module.isEnabled(), "El módulo ore_chunks debe estar habilitado");

        for (OreChunkType type : OreChunkType.values()) {
            ItemStack item = module.getOreChunkItem(type);
            assertNotNull(item, "El ítem de " + type.name() + " no debe ser nulo");
            assertEquals(Material.PLAYER_HEAD, item.getType(), "Debe ser una cabeza de jugador");
            assertEquals(type.getSlimefunId(), SuiteItemPdcBridge.getSlimefunId(item), "El PDC debe coincidir con el Slimefun ID canónico");
            assertTrue(SuiteItemPdcBridge.hasSlimefunId(item, type.getSlimefunId()));
        }
    }

    @Test
    @DisplayName("OreChunksModule nunca suelta OreChunks cuando se mina con Silk Touch")
    void testSilkTouchDoesNotDropOreChunks() {
        OreChunksModule module = (OreChunksModule) plugin.getModuleManager().getModule("ore_chunks");
        assertNotNull(module);

        var world = server.addSimpleWorld("ore_test_world");
        var player = server.addPlayer("SilkMiner");
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);

        ItemStack silkPick = new ItemStack(Material.DIAMOND_PICKAXE);
        silkPick.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH, 1);
        player.getInventory().setItemInMainHand(silkPick);

        var block = world.getBlockAt(0, 64, 0);
        block.setType(Material.IRON_ORE);

        for (int i = 0; i < 100; i++) {
            module.onOreMine(new org.bukkit.event.block.BlockBreakEvent(block, player));
        }

        assertTrue(world.getEntitiesByClass(org.bukkit.entity.Item.class).isEmpty(),
                "Minar con Silk Touch nunca debe generar OreChunks adicionales");
    }
}
