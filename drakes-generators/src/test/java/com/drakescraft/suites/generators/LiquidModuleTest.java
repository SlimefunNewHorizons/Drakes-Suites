package com.drakescraft.suites.generators;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import com.drakescraft.suites.generators.liquid.LiquidModule;
import com.drakescraft.suites.generators.liquid.MoltenFluidType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class LiquidModuleTest {

    private static ServerMock server;
    private static DrakesGeneratorsPlugin plugin;
    private static LiquidModule module;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesGeneratorsPlugin.class);
        module = (LiquidModule) plugin.getModuleManager().getModule("liquid");
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("LiquidModule está registrado y habilitado en DrakesGenerators")
    void testModuleEnabled() {
        assertNotNull(module, "LiquidModule debe estar registrado");
        assertTrue(module.isEnabled(), "LiquidModule debe estar habilitado");
        assertTrue(module.isAsyncThermodynamicsEnabled(), "Termodinámica asíncrona debe estar activa");
        assertEquals(16000, module.getMaxFluidStorageMb());
    }

    @Test
    @DisplayName("MoltenFluidType contiene metales fundidos canónicos con punto de fusión")
    void testMoltenFluidTypes() {
        assertEquals(9, MoltenFluidType.values().length);

        MoltenFluidType iron = MoltenFluidType.getBySlimefunId("MOLTEN_IRON");
        assertNotNull(iron);
        assertEquals(1538, iron.getMeltingPointCelsius());
        assertEquals(Material.LAVA_BUCKET, iron.getMaterial());

        MoltenFluidType gold = MoltenFluidType.getBySlimefunId("MOLTEN_GOLD");
        assertNotNull(gold);
        assertEquals(1064, gold.getMeltingPointCelsius());
    }

    @Test
    @DisplayName("LiquidModule genera cubos con PDC canónico y metadatos correctos")
    void testMoltenItemGeneration() {
        ItemStack ironBucket = module.getMoltenItem(MoltenFluidType.MOLTEN_IRON);
        assertNotNull(ironBucket);
        assertEquals(Material.LAVA_BUCKET, ironBucket.getType());
        assertEquals("MOLTEN_IRON", SuiteItemPdcBridge.getSlimefunId(ironBucket));
        assertTrue(module.isMoltenFluid(ironBucket));

        ItemStack goldBucket = module.getMoltenItem(MoltenFluidType.MOLTEN_GOLD);
        assertNotNull(goldBucket);
        assertEquals(Material.LAVA_BUCKET, goldBucket.getType());
        assertEquals("MOLTEN_GOLD", SuiteItemPdcBridge.getSlimefunId(goldBucket));
        assertTrue(module.isMoltenFluid(goldBucket));
    }
}
