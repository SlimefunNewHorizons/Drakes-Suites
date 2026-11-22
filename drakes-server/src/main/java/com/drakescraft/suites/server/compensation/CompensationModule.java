package com.drakescraft.suites.server.compensation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Módulo de Cofre de Devoluciones por Bug (/devolucion + /reclamar).
 * Permite entregar ítems Slimefun/vanilla a jugadores offline u online con
 * protección anti-dupe y bloqueo de modalidades no-Survival (ej. Laboratorio).
 */
public final class CompensationModule implements CommandExecutor, TabCompleter, Listener {

    private static final int CHEST_SIZE = 54;
    private static final String ADMIN_PERM = "drakes.devolucion.admin";

    private final JavaPlugin plugin;
    private final CompensationStore store;
    private final Set<UUID> openSessions = ConcurrentHashMap.newKeySet();

    public CompensationModule(@Nonnull JavaPlugin plugin) {
        this.plugin = plugin;
        this.store = new CompensationStore(plugin.getDataFolder().toPath());
    }

    public void register() {
        PluginCommand devCmd = plugin.getCommand("devolucion");
        if (devCmd != null) {
            devCmd.setExecutor(this);
            devCmd.setTabCompleter(this);
        }
        PluginCommand claimCmd = plugin.getCommand("reclamar");
        if (claimCmd != null) {
            claimCmd.setExecutor(this);
            claimCmd.setTabCompleter(this);
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("[Devoluciones] Módulo de Cofre de Devoluciones (/devolucion, /reclamar) activado.");
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof CompensationChestHolder holder) {
                persistClosedChest(player, player.getOpenInventory().getTopInventory(), holder);
                player.closeInventory();
            }
        }
        openSessions.clear();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (name.equals("reclamar")) {
            return handleReclamar(sender);
        }
        if (name.equals("devolucion")) {
            return handleDevolucion(sender, args);
        }
        return false;
    }

    private boolean handleReclamar(@Nonnull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix().append(Component.text("Este comando solo puede usarlo un jugador.", NamedTextColor.RED)));
            return true;
        }
        String worldName = player.getWorld().getName();
        if (!CompensationStore.isWorldAllowed(worldName)) {
            player.sendMessage(prefix().append(Component.text(
                "No puedes abrir tu Cofre de Devoluciones en '" + worldName + "'. Ve a Survival (mundo principal) y usa /reclamar.",
                NamedTextColor.RED
            )));
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (!openSessions.add(uuid)) {
            player.sendMessage(prefix().append(Component.text("Ya tienes tu Cofre de Devoluciones abierto.", NamedTextColor.YELLOW)));
            return true;
        }

        try {
            store.migrateNickToUuid(player.getName(), uuid);
            String key = CompensationStore.keyForUuid(uuid);
            List<ItemStack> allItems = store.loadItems(key);
            if (allItems.isEmpty()) {
                openSessions.remove(uuid);
                player.sendMessage(prefix().append(Component.text("No tienes ítems pendientes en tu Cofre de Devoluciones.", NamedTextColor.GRAY)));
                return true;
            }

            int showCount = Math.min(CHEST_SIZE, allItems.size());
            List<ItemStack> displayed = allItems.subList(0, showCount);
            List<ItemStack> overflow = allItems.size() > CHEST_SIZE
                ? new ArrayList<>(allItems.subList(CHEST_SIZE, allItems.size()))
                : Collections.emptyList();

            CompensationChestHolder holder = new CompensationChestHolder(uuid, player.getName(), displayed.size(), overflow);
            Inventory inv = Bukkit.createInventory(
                holder,
                CHEST_SIZE,
                Component.text("Cofre de Devoluciones", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
            );
            holder.setInventory(inv);
            for (int i = 0; i < displayed.size(); i++) {
                inv.setItem(i, displayed.get(i).clone());
            }
            player.openInventory(inv);
            if (!overflow.isEmpty()) {
                player.sendMessage(prefix().append(Component.text(
                    "Mostrando los primeros " + showCount + " stacks. Al vaciarlos vuelve a usar /reclamar para los " + overflow.size() + " restantes.",
                    NamedTextColor.AQUA
                )));
            }
        } catch (Exception ex) {
            openSessions.remove(uuid);
            plugin.getLogger().log(Level.SEVERE, "[Devoluciones] Error abriendo cofre para " + player.getName(), ex);
            player.sendMessage(prefix().append(Component.text("Ocurrió un error al abrir tu cofre. Avisa al Staff.", NamedTextColor.RED)));
        }
        return true;
    }

    private boolean handleDevolucion(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!sender.hasPermission(ADMIN_PERM)) {
            sender.sendMessage(prefix().append(Component.text("No tienes permiso para usar /devolucion.", NamedTextColor.RED)));
            return true;
        }
        if (args.length < 1) {
            sendAdminHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "dar" -> handleAdminDar(sender, args);
            case "lote" -> handleAdminLote(sender, args);
            case "mano" -> handleAdminMano(sender, args);
            case "ver" -> handleAdminVer(sender, args);
            case "limpiar" -> handleAdminLimpiar(sender, args);
            case "cofre" -> handleAdminCofre(sender, args);
            default -> sendAdminHelp(sender);
        }
        return true;
    }

    private void handleAdminDar(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(prefix().append(Component.text("Uso: /devolucion dar <jugador> <SLIMEFUN_ID|MATERIAL> <cantidad> [motivo...]", NamedTextColor.YELLOW)));
            return;
        }
        String targetName = args[1];
        String itemId = args[2];
        int qty;
        try {
            qty = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(prefix().append(Component.text("Cantidad inválida: " + args[3], NamedTextColor.RED)));
            return;
        }
        String reason = args.length > 4 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "Devolución por bug";
        ItemStack template = CompensationItemResolver.resolveSingle(itemId);
        if (template == null) {
            sender.sendMessage(prefix().append(Component.text("Ítem desconocido (ni Slimefun ni vanilla): " + itemId, NamedTextColor.RED)));
            return;
        }
        try {
            List<ItemStack> stacks = CompensationItemResolver.splitIntoStacks(template, qty);
            enqueueForTarget(sender, targetName, stacks, reason);
        } catch (Exception ex) {
            sender.sendMessage(prefix().append(Component.text("Error: " + ex.getMessage(), NamedTextColor.RED)));
        }
    }

    private void handleAdminLote(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(prefix().append(Component.text("Uso: /devolucion lote <jugador> <ID:CANT,ID:CANT,...> [motivo...]", NamedTextColor.YELLOW)));
            return;
        }
        String targetName = args[1];
        String specStr = args[2];
        String reason = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "Devolución por bug (lote)";
        try {
            Map<String, Integer> parsed = CompensationItemResolver.parseBatchSpec(specStr);
            List<ItemStack> stacks = CompensationItemResolver.resolveBatch(parsed, CompensationItemResolver::resolveSingle);
            enqueueForTarget(sender, targetName, stacks, reason);
        } catch (Exception ex) {
            sender.sendMessage(prefix().append(Component.text("Error en lote: " + ex.getMessage(), NamedTextColor.RED)));
        }
    }

    private void handleAdminMano(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(prefix().append(Component.text("Solo un jugador puede usar /devolucion mano.", NamedTextColor.RED)));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(prefix().append(Component.text("Uso: /devolucion mano <jugador> [motivo...]", NamedTextColor.YELLOW)));
            return;
        }
        ItemStack hand = staff.getInventory().getItemInMainHand();
        if (hand.getType().isAir() || hand.getAmount() <= 0) {
            sender.sendMessage(prefix().append(Component.text("No tienes ningún ítem en tu mano principal.", NamedTextColor.RED)));
            return;
        }
        String targetName = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Devolución manual";
        try {
            enqueueForTarget(sender, targetName, List.of(hand.clone()), reason);
        } catch (Exception ex) {
            sender.sendMessage(prefix().append(Component.text("Error: " + ex.getMessage(), NamedTextColor.RED)));
        }
    }

    private void handleAdminVer(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix().append(Component.text("Uso: /devolucion ver <jugador>", NamedTextColor.YELLOW)));
            return;
        }
        String targetName = args[1];
        String key = resolveTargetKey(targetName);
        List<ItemStack> items = store.loadItems(key);
        List<String> notes = store.loadNotes(key);
        sender.sendMessage(prefix().append(Component.text(
            "Pendientes para " + targetName + " (" + key + "): " + items.size() + " stack(s).",
            NamedTextColor.AQUA
        )));
        for (String note : notes) {
            sender.sendMessage(Component.text("  • " + note, NamedTextColor.GRAY));
        }
    }

    private void handleAdminLimpiar(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix().append(Component.text("Uso: /devolucion limpiar <jugador>", NamedTextColor.YELLOW)));
            return;
        }
        String targetName = args[1];
        String key = resolveTargetKey(targetName);
        try {
            int cleared = store.clear(key, targetName, sender.getName());
            sender.sendMessage(prefix().append(Component.text(
                "Se limpiaron " + cleared + " stack(s) pendientes de " + targetName + ".",
                NamedTextColor.GREEN
            )));
        } catch (IOException ex) {
            sender.sendMessage(prefix().append(Component.text("Error al limpiar: " + ex.getMessage(), NamedTextColor.RED)));
        }
    }

    private void handleAdminCofre(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (args.length < 7) {
            sender.sendMessage(prefix().append(Component.text(
                "Uso: /devolucion cofre <jugador> <mundo> <x> <y> <z> <ID:CANT,...> [motivo...]",
                NamedTextColor.YELLOW
            )));
            return;
        }
        String targetName = args[1];
        World world = Bukkit.getWorld(args[2]);
        if (world == null) {
            sender.sendMessage(prefix().append(Component.text("Mundo no encontrado: " + args[2], NamedTextColor.RED)));
            return;
        }
        int x, y, z;
        try {
            x = Integer.parseInt(args[3]);
            y = Integer.parseInt(args[4]);
            z = Integer.parseInt(args[5]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(prefix().append(Component.text("Coordenadas inválidas.", NamedTextColor.RED)));
            return;
        }
        String reason = args.length > 7 ? String.join(" ", Arrays.copyOfRange(args, 7, args.length)) : "Cofre físico de devolución";
        List<ItemStack> stacks;
        try {
            Map<String, Integer> parsed = CompensationItemResolver.parseBatchSpec(args[6]);
            stacks = CompensationItemResolver.resolveBatch(parsed, CompensationItemResolver::resolveSingle);
        } catch (Exception ex) {
            sender.sendMessage(prefix().append(Component.text("Error en lote: " + ex.getMessage(), NamedTextColor.RED)));
            return;
        }
        if (stacks.size() > 27) {
            sender.sendMessage(prefix().append(Component.text(
                "Un cofre simple admite máximo 27 stacks (pediste " + stacks.size() + "). Usa /devolucion lote para el cofre virtual.",
                NamedTextColor.RED
            )));
            return;
        }

        Block block = world.getBlockAt(x, y, z);
        if (!block.getType().isAir()) {
            sender.sendMessage(prefix().append(Component.text(
                "El bloque en " + x + ", " + y + ", " + z + " no es aire (" + block.getType() + "). Abortado para no sobrescribir.",
                NamedTextColor.RED
            )));
            return;
        }

        block.setType(Material.CHEST, false);
        if (block.getState() instanceof Chest chest) {
            chest.customName(Component.text("Cofre de Devolución - " + targetName, NamedTextColor.GOLD));
            chest.update(true, false);
            for (ItemStack stack : stacks) {
                chest.getBlockInventory().addItem(stack);
            }
            try {
                store.logPhysicalChest(sender.getName(), targetName, world.getName() + ":" + x + "," + y + "," + z, stacks.size(), reason);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.WARNING, "[Devoluciones] No se pudo escribir log de cofre físico", ex);
            }
            sender.sendMessage(prefix().append(Component.text(
                "Cofre físico generado en " + world.getName() + " (" + x + ", " + y + ", " + z + ") con " + stacks.size() + " stack(s) para " + targetName + ".",
                NamedTextColor.GREEN
            )));
        }
    }

    private void enqueueForTarget(
        @Nonnull CommandSender sender,
        @Nonnull String targetName,
        @Nonnull List<ItemStack> stacks,
        @Nonnull String reason
    ) throws IOException {
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null && openSessions.contains(online.getUniqueId())) {
            sender.sendMessage(prefix().append(Component.text(
                targetName + " tiene abierto su Cofre de Devoluciones ahora mismo. Espera a que lo cierre y reintenta.",
                NamedTextColor.YELLOW
            )));
            return;
        }
        String key = resolveTargetKey(targetName);
        store.addItems(key, targetName, sender.getName(), reason, stacks);
        sender.sendMessage(prefix().append(Component.text(
            "Cargados " + stacks.size() + " stack(s) en el Cofre de Devoluciones de " + targetName + " (" + key + ").",
            NamedTextColor.GREEN
        )));
        if (online != null && online.isOnline()) {
            notifyPlayerPending(online, store.loadItems(key).size());
        }
    }

    @Nonnull
    private String resolveTargetKey(@Nonnull String targetName) {
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return CompensationStore.keyForUuid(online.getUniqueId());
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(targetName);
        if (cached != null) {
            return CompensationStore.keyForUuid(cached.getUniqueId());
        }
        return CompensationStore.keyForNick(targetName);
    }

    private void notifyPlayerPending(@Nonnull Player player, int stackCount) {
        player.sendMessage(prefix().append(Component.text(
            "Tienes " + stackCount + " stack(s) de devolución por bug esperándote. Usa ",
            NamedTextColor.GOLD
        )).append(Component.text("/reclamar", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text(" en Survival para abrir tu Cofre de Devoluciones.", NamedTextColor.GOLD)));
    }

    private void sendAdminHelp(@Nonnull CommandSender sender) {
        sender.sendMessage(prefix().append(Component.text("Comandos de Cofre de Devoluciones:", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("  /devolucion dar <jugador> <SLIMEFUN_ID|MATERIAL> <cant> [motivo]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /devolucion lote <jugador> <ID:CANT,ID:CANT,...> [motivo]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /devolucion mano <jugador> [motivo]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /devolucion ver <jugador> | limpiar <jugador>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /devolucion cofre <jugador> <mundo> <x> <y> <z> <ID:CANT,...>", NamedTextColor.GRAY));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CompensationChestHolder)) {
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }
        boolean clickedTop = clicked.equals(top);
        InventoryAction action = event.getAction();

        // Bloquear meter ítems desde el inventario del jugador al cofre (solo-retiro anti-dupe)
        if (!clickedTop) {
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
            return;
        }

        // En el inventario superior (cofre): bloquear colocar cursor o intercambiar con ítem del jugador
        ItemStack cursor = event.getCursor();
        boolean hasCursor = cursor != null && !cursor.getType().isAir();
        if (hasCursor) {
            event.setCancelled(true);
            return;
        }
        if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
            int btn = event.getHotbarButton();
            if (btn >= 0 && event.getWhoClicked() instanceof Player p) {
                ItemStack hotbarItem = p.getInventory().getItem(btn);
                if (hotbarItem != null && !hotbarItem.getType().isAir()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(@Nonnull InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CompensationChestHolder)) {
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@Nonnull InventoryCloseEvent event) {
        Inventory top = event.getInventory();
        if (!(top.getHolder() instanceof CompensationChestHolder holder)) {
            return;
        }
        if (event.getPlayer() instanceof Player player) {
            persistClosedChest(player, top, holder);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@Nonnull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Inventory top = player.getOpenInventory().getTopInventory();
        if (top.getHolder() instanceof CompensationChestHolder holder) {
            persistClosedChest(player, top, holder);
        }
        openSessions.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@Nonnull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            try {
                store.migrateNickToUuid(player.getName(), player.getUniqueId());
                int count = store.loadItems(CompensationStore.keyForUuid(player.getUniqueId())).size();
                if (count > 0) {
                    notifyPlayerPending(player, count);
                }
            } catch (IOException ex) {
                plugin.getLogger().log(Level.WARNING, "[Devoluciones] Error revisando devoluciones de " + player.getName(), ex);
            }
        }, 60L);
    }

    private void persistClosedChest(@Nonnull Player player, @Nonnull Inventory top, @Nonnull CompensationChestHolder holder) {
        if (!openSessions.remove(holder.getOwnerId())) {
            return;
        }
        List<ItemStack> remaining = new ArrayList<>();
        int remainingDisplayed = 0;
        for (ItemStack item : top.getContents()) {
            if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
                remaining.add(item.clone());
                remainingDisplayed++;
            }
        }
        remaining.addAll(holder.getOverflowStacks());
        int claimed = Math.max(0, holder.getInitialDisplayedStacks() - remainingDisplayed);
        try {
            store.replaceItems(
                CompensationStore.keyForUuid(holder.getOwnerId()),
                holder.getOwnerName(),
                player.getName(),
                remaining,
                claimed
            );
            if (claimed > 0 && remaining.isEmpty()) {
                player.sendMessage(prefix().append(Component.text(
                    "¡Listo! Reclamaste todos los ítems de tu Cofre de Devoluciones.",
                    NamedTextColor.GREEN
                )));
            } else if (claimed > 0) {
                player.sendMessage(prefix().append(Component.text(
                    "Reclamaste " + claimed + " stack(s). Quedan " + remaining.size() + " guardados en /reclamar.",
                    NamedTextColor.AQUA
                )));
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "[Devoluciones] Error guardando estado al cerrar cofre de " + player.getName(), ex);
        }
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        if (!command.getName().equalsIgnoreCase("devolucion") || !sender.hasPermission(ADMIN_PERM)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filterPrefix(List.of("dar", "lote", "mano", "ver", "limpiar", "cofre"), args[0]);
        }
        if (args.length == 2) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return filterPrefix(players, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("cofre")) {
            List<String> worlds = Bukkit.getWorlds().stream().map(World::getName).toList();
            return filterPrefix(worlds, args[2]);
        }
        return Collections.emptyList();
    }

    @Nonnull
    private static List<String> filterPrefix(@Nonnull List<String> options, @Nonnull String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }

    @Nonnull
    private static Component prefix() {
        return Component.text("[Devoluciones] ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);
    }
}
