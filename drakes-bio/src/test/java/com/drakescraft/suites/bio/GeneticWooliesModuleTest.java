package com.drakescraft.suites.bio;

import com.drakescraft.suites.bio.geneticwoolies.GeneticWooliesModule;
import com.drakescraft.suites.bio.geneticwoolies.WoolColorBreedRegistry;
import com.drakescraft.suites.bio.geneticwoolies.WoolDNA;
import com.drakescraft.suites.bio.geneticwoolies.WoolGene;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GeneticWooliesModuleTest {

    private ServerMock server;
    private DrakesBioPlugin plugin;
    private GeneticWooliesModule module;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesBioPlugin.class);
        module = (GeneticWooliesModule) plugin.getModuleManager().getModule("genetic_woolies");
        assertNotNull(module, "GeneticWooliesModule debe estar registrado en DrakesBioPlugin");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("WoolColorBreedRegistry cubre los 16 DyeColor y sus bloques de lana")
    void testRegistryCoversAllSixteenDyeColors() {
        assertEquals(16, WoolColorBreedRegistry.getRegisteredColorCount());
        assertTrue(WoolColorBreedRegistry.getRuleCount() >= 12);

        for (DyeColor color : DyeColor.values()) {
            assertNotNull(WoolColorBreedRegistry.getTier(color));
            Material woolMat = WoolColorBreedRegistry.getWoolMaterial(color);
            assertNotNull(woolMat);
            assertTrue(woolMat.name().endsWith("_WOOL"), "Material debe terminar en _WOOL: " + woolMat);
        }
    }

    @Test
    @DisplayName("Herencia mendeliana: dominancia de color BASE sobre RARE y segregación de rendimiento")
    void testMendelianInheritanceWithoutMutation() {
        // Padre A: RED (BASE) puro, rendimiento YY (3), pureza PP
        WoolDNA parentA = WoolDNA.pure(DyeColor.RED, 3, 3, 1);
        // Padre B: MAGENTA (RARE) puro, rendimiento yy (1), pureza pp
        WoolDNA parentB = WoolDNA.pure(DyeColor.MAGENTA, 0, 0, 2);

        // Sin mutación (0.0)
        WoolDNA child = parentA.breed(parentB, 0.0, new Random(42L));

        // RED (BASE) domina sobre MAGENTA (RARE), pero MAGENTA se conserva como alelo recesivo
        assertEquals(DyeColor.RED, child.getExpressedColor());
        assertEquals(DyeColor.RED, child.getDominantColor());
        assertEquals(DyeColor.MAGENTA, child.getRecessiveColor());
        assertFalse(child.isColorHomozygous());

        // Rendimiento: YY x yy -> siempre Yy (estado 1 -> nivel 2)
        assertEquals(1, child.getYieldGene().getState());
        assertEquals(2, child.getYield());
        // Pureza: PP x pp -> siempre Pp (estado 1 -> nivel 2)
        assertEquals(1, child.getPurityGene().getState());
        assertEquals(3, child.getGeneration());

        // Cruce entre dos heterocigotos (RED/MAGENTA) debe poder segregar MAGENTA homocigoto (25%)
        boolean sawPureMagenta = false;
        Random rng = new Random(12345L);
        for (int i = 0; i < 64; i++) {
            WoolDNA f2 = child.breed(child, 0.0, rng);
            if (f2.getExpressedColor() == DyeColor.MAGENTA && f2.isColorHomozygous()) {
                sawPureMagenta = true;
                break;
            }
        }
        assertTrue(sawPureMagenta, "Dos portadores heterocigotos deben poder segregar el color recesivo MAGENTA");
    }

    @Test
    @DisplayName("Mutación de color: cruces configurados producen colores secundarios y raros")
    void testColorMutationRules() {
        WoolDNA red = WoolDNA.pure(DyeColor.RED, 0, 1, 0);
        WoolDNA yellow = WoolDNA.pure(DyeColor.YELLOW, 0, 1, 0);

        // Con mutation-chance = 1.0 y pureza normal (mult 1.0), RED + YELLOW -> ORANGE homocigoto
        WoolDNA mutatedOrange = red.breed(yellow, 1.0, new Random(7L));
        assertEquals(DyeColor.ORANGE, mutatedOrange.getExpressedColor());
        assertTrue(mutatedOrange.isColorHomozygous());

        // Cruce secundario -> raro: PURPLE + PINK -> MAGENTA
        WoolDNA purple = WoolDNA.pure(DyeColor.PURPLE, 1, 1, 1);
        WoolDNA pink = WoolDNA.pure(DyeColor.PINK, 1, 1, 1);
        WoolDNA mutatedMagenta = purple.breed(pink, 1.0, new Random(9L));
        assertEquals(DyeColor.MAGENTA, mutatedMagenta.getExpressedColor());
        assertEquals(WoolColorBreedRegistry.ColorTier.RARE, WoolColorBreedRegistry.getTier(mutatedMagenta.getExpressedColor()));

        // Con mutation-chance = 0.0 nunca muta a ORANGE
        WoolDNA noMutation = red.breed(yellow, 0.0, new Random(7L));
        assertNotEquals(DyeColor.ORANGE, noMutation.getExpressedColor());
    }

    @Test
    @DisplayName("Serialización y deserialización segura en PDC de Sheep (cero NBT frágil)")
    void testPdcSerializationRoundTripAndCorruptSafety() {
        WorldMock world = server.addSimpleWorld("survival");
        Sheep sheep = (Sheep) world.spawnEntity(new Location(world, 0, 64, 0), EntityType.SHEEP);

        WoolDNA original = new WoolDNA(
                DyeColor.CYAN,
                DyeColor.BLUE,
                new WoolGene('y', 3),
                new WoolGene('p', 1),
                4
        );
        module.writeDna(sheep, original);

        WoolDNA loaded = module.readDna(sheep);
        assertNotNull(loaded);
        assertEquals(original, loaded);
        assertEquals(DyeColor.BLUE, loaded.getExpressedColor());
        assertEquals(DyeColor.CYAN, loaded.getRecessiveColor());
        assertEquals(3, loaded.getYield());
        assertEquals(4, loaded.getGeneration());

        // Datos corruptos en PDC no deben lanzar excepción
        sheep.getPersistentDataContainer().set(module.getDnaKey(), PersistentDataType.STRING, "corrupt:data:xxx");
        assertNull(module.readDna(sheep));
        assertNull(WoolDNA.deserialize(null));
        assertNull(WoolDNA.deserialize("v99:RED/BLUE:YY:PP:g1"));
        assertNull(WoolDNA.deserialize("v1:NOT_A_COLOR/BLUE:YY:PP:g1"));
    }

    @Test
    @DisplayName("EntityBreedEvent persiste ADN en la cría y respeta mundos excluidos (Laboratorio)")
    void testEntityBreedEventAndWorldExclusion() {
        WorldMock survival = server.addSimpleWorld("survival");
        Sheep mother = (Sheep) survival.spawnEntity(new Location(survival, 1, 64, 1), EntityType.SHEEP);
        Sheep father = (Sheep) survival.spawnEntity(new Location(survival, 2, 64, 1), EntityType.SHEEP);
        Sheep baby = (Sheep) survival.spawnEntity(new Location(survival, 1, 64, 2), EntityType.SHEEP);

        module.writeDna(mother, WoolDNA.pure(DyeColor.LIME, 3, 3, 1));
        module.writeDna(father, WoolDNA.pure(DyeColor.LIME, 3, 3, 1));

        EntityBreedEvent breedEvent = new EntityBreedEvent(baby, mother, father, null, null, 1);
        module.onEntityBreed(breedEvent);

        WoolDNA babyDna = module.readDna(baby);
        assertNotNull(babyDna, "La cría en survival debe recibir WoolDNA en su PDC");
        assertEquals(DyeColor.LIME, babyDna.getExpressedColor());
        assertEquals(DyeColor.LIME, baby.getColor());
        assertEquals(2, babyDna.getGeneration());

        // En Laboratorio no debe aplicarse genética
        WorldMock lab = server.addSimpleWorld("laboratorio");
        assertTrue(module.isWorldExcluded(lab));
        Sheep labMother = (Sheep) lab.spawnEntity(new Location(lab, 1, 64, 1), EntityType.SHEEP);
        Sheep labFather = (Sheep) lab.spawnEntity(new Location(lab, 2, 64, 1), EntityType.SHEEP);
        Sheep labBaby = (Sheep) lab.spawnEntity(new Location(lab, 1, 64, 2), EntityType.SHEEP);
        module.writeDna(labMother, WoolDNA.pure(DyeColor.MAGENTA, 3, 3, 1));
        module.writeDna(labFather, WoolDNA.pure(DyeColor.MAGENTA, 3, 3, 1));

        module.onEntityBreed(new EntityBreedEvent(labBaby, labMother, labFather, null, null, 1));
        assertNull(module.readDna(labBaby), "Laboratorio debe quedar excluido del sistema genético");
    }

    @Test
    @DisplayName("Bonus de esquila respeta rendimiento, tope max-bonus-wool, mundos excluidos y anti-dupe")
    void testShearBonusAndAntiDupe() {
        // Cálculo puro de bonus
        assertEquals(0, module.calculateBonusWool(null));
        assertEquals(0, module.calculateBonusWool(WoolDNA.pure(DyeColor.WHITE, 0, 1, 0))); // yield 1 -> +0
        assertEquals(1, module.calculateBonusWool(WoolDNA.pure(DyeColor.WHITE, 1, 1, 0))); // yield 2 -> +1
        assertEquals(2, module.calculateBonusWool(WoolDNA.pure(DyeColor.WHITE, 3, 1, 0))); // yield 3 -> +2 (cap 2)

        WorldMock survival = server.addSimpleWorld("survival");
        PlayerMock player = server.addPlayer("WoolTester");
        Sheep sheep = (Sheep) survival.spawnEntity(new Location(survival, 10, 64, 10), EntityType.SHEEP);
        sheep.setSheared(false);
        module.applyDnaToSheep(sheep, WoolDNA.pure(DyeColor.CYAN, 3, 3, 2));

        // Primera esquila por jugador -> otorga +2 CYAN_WOOL
        PlayerShearEntityEvent firstShear = new PlayerShearEntityEvent(player, sheep, new ItemStack(Material.SHEARS), EquipmentSlot.HAND, List.of());
        module.onPlayerShear(firstShear);

        List<Item> droppedAfterFirst = survival.getEntitiesByClass(Item.class).stream().toList();
        assertEquals(1, droppedAfterFirst.size());
        assertEquals(Material.CYAN_WOOL, droppedAfterFirst.get(0).getItemStack().getType());
        assertEquals(2, droppedAfterFirst.get(0).getItemStack().getAmount());

        // Anti-dupe: segundo evento inmediato (ej. dispenser en mismo tick antes de marcar sheared) no dupea
        Block dispenser = survival.getBlockAt(10, 65, 10);
        BlockShearEntityEvent duplicateShear = new BlockShearEntityEvent(dispenser, sheep, new ItemStack(Material.SHEARS), List.of());
        module.onBlockShear(duplicateShear);

        assertEquals(1, survival.getEntitiesByClass(Item.class).size(), "El guard anti-dupe debe bloquear el doble bonus");

        // Oveja ya esquilada nunca otorga bonus
        Sheep shearedSheep = (Sheep) survival.spawnEntity(new Location(survival, 20, 64, 20), EntityType.SHEEP);
        shearedSheep.setSheared(true);
        module.applyDnaToSheep(shearedSheep, WoolDNA.pure(DyeColor.MAGENTA, 3, 3, 1));
        module.onPlayerShear(new PlayerShearEntityEvent(player, shearedSheep, new ItemStack(Material.SHEARS), EquipmentSlot.HAND, List.of()));
        assertEquals(1, survival.getEntitiesByClass(Item.class).size(), "Oveja ya esquilada no debe otorgar bonus");

        // Laboratorio nunca otorga bonus
        WorldMock lab = server.addSimpleWorld("laboratorio");
        Sheep labSheep = (Sheep) lab.spawnEntity(new Location(lab, 0, 64, 0), EntityType.SHEEP);
        labSheep.setSheared(false);
        module.applyDnaToSheep(labSheep, WoolDNA.pure(DyeColor.PURPLE, 3, 3, 1));
        module.onPlayerShear(new PlayerShearEntityEvent(player, labSheep, new ItemStack(Material.SHEARS), EquipmentSlot.HAND, List.of()));
        assertTrue(lab.getEntitiesByClass(Item.class).isEmpty(), "Laboratorio no debe dropear bonus de lana");
    }
}
