package com.drakescraft.suites.core.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Top-level economy commands ({@code /bal}, {@code /pay}, {@code /eco}) routed directly
 * through {@link EconomyRouter} so Vault per-modality providers (InvSwitcher) are always
 * used instead of EssentialsX's isolated internal wallet.
 */
public final class VaultEconomyCommand implements CommandExecutor, TabCompleter {

    private final Economy economy;

    public VaultEconomyCommand(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        return switch (cmd) {
            case "balance" -> handleBalance(sender, args);
            case "pay" -> handlePay(sender, args);
            case "eco" -> handleEco(sender, args);
            default -> false;
        };
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /bal <player>");
                return true;
            }
            double bal = economy.getBalance(player);
            player.sendMessage(ChatColor.GRAY + "Balance: " + ChatColor.GOLD + economy.format(bal));
            return true;
        }
        if (!sender.hasPermission("drakes.economy.balance.others") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to view another player's balance.");
            return true;
        }
        OfflinePlayer target = resolvePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }
        double bal = economy.getBalance(target);
        String name = target.getName() != null ? target.getName() : args[0];
        sender.sendMessage(ChatColor.GRAY + "Balance of " + ChatColor.WHITE + name + ChatColor.GRAY + ": "
                + ChatColor.GOLD + economy.format(bal));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player payer)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /pay.");
            return true;
        }
        if (args.length < 2) {
            payer.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }
        Double amount = parsePositiveAmount(args[1]);
        if (amount == null) {
            payer.sendMessage(ChatColor.RED + "Invalid amount: " + args[1]);
            return true;
        }
        OfflinePlayer target = resolvePlayer(args[0]);
        if (target == null) {
            payer.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }
        if (payer.getUniqueId().equals(target.getUniqueId())) {
            payer.sendMessage(ChatColor.RED + "You cannot pay yourself.");
            return true;
        }
        EconomyResponse w = economy.withdrawPlayer(payer, amount);
        if (!w.transactionSuccess()) {
            payer.sendMessage(ChatColor.RED + "Payment failed: " + safeMsg(w.errorMessage));
            return true;
        }
        EconomyResponse d = economy.depositPlayer(target, amount);
        if (!d.transactionSuccess()) {
            economy.depositPlayer(payer, amount);
            payer.sendMessage(ChatColor.RED + "Payment failed (refunded): " + safeMsg(d.errorMessage));
            return true;
        }
        String targetName = target.getName() != null ? target.getName() : args[0];
        String formatted = economy.format(amount);
        payer.sendMessage(ChatColor.GREEN + "Sent " + ChatColor.GOLD + formatted + ChatColor.GREEN + " to " + targetName + ".");
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.GREEN + "Received " + ChatColor.GOLD + formatted
                    + ChatColor.GREEN + " from " + payer.getName() + ".");
        }
        return true;
    }

    private boolean handleEco(CommandSender sender, String[] args) {
        if (!sender.hasPermission("drakes.economy.admin") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage economy.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco <give|take|set> <player> <amount>");
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        Double amount = parseNonNegativeAmount(args[2]);
        if (amount == null || (!action.equals("set") && amount <= 0.0D)) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
            return true;
        }

        String name = target.getName() != null ? target.getName() : args[1];
        EconomyResponse res;
        switch (action) {
            case "give" -> res = economy.depositPlayer(target, amount);
            case "take" -> res = economy.withdrawPlayer(target, amount);
            case "set" -> res = applySet(economy, target, amount);
            default -> {
                sender.sendMessage(ChatColor.RED + "Usage: /eco <give|take|set> <player> <amount>");
                return true;
            }
        }
        if (!res.transactionSuccess()) {
            sender.sendMessage(ChatColor.RED + "Operation failed: " + safeMsg(res.errorMessage));
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Updated " + name + " balance to " + ChatColor.GOLD + economy.format(res.balance) + ".");
        return true;
    }

    /** Applies an exact target balance using Vault deposit/withdraw deltas. */
    static EconomyResponse applySet(Economy eco, OfflinePlayer target, double targetBalance) {
        double current = eco.getBalance(target);
        double delta = targetBalance - current;
        if (Math.abs(delta) < 1.0E-6D) {
            return new EconomyResponse(0.0D, current, EconomyResponse.ResponseType.SUCCESS, null);
        }
        return delta > 0.0D
                ? eco.depositPlayer(target, delta)
                : eco.withdrawPlayer(target, -delta);
    }

    static Double parsePositiveAmount(String raw) {
        Double v = parseNonNegativeAmount(raw);
        return (v != null && v > 0.0D) ? v : null;
    }

    static Double parseNonNegativeAmount(String raw) {
        try {
            double v = Double.parseDouble(raw);
            return (Double.isFinite(v) && v >= 0.0D) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        return cached != null ? cached : Bukkit.getOfflinePlayer(name);
    }

    private static String safeMsg(String msg) {
        return (msg == null || msg.isBlank()) ? "unknown error" : msg;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        if (cmd.equals("eco") && args.length == 1) {
            return List.of("give", "take", "set");
        }
        if ((cmd.equals("balance") && args.length == 1)
                || (cmd.equals("pay") && args.length == 1)
                || (cmd.equals("eco") && args.length == 2)) {
            String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
