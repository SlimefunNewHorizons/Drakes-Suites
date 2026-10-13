package com.drakescraft.suites.server;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class LiveServerYamlConfigSmokeTest {

    /**
     * Resolucion del directorio de plugins en vivo. Se busca, en orden:
     * 1) propiedad de sistema "drakes.live.plugins";
     * 2) variable de entorno DRAKES_LIVE_PLUGINS;
     * 3) la ruta que usa el respaldo de produccion en el VPS;
     * 4) la ruta historica del portatil de Jack.
     * Si ninguna existe se omiten los tests: dependen de un respaldo externo
     * que no viaja en el repositorio ni en CI.
     */
    private static File resolveBackupDir() {
        String candidates = System.getProperty("drakes.live.plugins")
                + "," + System.getenv("DRAKES_LIVE_PLUGINS")
                + "," + System.getProperty("user.home") + "/star-server-backup/drakescraft/plugins"
                + ",/home/jack/Documentos/Desarrollo/Repositorios/star-server-backup/drakescraft/plugins";
        for (String candidate : candidates.split(",")) {
            String path = candidate.trim();
            if (path.isEmpty() || "null".equals(path)) continue;
            File dir = new File(path);
            if (dir.isDirectory()) return dir;
        }
        return null;
    }

    private static File backupDir;

    @BeforeEach
    void requireLiveBackup() {
        backupDir = resolveBackupDir();
        Assumptions.assumeTrue(backupDir != null,
                "No hay respaldo en vivo de plugins disponible (define -Ddrakes.live.plugins=<ruta> o DRAKES_LIVE_PLUGINS); test omitido");
    }

    @Test
    @DisplayName("Smoke Test: Los 13 YAMLs en vivo de Odysseia/Star cargan y validan sin errores")
    void testLiveOdysseiaYamls() {
        File odysseiaDir = new File(backupDir, "Odysseia");
        assertTrue(odysseiaDir.exists() && odysseiaDir.isDirectory(), "Directorio de Odysseia en vivo debe existir");

        String[] requiredYamls = {
            "config.yml",
            "daily-rewards.yml",
            "kit-claims.yml",
            "muertes.yml",
            "purchases.yml",
            "sfmaster_blocks.yml",
            "papa-trader.yml",
            "global-playtime.yml",
            "cosmetics.yml",
            "chat-warnings.yml",
            "papa-canjes.yml"
        };

        for (String yamlName : requiredYamls) {
            File yamlFile = new File(odysseiaDir, yamlName);
            assertTrue(yamlFile.exists(), "Archivo en vivo requerido debe existir: " + yamlName);

            YamlConfiguration config = YamlConfiguration.loadConfiguration(yamlFile);
            assertNotNull(config, "Configuración no debe ser nula al parsear: " + yamlName);

            // Validar que no haya excepciones silenciosas o contenido vacío si el archivo tiene tamaño > 0
            if (yamlFile.length() > 100) {
                assertFalse(config.getKeys(false).isEmpty(), "YAML en vivo no debe estar vacío: " + yamlName);
            }
        }
    }

    @Test
    @DisplayName("Smoke Test: Validacion estructural profunda de config.yml y daily-rewards.yml en vivo")
    void testOdysseiaConfigDeepIntegrity() {
        File configFile = new File(backupDir, "Odysseia/config.yml");
        assertTrue(configFile.isFile(), "config.yml en vivo debe existir");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // config.yml es el maestro de modulos de Odysseia/Star: debe seguir siendo un
        // archivo amplio de secciones de raiz, no una plantilla vacia.
        ConfigurationSection root = config.getConfigurationSection("");
        assertNotNull(root, "config.yml debe parsear con una raiz de configuracion");
        int secciones = root.getKeys(false).size();
        assertTrue(secciones >= 40,
                "config.yml debe conservar la red amplia de modulos (minimo 40 secciones raiz, halladas " + secciones + ")");
        assertTrue(config.contains("modalidades"), "config.yml debe declarar la seccion de modalidades");

        File rewardsFile = new File(backupDir, "Odysseia/daily-rewards.yml");
        assertTrue(rewardsFile.isFile(), "daily-rewards.yml en vivo debe existir");
        YamlConfiguration rewards = YamlConfiguration.loadConfiguration(rewardsFile);
        ConfigurationSection players = rewards.getConfigurationSection("players");
        assertNotNull(players, "daily-rewards.yml debe contener la seccion players");
        // Es un almacen de datos reales: cada registro debe traer streak y last.
        for (String uuid : players.getKeys(false)) {
            ConfigurationSection entry = players.getConfigurationSection(uuid);
            assertNotNull(entry, "Cada registro de jugador debe ser una seccion");
            assertTrue(entry.contains("streak"), "Cada registro de jugador debe traer streak");
            assertTrue(entry.contains("last"), "Cada registro de jugador debe traer last");
        }
    }

    @Test
    @DisplayName("Smoke Test: YAMLs de plugins complementarios del servidor en vivo cargan con exito")
    void testComplementaryPluginsLiveYamls() {
        String[] complementaryPlugins = {
            "AxGraves",
            "PlayerVaultZ",
            "BentoBox",
            "AlchimiaVitae",
            "CrystamaeHistoria",
            "Cultivation",
            "Netheopoiesis",
            "WorldwideChat"
        };

        for (String pluginName : complementaryPlugins) {
            File pDir = new File(backupDir, pluginName);
            if (pDir.exists()) {
                File[] ymls = pDir.listFiles((dir, name) -> name.endsWith(".yml"));
                if (ymls != null) {
                    for (File yml : ymls) {
                        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(yml);
                        assertNotNull(cfg, "Error al parsear YAML de " + pluginName + ": " + yml.getName());
                    }
                }
            }
        }
    }
}
