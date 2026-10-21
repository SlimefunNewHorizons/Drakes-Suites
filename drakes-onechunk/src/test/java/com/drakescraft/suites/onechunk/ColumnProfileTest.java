package com.drakescraft.suites.onechunk;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColumnProfileTest {
    @Test
    void cyclesProgressionWithoutGrowingState() {
        ColumnProfile profile = new ColumnProfile(List.of("STONE", "DIRT", "GRASS_BLOCK"));
        assertEquals("STONE", profile.materialAt(0));
        assertEquals("DIRT", profile.materialAt(1));
        assertEquals("GRASS_BLOCK", profile.materialAt(2));
        assertEquals("STONE", profile.materialAt(3));
    }

    @Test
    void calculatesTheFullSixteenBySixteenColumn() {
        assertEquals(768, new ColumnProfile(List.of("STONE")).columnBlockCount(0, 2));
    }

    @Test
    void rejectsAnInvertedHeightRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new ColumnProfile(List.of("STONE")).columnBlockCount(3, 2));
    }
}
