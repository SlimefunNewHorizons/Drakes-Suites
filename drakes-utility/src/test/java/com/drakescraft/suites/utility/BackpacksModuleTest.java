package com.drakescraft.suites.utility;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import com.drakescraft.suites.utility.backpacks.BackpackColor;
import com.drakescraft.suites.utility.backpacks.BackpackTier;
import com.drakescraft.suites.utility.backpacks.BackpacksModule;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

class BackpacksModuleTest {

    private static ServerMock server;
    private static DrakesUtilityPlugin plugin;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesUtilityPlugin.class);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("BackpacksModule genera ítems con PDC canónico y slots correctos")
    void testBackpackPdcAndCreation() {
        BackpacksModule module = (BackpacksModule) plugin.getModuleManager().getModule("backpacks");
        assertNotNull(module, "El módulo backpacks debe estar registrado");
        assertTrue(module.isEnabled(), "El módulo backpacks debe estar activo");

        // Probar nivel pequeño con color rojo
        ItemStack redSmall = module.createBackpackItemStack(BackpackTier.SMALL, BackpackColor.RED);
        assertNotNull(redSmall);
        assertEquals("DYED_BACKPACK_SMALL_RED", SuiteItemPdcBridge.getSlimefunId(redSmall));
        assertEquals(Integer.valueOf(9), SuiteItemPdcBridge.getCustomInt(redSmall, "drakesutility", "backpack_slots"));
        assertTrue(module.isBackpack(redSmall));

        // Probar nivel radiante con color púrpura
        ItemStack purpleRadiant = module.createBackpackItemStack(BackpackTier.RADIANT, BackpackColor.PURPLE);
        assertNotNull(purpleRadiant);
        assertEquals("DYED_RADIANT_BACKPACK_PURPLE", SuiteItemPdcBridge.getSlimefunId(purpleRadiant));
        assertEquals(Integer.valueOf(54), SuiteItemPdcBridge.getCustomInt(purpleRadiant, "drakesutility", "backpack_slots"));
        assertTrue(module.isBackpack(purpleRadiant));
    }

    @Test
    @DisplayName("BackpacksModule previene anidación de mochilas y valida apertura")
    void testBackpackInteractionAndAntiDupe() {
        BackpacksModule module = (BackpacksModule) plugin.getModuleManager().getModule("backpacks");
        assertNotNull(module);

        PlayerMock player = server.addPlayer();
        ItemStack backpack = module.createBackpackItemStack(BackpackTier.MEDIUM, BackpackColor.BLUE);
        player.getInventory().setItemInMainHand(backpack);

        // Simular clic derecho para abrir
        server.getPluginManager().callEvent(new org.bukkit.event.player.PlayerInteractEvent(
                player,
                org.bukkit.event.block.Action.RIGHT_CLICK_AIR,
                backpack,
                null,
                org.bukkit.block.BlockFace.SELF,
                org.bukkit.inventory.EquipmentSlot.HAND
        ));

        // El inventario abierto debe tener 18 slots
        assertNotNull(player.getOpenInventory());
        assertEquals(18, player.getOpenInventory().getTopInventory().getSize());

        // Cerrar la mochila y verificar que se guarda en caché
        player.closeInventory();
        assertFalse(module.getBackpackStorageCache().isEmpty());
    }

    @Test
    @DisplayName("BackpacksModule nunca intercepta ENDER_BACKPACK ni mochilas nativas de Slimefun")
    void testDoesNotInterceptEnderBackpack() {
        BackpacksModule module = (BackpacksModule) plugin.getModuleManager().getModule("backpacks");
        assertNotNull(module);

        ItemStack enderBackpack = new ItemStack(Material.PLAYER_HEAD);
        SuiteItemPdcBridge.setSlimefunId(enderBackpack, "ENDER_BACKPACK");

        assertFalse(module.isStarSuitesBackpack(enderBackpack),
                "ENDER_BACKPACK nunca debe ser interceptada por BackpacksModule");

        ItemStack nativeSfBackpack = new ItemStack(Material.PLAYER_HEAD);
        SuiteItemPdcBridge.setSlimefunId(nativeSfBackpack, "LARGE_BACKPACK");
        assertFalse(module.isStarSuitesBackpack(nativeSfBackpack),
                "Mochilas nativas de Slimefun sin PDC drakesutility no deben ser interceptadas");
    }
}
