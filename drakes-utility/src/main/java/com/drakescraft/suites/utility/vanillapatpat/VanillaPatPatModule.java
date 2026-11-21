package com.drakescraft.suites.utility.vanillapatpat;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VanillaPatPatModule - QoL module allowing players to pat and pet mobs with custom sounds, hearts, and actionbar notifications.
 */
public class VanillaPatPatModule extends AbstractSuiteModule implements Listener {

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private long cooldownMs = 500;
    private Sound patSound = Sound.ENTITY_CAT_PURR;
    private Particle patParticle = Particle.HEART;
    private int particleCount = 3;
    private String patMessage = "You patted {name}!";

    public VanillaPatPatModule(@Nonnull JavaPlugin plugin) {
        super(plugin, "vanillapatpat", "VanillaPatPat Mob Interaction & Petting Mechanics");
    }

    static boolean isStandaloneVanillaPatPatPresent() {
        return org.bukkit.Bukkit.getPluginManager() != null
                && org.bukkit.Bukkit.getPluginManager().getPlugin("VanillaPatPat") != null;
    }

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        this.cooldownMs = config.getLong("cooldown-ms", 500);
        this.patMessage = config.getString("message", "You patted {name}!");
        this.particleCount = config.getInt("particle-count", 3);

        String soundName = config.getString("sound", "ENTITY_CAT_PURR");
        try {
            this.patSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            this.patSound = Sound.ENTITY_CAT_PURR;
        }

        String particleName = config.getString("particle", "HEART");
        try {
            this.patParticle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            this.patParticle = Particle.HEART;
        }

        if (isStandaloneVanillaPatPatPresent()) {
            logInfo("Plugin standalone VanillaPatPat detectado; cediendo listener al plugin dedicado.");
            return;
        }

        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        logInfo("Mob petting mechanics initialized.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        cooldowns.clear();
        logInfo("Module disabled cleanly.");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();

        // Must be sneaking to avoid interfering with villagers/ridable entities
        if (!player.isSneaking()) {
            return;
        }

        // Must have empty hand
        if (!player.getInventory().getItemInMainHand().getType().isAir()) {
            return;
        }

        if (!(clicked instanceof LivingEntity)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastPat = cooldowns.get(playerId);
        if (lastPat != null && (now - lastPat) < cooldownMs) {
            return;
        }

        cooldowns.put(playerId, now);
        event.setCancelled(true);

        // 1. Swing main hand
        player.swingMainHand();

        // 2. Play sound
        try {
            player.playSound(clicked.getLocation(), patSound, 1.0f, 1.0f);
        } catch (Exception ignored) {
        }

        // 3. Spawn particles
        try {
            Location headLocation = clicked.getLocation().add(0, clicked.getHeight(), 0);
            clicked.getWorld().spawnParticle(
                patParticle,
                headLocation,
                particleCount,
                0.3, 0.3, 0.3,
                0.0
            );
        } catch (Exception ignored) {
        }

        // 4. Send action bar in English
        String entityName = clicked.customName() != null ? clicked.getName() : clicked.getName();
        String formatted = patMessage.replace("{name}", entityName);
        Component msg = Component.text(formatted).color(NamedTextColor.LIGHT_PURPLE);
        player.sendActionBar(msg);
    }

    public long getCooldownMs() {
        return cooldownMs;
    }

    public String getPatMessage() {
        return patMessage;
    }
}
