package com.drakescraft.suites.combat;

import com.drakescraft.suites.combat.luckyblocks.LuckyBlocksModule;
import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class LuckyBlocksModuleTest {

    private ServerMock server;
    private DrakesCombatPlugin plugin;
    private LuckyBlocksModule module;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesCombatPlugin.class);
        module = (LuckyBlocksModule) plugin.getModuleManager().getModule("luckyblocks");
        assertNotNull(module, "LuckyBlocksModule must be registered in ModuleManager");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("LuckyBlocksModule initializes and loads all default surprises")
    void testModuleEnabledAndSurprisesLoaded() {
        assertEquals("luckyblocks", module.getId());
        assertTrue(module.isEnabled());
        assertEquals(51, module.getSurprises().size(), "Must register all 51 default surprises across lucky, neutral, unlucky, and pandora levels");

        assertTrue(module.getSurprises().stream().anyMatch(s -> s.getLuckLevel() == LuckLevel.LUCKY));
        assertTrue(module.getSurprises().stream().anyMatch(s -> s.getLuckLevel() == LuckLevel.NEUTRAL));
        assertTrue(module.getSurprises().stream().anyMatch(s -> s.getLuckLevel() == LuckLevel.UNLUCKY));
        assertTrue(module.getSurprises().stream().anyMatch(s -> s.getLuckLevel() == LuckLevel.PANDORA));
    }
}
