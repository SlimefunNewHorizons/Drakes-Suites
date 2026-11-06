package com.drakescraft.suites.core;

import com.drakescraft.suites.core.command.CommandModalityGate;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

class CommandModalityGateTest {

    private static ServerMock server;
    private static DrakesCorePlugin plugin;
    private static CommandModalityGate gate;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DrakesCorePlugin.class);
        gate = plugin.getCommandGate();
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Comando /sf es bloqueado en world_clasico")
    void testCommandBlockedInClasico() {
        WorldMock clasicoWorld = server.addSimpleWorld("world_clasico");
        PlayerMock player = server.addPlayer();
        player.teleport(clasicoWorld.getSpawnLocation());

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/sf");
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "Comando /sf debe ser cancelado en Survival Clásico");
    }

    @Test
    @DisplayName("Comando /sf es permitido en world (Survival Slimefun)")
    void testCommandAllowedInNormalWorld() {
        WorldMock normalWorld = server.addSimpleWorld("world");
        PlayerMock player = server.addPlayer();
        player.teleport(normalWorld.getSpawnLocation());

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/sf");
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "Comando /sf debe estar permitido en world");
    }

    @ParameterizedTest
    @CsvSource({"clasico,/sf", "clasico_nether,/slimefun:sf", "clasico_the_end,/sf guide",
            "oneblock_world,/is", "oneblock_world_nether,/bskyblock:is",
            "oneblock_world_the_end,/is", "bskyblock_world,/ob",
            "bskyblock_world_nether,/oneblock:ob", "bskyblock_world_the_end,/ob"})
    void testCurrentWorldNames(String worldName, String command) {
        WorldMock world = server.addSimpleWorld(worldName);
        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled(), command + " in " + worldName);
    }

    @ParameterizedTest
    @CsvSource({"/sf cheat STONE", "/slimefun:sf give DIAMOND"})
    void testLaboratorySubcommandRestrictions(String command) {
        WorldMock world = server.addSimpleWorld("laboratorio");
        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled(), command);
    }

    @Test
    @DisplayName("Comando /is es bloqueado en world_oneblock y sugiere /ob")
    void testIsBlockedInOneBlock() {
        WorldMock oneblockWorld = server.addSimpleWorld("world_oneblock");
        PlayerMock player = server.addPlayer();
        player.teleport(oneblockWorld.getSpawnLocation());

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/is");
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "Comando /is debe ser cancelado en OneBlock");
    }

    @Test
    @DisplayName("Bypass con permiso drakescore.commandgate.bypass permite ejecutar comandos bloqueados")
    void testBypassPermission() {
        WorldMock clasicoWorld = server.addSimpleWorld("world_clasico");
        PlayerMock admin = server.addPlayer();
        admin.teleport(clasicoWorld.getSpawnLocation());
        admin.addAttachment(plugin, "drakescore.commandgate.bypass", true);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(admin, "/sf");
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "Admin con bypass no debe tener comandos bloqueados");
    }

    @Test
    @DisplayName("Prefijo de plugin /slimefun:sf es bloqueado igual que /sf para evitar evasión")
    void testPrefixBypassBlockedInClasico() {
        WorldMock clasicoWorld = server.addSimpleWorld("world_clasico");
        PlayerMock player = server.addPlayer();
        player.teleport(clasicoWorld.getSpawnLocation());

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/slimefun:sf");
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "Comando /slimefun:sf con prefijo de plugin debe ser cancelado");
    }

    @Test
    @DisplayName("En Laboratorio los comandos de economía, subastas y bóvedas son bloqueados")
    void testLaboratorioEconomyCommandsBlocked() {
        WorldMock labWorld = server.addSimpleWorld("laboratorio");
        PlayerMock player = server.addPlayer();
        player.teleport(labWorld.getSpawnLocation());

        PlayerCommandPreprocessEvent eventShop = new PlayerCommandPreprocessEvent(player, "/shop");
        server.getPluginManager().callEvent(eventShop);
        assertTrue(eventShop.isCancelled(), "/shop debe estar bloqueado en Laboratorio");

        PlayerCommandPreprocessEvent eventPay = new PlayerCommandPreprocessEvent(player, "/essentials:pay fulano 100");
        server.getPluginManager().callEvent(eventPay);
        assertTrue(eventPay.isCancelled(), "/essentials:pay debe estar bloqueado en Laboratorio");

        PlayerCommandPreprocessEvent eventAh = new PlayerCommandPreprocessEvent(player, "/ah");
        server.getPluginManager().callEvent(eventAh);
        assertTrue(eventAh.isCancelled(), "/ah debe estar bloqueado en Laboratorio");

        PlayerCommandPreprocessEvent eventPv = new PlayerCommandPreprocessEvent(player, "/pv 1");
        server.getPluginManager().callEvent(eventPv);
        assertTrue(eventPv.isCancelled(), "/pv debe estar bloqueado en Laboratorio");
    }

    @Test
    @DisplayName("En Laboratorio los comandos permitidos en lista blanca pasan y comandos no autorizados son bloqueados")
    void testLaboratorioWhitelistMode() {
        WorldMock labWorld = server.addSimpleWorld("laboratorio");
        PlayerMock player = server.addPlayer();
        player.teleport(labWorld.getSpawnLocation());

        PlayerCommandPreprocessEvent eventPlot = new PlayerCommandPreprocessEvent(player, "/plot auto");
        server.getPluginManager().callEvent(eventPlot);
        assertFalse(eventPlot.isCancelled(), "/plot auto debe estar permitido por lista blanca");

        PlayerCommandPreprocessEvent eventSpawn = new PlayerCommandPreprocessEvent(player, "/spawn");
        server.getPluginManager().callEvent(eventSpawn);
        assertFalse(eventSpawn.isCancelled(), "/spawn debe estar permitido por lista blanca");

        PlayerCommandPreprocessEvent eventUnknown = new PlayerCommandPreprocessEvent(player, "/randomunknowncommand");
        server.getPluginManager().callEvent(eventUnknown);
        assertTrue(eventUnknown.isCancelled(), "Comando no autorizado debe ser cancelado por modo lista blanca");
    }
}
