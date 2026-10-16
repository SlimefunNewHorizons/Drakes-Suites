package com.drakescraft.suites.core.command;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.module.SuiteModule;
import com.drakescraft.suites.core.registry.SuiteRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Comando unificado para la administración, diagnóstico y recarga en caliente
 * de las 8 Mega-Suites de DrakesCraft.
 */
public class DrakesSuitesCommand implements CommandExecutor, TabCompleter {

    private final DrakesCorePlugin plugin;

    public DrakesSuitesCommand(DrakesCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("drakes.suites.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage DrakesCraft Mega-Suites.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("estado")) {
            showStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("recargar")) {
            if (args.length == 1) {
                // Recargar todo el ecosistema
                plugin.reloadConfig();
                for (SuiteRegistry.SuiteRegistration reg : plugin.getSuiteRegistry().getAllSuites().values()) {
                    reg.manager().reloadAll();
                }
                sender.sendMessage(ChatColor.GREEN + "[Drakes-Suites] All suites and modules have been reloaded.");
                return true;
            }

            String suiteId = args[1].toLowerCase();
            SuiteRegistry.SuiteRegistration reg = plugin.getSuiteRegistry().getSuite(suiteId);
            if (reg == null) {
                sender.sendMessage(ChatColor.RED + "Suite '" + suiteId + "' is not registered or active.");
                return true;
            }

            if (args.length >= 3) {
                String moduleId = args[2].toLowerCase();
                boolean ok = reg.manager().reloadModule(moduleId);
                if (ok) {
                    sender.sendMessage(ChatColor.GREEN + "[Drakes-Suites] Module '" + moduleId + "' from suite '" + suiteId + "' reloaded successfully.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Module '" + moduleId + "' was not found in suite '" + suiteId + "'.");
                }
            } else {
                reg.manager().reloadAll();
                sender.sendMessage(ChatColor.GREEN + "[Drakes-Suites] All modules in suite '" + suiteId + "' have been reloaded.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Drakes-Suites Master CLI ===");
        sender.sendMessage(ChatColor.YELLOW + "/drakessuites status " + ChatColor.GRAY + "- Global suite and module status");
        sender.sendMessage(ChatColor.YELLOW + "/drakessuites reload [suite] [module] " + ChatColor.GRAY + "- Hot reload");
        return true;
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "══════════════════════════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "  🏛️ DRAKES-SUITES — 8 MEGA-SUITE CONSOLIDATION");
        sender.sendMessage(ChatColor.DARK_AQUA + "══════════════════════════════════════════════════════");

        long currentTick = plugin.getTickerEngine() != null ? plugin.getTickerEngine().getCurrentTick() : 0;
        sender.sendMessage(ChatColor.GRAY + "Centralized Ticking Engine: " + ChatColor.GREEN + "ACTIVE "
                + ChatColor.DARK_GRAY + "(Tick #" + currentTick + ")");

        Map<String, SuiteRegistry.SuiteRegistration> suites = plugin.getSuiteRegistry().getAllSuites();
        sender.sendMessage(ChatColor.GRAY + "Registered Suites: " + ChatColor.YELLOW + suites.size() + "/8");

        for (Map.Entry<String, SuiteRegistry.SuiteRegistration> entry : suites.entrySet()) {
            SuiteRegistry.SuiteRegistration reg = entry.getValue();
            Map<String, SuiteModule> modules = reg.manager().getModules();

            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.AQUA).append("✦ Suite [").append(reg.plugin().getName()).append("]: ");

            if (modules.isEmpty()) {
                sb.append(ChatColor.DARK_GRAY).append("No submodules registered");
            } else {
                int activeCount = 0;
                for (SuiteModule mod : modules.values()) {
                    if (mod.isEnabled()) activeCount++;
                }
                sb.append(ChatColor.YELLOW).append(activeCount).append("/").append(modules.size()).append(" active ");
                sb.append(ChatColor.DARK_GRAY).append("(");
                int i = 0;
                for (SuiteModule mod : modules.values()) {
                    if (i++ > 0) sb.append(", ");
                    sb.append(mod.isEnabled() ? ChatColor.GREEN : ChatColor.RED).append(mod.getId());
                }
                sb.append(ChatColor.DARK_GRAY).append(")");
            }
            sender.sendMessage(sb.toString());
        }

        sender.sendMessage(ChatColor.DARK_AQUA + "══════════════════════════════════════════════════════");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("status");
            list.add("reload");
            return list;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            list.addAll(plugin.getSuiteRegistry().getAllSuites().keySet());
            return list;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("reload")) {
            SuiteRegistry.SuiteRegistration reg = plugin.getSuiteRegistry().getSuite(args[1]);
            if (reg != null) {
                list.addAll(reg.manager().getModules().keySet());
            }
            return list;
        }
        return List.of();
    }
}
