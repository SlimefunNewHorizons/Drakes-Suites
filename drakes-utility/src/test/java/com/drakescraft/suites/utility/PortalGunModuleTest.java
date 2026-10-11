package com.drakescraft.suites.utility;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import com.drakescraft.suites.utility.portalgun.PortalGunItems;
import com.drakescraft.suites.utility.portalgun.PortalGunModule;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class PortalGunModuleTest {

    private ServerMock server;
    private DrakesUtilityPlugin plugin;
    private PortalGunModule module;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesUtilityPlugin.class);
        module = (PortalGunModule) plugin.getModuleManager().getModule("portalgun");
        assertNotNull(module, "PortalGunModule must be registered in ModuleManager");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("PortalGun module must initialize and be enabled")
    void testModuleEnabled() {
        assertEquals("portalgun", module.getId());
        assertTrue(module.isEnabled());
        assertNotNull(module.getPortalGunItem());
        assertNotNull(module.getGravityGunItem());
    }

    @Test
    @DisplayName("Portal Gun and Gravity Gun must be registered with canonical PDC")
    void testItemsRegistered() {
        ItemStack pg = PortalGunItems.getItem("PORTAL_GUN");
        assertNotNull(pg);
        assertEquals("PORTAL_GUN", SuiteItemPdcBridge.getSlimefunId(pg));

        ItemStack gg = PortalGunItems.getItem("GRAVITY_GUN");
        assertNotNull(gg);
        assertEquals("GRAVITY_GUN", SuiteItemPdcBridge.getSlimefunId(gg));
    }

    @Test
    @DisplayName("Portal Gun carried over from SFPortalGunsExtension keeps its pair id and colors")
    void testLegacyStandalonePdcIsHonored() {
        ItemStack pg = PortalGunItems.getItem("PORTAL_GUN");
        ItemMeta meta = pg.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("sfportalgunsextension", "unique_portals_id"), PersistentDataType.STRING, "legacy-pair-id");
        meta.getPersistentDataContainer().set(new NamespacedKey("sfportalgunsextension", "portal_color_one"), PersistentDataType.STRING, "10;20;30");
        meta.getPersistentDataContainer().set(new NamespacedKey("sfportalgunsextension", "portal_color_two"), PersistentDataType.STRING, "40;50;60");
        pg.setItemMeta(meta);

        assertEquals("legacy-pair-id", module.getPortalGunItem().getPortalGunId(pg));
        assertEquals("10;20;30", module.getPortalGunItem().getPortal1Color(pg));
        assertEquals("40;50;60", module.getPortalGunItem().getPortal2Color(pg));
    }

    @Test
    @DisplayName("Suite PDC wins over legacy keys once written (e.g. after randomizing colors)")
    void testNativePdcWinsOverLegacy() {
        ItemStack pg = PortalGunItems.getItem("PORTAL_GUN");
        ItemMeta meta = pg.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("sfportalgunsextension", "portal_color_one"), PersistentDataType.STRING, "10;20;30");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pg_1color"), PersistentDataType.STRING, "1;2;3");
        pg.setItemMeta(meta);

        assertEquals("1;2;3", module.getPortalGunItem().getPortal1Color(pg));
    }

    @Test
    @DisplayName("Fresh Portal Gun gets a stable generated id")
    void testFreshGunGetsStableId() {
        ItemStack pg = PortalGunItems.getItem("PORTAL_GUN");
        String id = module.getPortalGunItem().getPortalGunId(pg);
        assertNotNull(id);
        assertEquals(id, module.getPortalGunItem().getPortalGunId(pg));
    }
}
