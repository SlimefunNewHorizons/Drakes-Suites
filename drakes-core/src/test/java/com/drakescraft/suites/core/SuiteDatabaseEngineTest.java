package com.drakescraft.suites.core;

import com.drakescraft.suites.core.database.SuiteDatabaseEngine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import static org.junit.jupiter.api.Assertions.*;

class SuiteDatabaseEngineTest {

    private static ServerMock server;
    private static PluginMock plugin;
    private static SuiteDatabaseEngine db;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("DrakesTestPlugin");
        db = new SuiteDatabaseEngine(plugin);
        db.start();
    }

    @AfterAll
    static void tearDown() {
        if (db != null) {
            db.stop();
        }
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Persistencia y lectura de estados de bloques en SQLite WAL")
    void testBlockStatePersistence() {
        db.setBlockStateAsync("world", 100, 65, -200, "DrakesTech", "Networks", "NETWORKS_NODE", "{\"active\":true}");

        String state = awaitValue(() -> db.getBlockState("world", 100, 65, -200));
        assertNotNull(state);
        assertTrue(state.contains("active"));
    }

    @Test
    @DisplayName("Persistencia de Quantum Storage y balances masivos")
    void testQuantumStorage() {
        db.setQuantumStorageAsync("vault-alpha-1", "uuid-player-1", "INFINITY_SINGULARITY", 500000L, 10000000L);

        long amount = awaitValue(() -> db.getQuantumStorageAmount("vault-alpha-1"), 500000L);
        assertEquals(500000L, amount);
    }

    @Test
    @DisplayName("Persistencia de preferencias de jugador y key-value de modulo")
    void testPlayerDataAndKeyValue() {
        db.setPlayerDataAsync("uuid-jack-star", "DrakesServer", "selected_title", "§6[Dios Creador]");
        db.setKeyValueAsync("DrakesTech", "EnergyNet", "global_tps_limit", "19.8");

        assertEquals("§6[Dios Creador]", awaitValue(() -> db.getPlayerData("uuid-jack-star", "DrakesServer", "selected_title")));
        assertEquals("19.8", awaitValue(() -> db.getKeyValue("DrakesTech", "EnergyNet", "global_tps_limit")));
    }

    private static String awaitValue(java.util.function.Supplier<String> read) {
        return awaitValue(read, null);
    }

    private static <T> T awaitValue(java.util.function.Supplier<T> read, T expected) {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
        T value;
        do {
            value = read.get();
            if (expected == null ? value != null : expected.equals(value)) {
                return value;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                fail("Interrupcion esperando la persistencia asincrona", interrupted);
            }
        } while (System.nanoTime() < deadline);
        return value;
    }
}
