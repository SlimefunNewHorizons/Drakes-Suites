package com.drakescraft.suites.server.compensation;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompensationModuleTest {

    @Test
    void parseBatchSpecNormalizesAndMergesValidEntries() {
        Map<String, Integer> parsed = CompensationItemResolver.parseBatchSpec(
            "fm_fast_smeltery:2, AUTO_ENCHANTER:1, fm_fast_smeltery:1, ADVANCED_ANVIL"
        );
        assertEquals(3, parsed.get("FM_FAST_SMELTERY"));
        assertEquals(1, parsed.get("AUTO_ENCHANTER"));
        assertEquals(1, parsed.get("ADVANCED_ANVIL"));
    }

    @Test
    void parseBatchSpecRejectsInvalidQuantitiesAndEmptySpecs() {
        assertThrows(IllegalArgumentException.class, () -> CompensationItemResolver.parseBatchSpec(""));
        assertThrows(IllegalArgumentException.class, () -> CompensationItemResolver.parseBatchSpec("AUTO_ENCHANTER:0"));
        assertThrows(IllegalArgumentException.class, () -> CompensationItemResolver.parseBatchSpec("AUTO_ENCHANTER:-3"));
        assertThrows(IllegalArgumentException.class, () -> CompensationItemResolver.parseBatchSpec("AUTO_ENCHANTER:abc"));
    }

    @Test
    void isWorldAllowedBlocksLaboratorioAndAllowsSurvivalWorlds() {
        assertTrue(CompensationStore.isWorldAllowed("world"));
        assertTrue(CompensationStore.isWorldAllowed("world_nether"));
        assertTrue(CompensationStore.isWorldAllowed("world_the_end"));
        assertTrue(CompensationStore.isWorldAllowed("world_yggdrasil_runic"));

        assertFalse(CompensationStore.isWorldAllowed("laboratorio"));
        assertFalse(CompensationStore.isWorldAllowed("laboratorio_nether"));
        assertFalse(CompensationStore.isWorldAllowed("lobby"));
        assertFalse(CompensationStore.isWorldAllowed("bskyblock_world"));
        assertFalse(CompensationStore.isWorldAllowed("oneblock_world"));
        assertFalse(CompensationStore.isWorldAllowed(null));
    }

    @Test
    void storePersistsMigratesNickToUuidAndAudits(@TempDir Path tempDir) throws Exception {
        ServerMock server = MockBukkit.mock();
        try {
            CompensationStore store = new CompensationStore(tempDir);
            String nickKey = CompensationStore.keyForNick("Sebass2002");
            UUID uuid = UUID.randomUUID();
            String uuidKey = CompensationStore.keyForUuid(uuid);

            List<ItemStack> batch = CompensationItemResolver.resolveBatch(
                CompensationItemResolver.parseBatchSpec("DIAMOND:5,EMERALD:2"),
                id -> new ItemStack(Material.valueOf(id), 1)
            );
            assertEquals(2, batch.size());

            store.addItems(nickKey, "Sebass2002", "CONSOLE", "Bug BlockStorage", batch);
            assertEquals(2, store.loadItems(nickKey).size());
            assertTrue(Files.exists(tempDir.resolve("compensations-audit.log")));

            int migrated = store.migrateNickToUuid("Sebass2002", uuid);
            assertEquals(2, migrated);
            assertTrue(store.loadItems(nickKey).isEmpty());
            assertEquals(2, store.loadItems(uuidKey).size());

            // Simular retiro parcial (1 stack reclamado, 1 pendiente)
            store.replaceItems(uuidKey, "Sebass2002", "Sebass2002", List.of(batch.get(1)), 1);
            assertEquals(1, store.loadItems(uuidKey).size());

            // Simular retiro total
            store.replaceItems(uuidKey, "Sebass2002", "Sebass2002", List.of(), 1);
            assertTrue(store.loadItems(uuidKey).isEmpty());
            assertFalse(Files.exists(store.fileForKey(uuidKey)));
        } finally {
            MockBukkit.unmock();
        }
    }

    @Test
    void resolveBatchIsAtomicWhenAnyItemIsUnknown() {
        MockBukkit.mock();
        try {
            Map<String, Integer> spec = CompensationItemResolver.parseBatchSpec("VALID_ITEM:2,UNKNOWN_ITEM:1");
            assertThrows(IllegalArgumentException.class, () ->
                CompensationItemResolver.resolveBatch(spec, id -> id.equals("VALID_ITEM") ? new ItemStack(Material.STONE) : null)
            );
        } finally {
            MockBukkit.unmock();
        }
    }
}
