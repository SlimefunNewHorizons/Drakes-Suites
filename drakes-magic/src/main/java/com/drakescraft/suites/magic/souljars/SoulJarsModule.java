package com.drakescraft.suites.magic.souljars;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Módulo de SoulJars para DrakesMagic.
 * Permite almacenar y cosechar almas de monstruos y criaturas pasivas en frascos místicos
 * para la restauración de Broken Spawners en Ancient Altars.
 */
public class SoulJarsModule extends AbstractSuiteModule {

    private final SoulJarRegistry registry = new SoulJarRegistry();
    private SoulJarListener listener;

    public SoulJarsModule(JavaPlugin plugin) {
        super(plugin, "soul_jars", "Soul Containment Jars");
    }

    static boolean isStandaloneSoulJarsPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("SoulJars") != null;
    }

    @Override
    public void onEnable() {
        // Cargar posibles personalizaciones de requisitos de almas desde la configuración del módulo
        ConfigurationSection soulsSection = config.getConfigurationSection("souls-required");
        if (soulsSection != null) {
            for (String key : soulsSection.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int souls = soulsSection.getInt(key, 128);
                    registry.setRequiredSouls(type, souls);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (isStandaloneSoulJarsPresent()) {
            getPlugin().getLogger().info("[DrakesMagic] Plugin standalone SoulJars detectado; cediendo listener al addon dedicado.");
            return;
        }

        // Registrar listener
        this.listener = new SoulJarListener(this, registry);
        Bukkit.getPluginManager().registerEvents(this.listener, getPlugin());

        getPlugin().getLogger().info("[DrakesMagic] SoulJarsModule habilitado con "
                + registry.getSupportedEntities().size() + " criaturas registradas.");
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            HandlerList.unregisterAll(this.listener);
            this.listener = null;
        }
        getPlugin().getLogger().info("[DrakesMagic] SoulJarsModule deshabilitado limpiamente.");
    }

    public SoulJarRegistry getRegistry() {
        return registry;
    }
}
