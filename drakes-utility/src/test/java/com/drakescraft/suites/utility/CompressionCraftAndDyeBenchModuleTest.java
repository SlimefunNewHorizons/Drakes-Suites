package com.drakescraft.suites.utility;

import com.drakescraft.suites.utility.compressioncraft.CompressionCraftModule;
import com.drakescraft.suites.utility.dyebench.DyeBenchModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class CompressionCraftAndDyeBenchModuleTest {

    private ServerMock server;
    private DrakesUtilityPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesUtilityPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("CompressionCraftModule and DyeBenchModule are registered and enabled in DrakesUtility")
    void testModulesRegisteredAndEnabled() {
        CompressionCraftModule cc = (CompressionCraftModule) plugin.getModuleManager().getModule("compressioncraft");
        assertNotNull(cc, "CompressionCraftModule must be registered");
        assertTrue(cc.isEnabled(), "CompressionCraftModule must be enabled");
        assertSame(plugin, cc.getJavaPlugin());

        DyeBenchModule db = (DyeBenchModule) plugin.getModuleManager().getModule("dyebench");
        assertNotNull(db, "DyeBenchModule must be registered");
        assertTrue(db.isEnabled(), "DyeBenchModule must be enabled");
        assertSame(plugin, db.getJavaPlugin());
    }
}
