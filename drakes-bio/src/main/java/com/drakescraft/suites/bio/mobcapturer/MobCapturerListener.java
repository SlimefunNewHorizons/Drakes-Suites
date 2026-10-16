package com.drakescraft.suites.bio.mobcapturer;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Listener de interacción para el disparo del Mob Cannon y el impacto del perdigón.
 */
public class MobCapturerListener implements Listener {

    public static final String METADATA_PELLET = "mob_capturing_pellet";

    private final MobCapturerModule module;
    private final MobCapturerRegistry registry;

    public MobCapturerListener(MobCapturerModule module, MobCapturerRegistry registry) {
        this.module = module;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!module.isEnabled()) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !registry.isMobCannon(item)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Verificar munición
        if (!consumePellet(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c[DrakesBio · MobCapturer] &7You do not have any &bMob Capturing Pellets&7 in your inventory!"));
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.5f);
            return;
        }

        // Lanzar perdigón
        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setMetadata(METADATA_PELLET, new FixedMetadataValue(module.getPlugin(), player.getUniqueId().toString()));
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.8f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!module.isEnabled()) return;

        if (!(event.getEntity() instanceof Snowball projectile)) {
            return;
        }

        if (!projectile.hasMetadata(METADATA_PELLET)) {
            return;
        }

        if (!(event.getHitEntity() instanceof LivingEntity target)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }

        if (!registry.canCapture(target)) {
            shooter.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c[DrakesBio · MobCapturer] &7This creature is immune to bio-cryogenic capture."));
            shooter.playSound(shooter.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
            return;
        }

        // Captura exitosa
        ItemStack egg = registry.createMobEgg(target.getType());
        target.getWorld().dropItemNaturally(target.getLocation(), egg);
        target.remove();

        shooter.playSound(shooter.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        shooter.playSound(shooter.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        shooter.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a[DrakesBio · MobCapturer] &fYou successfully captured a &e"
                        + MobCapturerRegistry.humanize(target.getType().name()) + "&f!"));
    }

    private boolean consumePellet(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack != null && registry.isMobPellet(stack)) {
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                return true;
            }
        }
        return false;
    }
}
