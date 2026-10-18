package com.drakescraft.suites.combat;

import com.drakescraft.suites.combat.nukes.NukeWarheadType;
import com.drakescraft.suites.combat.nukes.SlimefunNukesModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class SlimefunNukesModuleTest {

    private static ServerMock server;
    private static DrakesCombatPlugin plugin;
    private static SlimefunNukesModule module;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesCombatPlugin.class);
        module = (SlimefunNukesModule) plugin.getModuleManager().getModule("slimefun_nukes");
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("SlimefunNukesModule está registrado y habilitado en DrakesCombat")
    void testModuleEnabled() {
        assertNotNull(module, "SlimefunNukesModule debe estar registrado");
        assertTrue(module.isEnabled(), "SlimefunNukesModule debe estar habilitado");
        assertTrue(module.isAsyncBlastRadiusEnabled(), "Cálculo asíncrono de radio debe estar activo");
        assertTrue(module.isSafeZoneShielding(), "Protección de zonas seguras debe estar activa");
    }

    @Test
    @DisplayName("NukeWarheadType contiene Little Nuke y Medium Nuke con PDC canónico")
    void testNukeWarheadTypes() {
        assertEquals(2, NukeWarheadType.values().length);
        
        NukeWarheadType little = NukeWarheadType.getBySlimefunId("LITTLE_NUKE");
        assertNotNull(little);
        assertEquals(16, little.getRadius());
        assertEquals(30, little.getFuseSeconds());
        assertFalse(little.isIncendiary());

        NukeWarheadType medium = NukeWarheadType.getBySlimefunId("MEDIUM_NUKE");
        assertNotNull(medium);
        assertEquals(32, medium.getRadius());
        assertEquals(45, medium.getFuseSeconds());
        assertTrue(medium.isIncendiary());
    }

    @Test
    @DisplayName("SlimefunNukesModule genera ítems con PDC canónico y metadatos correctos")
    void testNukeItemGeneration() {
        ItemStack littleItem = module.getWarheadItem(NukeWarheadType.LITTLE_NUKE);
        assertNotNull(littleItem);
        assertEquals(Material.TNT, littleItem.getType());
        assertEquals("LITTLE_NUKE", SuiteItemPdcBridge.getSlimefunId(littleItem));
        assertTrue(module.isNukeWarhead(littleItem));

        ItemStack mediumItem = module.getWarheadItem(NukeWarheadType.MEDIUM_NUKE);
        assertNotNull(mediumItem);
        assertEquals(Material.TNT, mediumItem.getType());
        assertEquals("MEDIUM_NUKE", SuiteItemPdcBridge.getSlimefunId(mediumItem));
        assertTrue(module.isNukeWarhead(mediumItem));
    }
}
