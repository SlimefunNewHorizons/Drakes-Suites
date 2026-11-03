package com.drakescraft.suites.tech.nanotech;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.tech.nanotech.content.NanotechContent;
import com.drakescraft.suites.tech.nanotech.gameplay.CosmicExposureListener;
import com.drakescraft.suites.tech.nanotech.gameplay.HeroSuitListener;
import com.drakescraft.suites.tech.nanotech.gameplay.NanotechWeaponListener;
import com.drakescraft.suites.tech.nanotech.gameplay.GodPrisonFieldService;
import com.drakescraft.suites.tech.nanotech.gameplay.StarkArmorEnchantService;
import com.drakescraft.suites.tech.nanotech.protection.ProtectionGate;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

/**
 * Módulo nativo de Nanotecnología Stark, reactores ARC, armaduras cinéticas y contención cósmica para drakes-tech.
 */
public class NanotechModule extends AbstractSuiteModule implements SlimefunAddon {

    private NanotechContent content;
    private GodPrisonFieldService godPrisonFields;
    private ProtectionGate protectionGate;
    private boolean heroSuitsEnabled = true;
    private boolean weaponsEnabled = true;
    private boolean cosmicExposureEnabled = true;

    public NanotechModule(JavaPlugin plugin) {
        super(plugin, "nanotech", "Drakes Nanotechnology & Stark Engineering");
    }

    @Override
    public void onEnable() {
        applyConfiguration();
        try {
            this.content = new NanotechContent(getPlugin(), this);
            this.content.registerAll();

            this.protectionGate = new ProtectionGate(getPlugin());
            this.godPrisonFields = new GodPrisonFieldService(getPlugin(), content, protectionGate);

            if (weaponsEnabled) {
                registerListener(new NanotechWeaponListener(getPlugin(), content, protectionGate, godPrisonFields));
            }

            if (heroSuitsEnabled) {
                HeroSuitListener heroSuits = new HeroSuitListener(getPlugin(), content);
                registerListener(heroSuits);
                StarkArmorEnchantService armorEnchantments = new StarkArmorEnchantService(getPlugin(), content);
                registerListener(armorEnchantments);
            }

            if (cosmicExposureEnabled) {
                registerListener(new CosmicExposureListener(getPlugin(), content));
            }

            getPlugin().getLogger().info("[DrakesTech] NanotechModule activado con éxito ("
                    + content.itemCount() + " items, " + content.machineCount() + " máquinas, "
                    + content.multiblockCount() + " multibloques).");
        } catch (Throwable t) {
            getPlugin().getLogger().warning("[DrakesTech] NanotechModule cargó en modo desacoplado / standalone: " + t.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (godPrisonFields != null) {
            godPrisonFields.shutdown();
            godPrisonFields = null;
        }
        this.content = null;
        getPlugin().getLogger().info("[DrakesTech] NanotechModule deshabilitado limpiamente.");
    }

    @Override
    public void onReload() {
        super.onReload();
        applyConfiguration();
    }

    private void applyConfiguration() {
        FileConfiguration cfg = getConfig();
        if (cfg != null) {
            this.heroSuitsEnabled = cfg.getBoolean("hero-suits-enabled", true);
            this.weaponsEnabled = cfg.getBoolean("weapons-enabled", true);
            this.cosmicExposureEnabled = cfg.getBoolean("cosmic-exposure-enabled", true);
        }
    }

    private void registerListener(org.bukkit.event.Listener listener) {
        getPlugin().getServer().getPluginManager().registerEvents(listener, getPlugin());
    }

    public NanotechContent getContent() {
        return content;
    }

    @Override
    public @Nonnull JavaPlugin getJavaPlugin() {
        return getPlugin();
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunNewHorizons/Drakes-Suites/issues";
    }
}
