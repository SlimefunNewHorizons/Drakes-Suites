package com.drakescraft.suites.utility.portalgun;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class PortalGunStateTest {
    private ServerMock server;

    @BeforeEach
    void setUp() { server = MockBukkit.mock(); }

    @AfterEach
    void tearDown() { MockBukkit.unmock(); }

    @Test
    void preservesStandaloneNamespaceForChargeAndSavedDestination() {
        ItemStack gun = PortalGunItems.getItem(PortalGunItems.PORTAL_GUN);
        ItemMeta meta = gun.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("sfportalgunsextension", "portal_charge"), PersistentDataType.INTEGER, 37);
        gun.setItemMeta(meta);
        assertEquals(37, PortalGunState.charge(gun));

        World world = server.addSimpleWorld("portal-state-test");
        assertTrue(PortalGunState.saveLocation(gun, new Location(world, 10.5, 70, -4.5, 90, 0)));
        Location saved = PortalGunState.savedLocation(gun, 0);
        assertNotNull(saved);
        assertEquals(world, saved.getWorld());
        assertEquals(10.5, saved.getX());
        assertEquals(0, PortalGunState.selectedSlot(gun));
    }

    @Test
    void consumesChargeUnlessInfiniteModeIsEnabled() {
        ItemStack gun = PortalGunItems.getItem(PortalGunItems.PORTAL_GUN);
        PortalGunState.setCharge(gun, 10);
        assertTrue(PortalGunState.consume(gun, 5));
        assertEquals(5, PortalGunState.charge(gun));
        PortalGunState.setInfinite(gun, true);
        assertTrue(PortalGunState.consume(gun, 5));
        assertEquals(5, PortalGunState.charge(gun));
    }
}
