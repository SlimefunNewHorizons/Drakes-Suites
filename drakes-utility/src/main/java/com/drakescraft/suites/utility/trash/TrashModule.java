package com.drakescraft.suites.utility.trash;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

/**
 * TrashModule - Papelera virtual interactiva (/trash, /disposal, /basura, /papelera).
 * Permite a los jugadores descartar objetos no deseados de forma instantánea y segura.
 */
public class TrashModule extends AbstractSuiteModule implements Listener, CommandExecutor, TabCompleter {

    public static final String PERMISSION_USE = "drakes.utility.trash";

    public TrashModule(JavaPlugin plugin) {
        super(plugin, "trash", "Trash Disposal System");
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        PluginCommand cmd = plugin.getCommand("trash");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }

        logInfo("Módulo Trash cargado correctamente. Comandos: /trash, /disposal, /basura, /papelera, /basurero.");
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            CrossVersionAdapter.sendMessage(sender, config.getString("messages.only-players", "<red>Only online players can open the recycling bin.</red>"));
            return true;
        }

        if (!player.hasPermission(PERMISSION_USE)) {
            CrossVersionAdapter.sendMessage(player, config.getString("messages.no-permission", "<red>You do not have permission to use the recycling bin.</red>"));
            return true;
        }

        int rows = config.getInt("gui.rows", 4);
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        int size = rows * 9;

        String title = config.getString("gui.title", "<gradient:#e53935:#d32f2f><bold>Recycling Bin</bold></gradient> <dark_gray>(Close to destroy)</dark_gray>");

        TrashHolder holder = new TrashHolder();
        Inventory inv = Bukkit.createInventory(holder, size, CrossVersionAdapter.parseComponent(title));
        holder.setInventory(inv);

        player.openInventory(inv);

        if (config.getBoolean("sounds.open", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.7f, 1.2f);
        }

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TrashHolder)) {
            return;
        }

        int totalCount = 0;
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalCount += item.getAmount();
            }
        }

        // Limpiar el inventario inmediatamente para que los items desaparezcan
        event.getInventory().clear();

        if (totalCount > 0 && event.getPlayer() instanceof Player player) {
            if (config.getBoolean("sounds.destroy", true)) {
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.6f, 1.4f);
            }

            String msg = config.getString("messages.destroyed", "<gray>[<gradient:#e53935:#d32f2f>Recycling Bin</gradient>] <red>{count}</red> <gray>item(s) permanently destroyed.</gray>")
                    .replace("{count}", String.valueOf(totalCount));
            CrossVersionAdapter.sendActionBar(player, msg);
            logInfo("Jugador " + player.getName() + " destruyó " + totalCount + " items en la papelera virtual.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
