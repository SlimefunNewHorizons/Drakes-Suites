package com.chagui68.multiversenets;

import com.chagui68.multiversenets.compat.SlimefunBridge;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests verifying SlimefunBridge fallback behavior when Slimefun is absent.
 *
 * Pruebas unitarias que verifican el comportamiento seguro del SlimefunBridge cuando Slimefun no está presente.
 */
class SlimefunBridgeTest {

    @Test
    void withoutSlimefunBridgeIsUnavailable() {
        assertFalse(SlimefunBridge.isAvailable(),
                "Without init() and without server, bridge must report unavailable");
        assertFalse(SlimefunBridge.disponible(),
                "Backward compatibility alias must report unavailable");
    }

    @Test
    void queryingNullBlockOrItemDoesNotThrow() {
        assertFalse(SlimefunBridge.isMachine(null));
        assertNull(SlimefunBridge.getId((org.bukkit.block.Block) null));
        assertNull(SlimefunBridge.getId((org.bukkit.inventory.ItemStack) null));
        assertFalse(SlimefunBridge.isSlimefunItem(null));

        // Alias verification
        assertFalse(SlimefunBridge.esMaquina(null));
        assertNull(SlimefunBridge.idDe((org.bukkit.block.Block) null));
        assertNull(SlimefunBridge.idDe((org.bukkit.inventory.ItemStack) null));
        assertFalse(SlimefunBridge.esItemSlimefun(null));
    }

    @Test
    void getIdPreservesLegacyNamespaceFallback() {
        MockBukkit.mock();
        try {
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_INGOT);
            var meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey("legacy_slimefun", "slimefun_item"),
                    org.bukkit.persistence.PersistentDataType.STRING, "LEGACY_STEEL_INGOT");
            item.setItemMeta(meta);

            assertEquals("LEGACY_STEEL_INGOT", SlimefunBridge.getId(item));
        } finally {
            MockBukkit.unmock();
        }
    }

    @Test
    void extractWithoutSlimefunReturnsNullSafely() {
        assertNull(SlimefunBridge.extract(null, item -> true, 64));
        assertNull(SlimefunBridge.extract(null, null, 0));

        // Alias verification
        assertNull(SlimefunBridge.extraer(null, item -> true, 64));
    }

    @Test
    void insertWithoutSlimefunHandlesNullSafely() {
        org.bukkit.inventory.ItemStack nullItem = null;
        assertEquals(0, SlimefunBridge.insert(null, nullItem));
        assertEquals(0, SlimefunBridge.insertar(null, nullItem));
    }
}
