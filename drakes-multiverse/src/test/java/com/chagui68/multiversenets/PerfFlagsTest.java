package com.chagui68.multiversenets;

import com.chagui68.multiversenets.util.PerfFlags;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/** suites.multiverse.no-snapshot: con y sin el flag el holder ve y modifica el mismo inventario. */
class PerfFlagsTest {

    private WorldMock world;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        PerfFlags.noSnapshot = false;
        MockBukkit.unmock();
    }

    @Test
    void sameInventoryWithAndWithoutSnapshot() {
        for (boolean flag : new boolean[] { false, true }) {
            PerfFlags.noSnapshot = flag;
            Block chest = world.getBlockAt(flag ? 2 : 0, 64, 0);
            chest.setType(Material.CHEST);
            InventoryHolder h = PerfFlags.holderOf(chest);
            assertNotNull(h, "flag=" + flag);
            h.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
            // una segunda lectura (otro holder) ve lo escrito por el primero
            assertEquals(5, PerfFlags.holderOf(chest).getInventory().all(Material.DIAMOND).values().stream()
                    .mapToInt(ItemStack::getAmount).sum(), "flag=" + flag);
        }
        Block stone = world.getBlockAt(5, 64, 0);
        stone.setType(Material.STONE);
        assertNull(PerfFlags.holderOf(stone));
    }
}
