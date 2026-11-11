package com.drakescraft.suites.server.cosmetics;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Módulo de Cosméticos de Servidor para Dallas (DrakesCraft): Armadura Fantasma / HideArmor.
 * Permite a los jugadores ocultar visualmente su armadura para que otros vean su skin completa,
 * preservando el 100% de la protección, encantamientos, atributos y durabilidad real en inventario.
 */
public class PhantomArmorModule extends AbstractSuiteModule implements Listener {

    private final Set<UUID> hiddenArmorPlayers = ConcurrentHashMap.newKeySet();
    private HideArmorCommand command;

    public PhantomArmorModule(JavaPlugin plugin) {
        super(plugin, "cosmetics_armor", "DrakesCosmetics Phantom Armor & Skin Visuals Engine");
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.command = new HideArmorCommand(this);
        try {
            Bukkit.getCommandMap().register("drakes", this.command);
        } catch (Exception e) {
            plugin.getLogger().warning("[DrakesCosmetics] No se pudo registrar el comando dinámico /hidearmor: " + e.getMessage());
        }

        plugin.getLogger().info("[DrakesCosmetics] Módulo de Armadura Fantasma (Dallas) habilitado con éxito.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        for (UUID uuid : hiddenArmorPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                restoreVisibleArmor(p);
            }
        }
        hiddenArmorPlayers.clear();

        plugin.getLogger().info("[DrakesCosmetics] Módulo de Armadura Fantasma deshabilitado limpiamente.");
    }

    public boolean isArmorHidden(Player player) {
        return hiddenArmorPlayers.contains(player.getUniqueId());
    }

    public boolean toggleArmorVisibility(Player player) {
        if (isArmorHidden(player)) {
            setArmorHidden(player, false);
            return false;
        } else {
            setArmorHidden(player, true);
            return true;
        }
    }

    public void setArmorHidden(Player player, boolean hide) {
        if (hide) {
            hiddenArmorPlayers.add(player.getUniqueId());
            sendPhantomArmorPackets(player);
        } else {
            hiddenArmorPlayers.remove(player.getUniqueId());
            restoreVisibleArmor(player);
        }
    }

    public void sendPhantomArmorPackets(Player target) {
        Map<EquipmentSlot, ItemStack> emptySlots = new EnumMap<>(EquipmentSlot.class);
        ItemStack empty = new ItemStack(Material.AIR);
        emptySlots.put(EquipmentSlot.HEAD, empty);
        emptySlots.put(EquipmentSlot.CHEST, empty);
        emptySlots.put(EquipmentSlot.LEGS, empty);
        emptySlots.put(EquipmentSlot.FEET, empty);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(target) && viewer.getWorld().equals(target.getWorld())) {
                viewer.sendEquipmentChange(target, emptySlots);
            }
        }
    }

    public void restoreVisibleArmor(Player target) {
        if (target.getEquipment() == null) return;

        Map<EquipmentSlot, ItemStack> realArmor = new EnumMap<>(EquipmentSlot.class);
        realArmor.put(EquipmentSlot.HEAD, target.getEquipment().getHelmet() != null ? target.getEquipment().getHelmet() : new ItemStack(Material.AIR));
        realArmor.put(EquipmentSlot.CHEST, target.getEquipment().getChestplate() != null ? target.getEquipment().getChestplate() : new ItemStack(Material.AIR));
        realArmor.put(EquipmentSlot.LEGS, target.getEquipment().getLeggings() != null ? target.getEquipment().getLeggings() : new ItemStack(Material.AIR));
        realArmor.put(EquipmentSlot.FEET, target.getEquipment().getBoots() != null ? target.getEquipment().getBoots() : new ItemStack(Material.AIR));

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(target) && viewer.getWorld().equals(target.getWorld())) {
                viewer.sendEquipmentChange(target, realArmor);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        if (isArmorHidden(joined)) {
            sendPhantomArmorPackets(joined);
        }
        for (UUID hiddenId : hiddenArmorPlayers) {
            Player hidden = Bukkit.getPlayer(hiddenId);
            if (hidden != null && hidden.isOnline() && !hidden.equals(joined) && hidden.getWorld().equals(joined.getWorld())) {
                Map<EquipmentSlot, ItemStack> emptySlots = new EnumMap<>(EquipmentSlot.class);
                ItemStack empty = new ItemStack(Material.AIR);
                emptySlots.put(EquipmentSlot.HEAD, empty);
                emptySlots.put(EquipmentSlot.CHEST, empty);
                emptySlots.put(EquipmentSlot.LEGS, empty);
                emptySlots.put(EquipmentSlot.FEET, empty);
                joined.sendEquipmentChange(hidden, emptySlots);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (isArmorHidden(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendPhantomArmorPackets(player), 2L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isArmorHidden(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendPhantomArmorPackets(player), 2L);
        }
    }

    private static class HideArmorCommand extends Command {
        private final PhantomArmorModule module;

        public HideArmorCommand(PhantomArmorModule module) {
            super("hidearmor", "Alterna la visibilidad de tu armadura para mostrar tu skin.", "/hidearmor [on|off|toggle]", List.of("phantomarmor", "armadurafantasma", "armadura"));
            this.module = module;
            setPermission("drakes.cosmetics.hidearmor");
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Este comando solo puede ser ejecutado por jugadores en Dallas.", NamedTextColor.RED));
                return true;
            }

            if (!testPermission(player)) {
                return true;
            }

            boolean newState;
            if (args.length > 0) {
                String sub = args[0].toLowerCase();
                if (sub.equals("on") || sub.equals("ocultar") || sub.equals("hide")) {
                    newState = true;
                } else if (sub.equals("off") || sub.equals("mostrar") || sub.equals("show")) {
                    newState = false;
                } else {
                    newState = !module.isArmorHidden(player);
                }
            } else {
                newState = !module.isArmorHidden(player);
            }

            module.setArmorHidden(player, newState);

            if (newState) {
                player.sendMessage(Component.text("✦ [DrakesCosmetics] ", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .append(Component.text("Armadura Fantasma ", NamedTextColor.AQUA))
                        .append(Component.text("ACTIVADA. Tu skin ahora es visible para los demás jugadores.", NamedTextColor.GREEN)));
            } else {
                player.sendMessage(Component.text("✦ [DrakesCosmetics] ", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .append(Component.text("Armadura visible ", NamedTextColor.YELLOW))
                        .append(Component.text("RESTAURADA. Tu armadura vuelve a verse normalmente.", NamedTextColor.GRAY)));
            }

            return true;
        }
    }
}
