package com.drakescraft.suites.bio.mobcapturer;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Módulo MobCapturer para DrakesBio.
 * Permite capturar entidades y almacenarlas en huevos de spawn mediante el Mob Cannon y Mob Capturing Pellets.
 */
public class MobCapturerModule extends AbstractSuiteModule {

    private final MobCapturerRegistry registry = new MobCapturerRegistry();
    private MobCapturerListener listener;

    public MobCapturerModule(JavaPlugin plugin) {
        super(plugin, "mob_capturer", "Bio-Extraction & Mob Capturing");
    }

    static boolean isStandaloneMobCapturerPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("MobCapturer") != null;
    }

    @Override
    public void onEnable() {
        boolean preventBoss = config.getBoolean("anti-dupe.prevent-boss-capture", true);
        boolean preventNpc = config.getBoolean("anti-dupe.prevent-named-npc-capture", true);
        this.registry.configure(preventBoss, preventNpc);

        if (isStandaloneMobCapturerPresent()) {
            getPlugin().getLogger().info("[DrakesBio] Plugin standalone MobCapturer detectado; cediendo listener al addon dedicado.");
            return;
        }

        this.listener = new MobCapturerListener(this, this.registry);
        Bukkit.getPluginManager().registerEvents(this.listener, getPlugin());

        getPlugin().getLogger().info("[DrakesBio] MobCapturerModule habilitado.");
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            HandlerList.unregisterAll(this.listener);
            this.listener = null;
        }
        getPlugin().getLogger().info("[DrakesBio] MobCapturerModule deshabilitado limpiamente.");
    }

    public MobCapturerRegistry getRegistry() {
        return registry;
    }
}
