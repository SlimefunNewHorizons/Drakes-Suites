package com.drakescraft.suites.utility;

import com.drakescraft.suites.utility.endercarryon.CarriedEntityData;
import com.drakescraft.suites.utility.endercarryon.CarryKeys;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UTFDataFormatException;

import static org.junit.jupiter.api.Assertions.*;

class CarriedEntityDataTest {
    private PersistentDataContainer container;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        container = new ItemStack(Material.CHEST).getItemMeta().getPersistentDataContainer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "\u0000", "é", "界", "\uD83D\uDE00"})
    void switchesAtModifiedUtfLimitWithoutChangingSnapshot(String unit) throws Exception {
        int unitSize = modifiedUtf(unit).length - 2;
        String fits = unit.repeat(65535 / unitSize);
        CarriedEntityData.write(container, CarryKeys.ENTITY_DATA, fits);
        assertTrue(container.has(CarryKeys.ENTITY_DATA, PersistentDataType.STRING));
        assertEquals(fits, CarriedEntityData.read(container, CarryKeys.ENTITY_DATA));
        assertDoesNotThrow(() -> modifiedUtf(fits));

        String oversized = fits + unit;
        assertThrows(UTFDataFormatException.class, () -> modifiedUtf(oversized));
        CarriedEntityData.write(container, CarryKeys.ENTITY_DATA, oversized);
        assertFalse(container.has(CarryKeys.ENTITY_DATA, PersistentDataType.STRING));
        assertTrue(container.has(CarryKeys.ENTITY_DATA, PersistentDataType.BYTE_ARRAY));
        assertTrue(CarriedEntityData.has(container, CarryKeys.ENTITY_DATA));
        assertEquals(oversized, CarriedEntityData.read(container, CarryKeys.ENTITY_DATA));
    }

    @Test
    void observedSizeFailsLegacyEncodingButRoundTripsWithBinaryPdc() {
        String snapshot = "{id:\"minecraft:villager\",CustomName:\"" + "x".repeat(67048 - 39) + "\"}";
        assertEquals(67048, snapshot.length());
        assertThrows(UTFDataFormatException.class, () -> modifiedUtf(snapshot));
        CarriedEntityData.write(container, CarryKeys.ENTITY_DATA, snapshot);
        assertEquals(snapshot, CarriedEntityData.read(container, CarryKeys.ENTITY_DATA));
        assertFalse(container.has(CarryKeys.ENTITY_DATA, PersistentDataType.STRING));
    }

    @Test
    void readsExistingLegacyCarrierWithoutRewritingIt() {
        String snapshot = "{id:\"minecraft:villager\",Health:20.0f}";
        container.set(CarryKeys.ENTITY_DATA, PersistentDataType.STRING, snapshot);
        assertEquals(snapshot, CarriedEntityData.read(container, CarryKeys.ENTITY_DATA));
        assertTrue(container.has(CarryKeys.ENTITY_DATA, PersistentDataType.STRING));
    }

    @Test
    void rejectsMalformedBinaryWithoutRewritingIt() {
        byte[] malformed = {(byte) 0xC3, (byte) 0x28};
        container.set(CarryKeys.ENTITY_DATA, PersistentDataType.BYTE_ARRAY, malformed);
        assertThrows(IllegalArgumentException.class, () -> CarriedEntityData.read(container, CarryKeys.ENTITY_DATA));
        assertArrayEquals(malformed, container.get(CarryKeys.ENTITY_DATA, PersistentDataType.BYTE_ARRAY));
    }

    @Test
    void rejectsMalformedLargeInputBeforeReplacingExistingData() {
        container.set(CarryKeys.ENTITY_DATA, PersistentDataType.STRING, "original");
        String malformed = "a".repeat(65536) + '\uD800';
        assertThrows(IllegalArgumentException.class, () -> CarriedEntityData.write(container, CarryKeys.ENTITY_DATA, malformed));
        assertEquals("original", container.get(CarryKeys.ENTITY_DATA, PersistentDataType.STRING));
    }

    @Test
    void missingSnapshotIsNotACarrier() {
        assertFalse(CarriedEntityData.has(container, CarryKeys.ENTITY_DATA));
        assertNull(CarriedEntityData.read(container, CarryKeys.ENTITY_DATA));
    }

    private static byte[] modifiedUtf(String value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeUTF(value);
        }
        return bytes.toByteArray();
    }
}
