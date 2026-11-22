package com.drakescraft.suites.server.compensation;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Almacén persistente, atómico y auditable de Cofres de Devolución por Bug.
 * Soporta entrega a jugadores offline (por UUID o nick en minúsculas con migración automática al conectar).
 */
public final class CompensationStore {

    private static final Set<String> BLOCKED_WORLD_TOKENS = Set.of(
        "laboratorio", "lab", "lobby", "hub", "oneblock", "skyblock", "creative", "acid", "bskyblock"
    );

    private final Path compensationsDir;
    private final Path auditLogFile;

    public CompensationStore(@Nonnull Path dataFolder) {
        this.compensationsDir = dataFolder.resolve("compensations");
        this.auditLogFile = dataFolder.resolve("compensations-audit.log");
    }

    /**
     * Protege contra reclamos en modalidades efímeras o aisladas (como Laboratorio).
     */
    public static boolean isWorldAllowed(@Nullable String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return false;
        }
        String lower = worldName.toLowerCase(Locale.ROOT);
        for (String token : BLOCKED_WORLD_TOKENS) {
            if (lower.equals(token) || lower.startsWith(token + "_") || lower.contains("_" + token)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public static String keyForUuid(@Nonnull UUID uuid) {
        return uuid.toString().toLowerCase(Locale.ROOT);
    }

    @Nonnull
    public static String keyForNick(@Nonnull String nick) {
        String safe = nick.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        if (safe.isEmpty()) {
            throw new IllegalArgumentException("Nombre de jugador inválido.");
        }
        return "nick_" + safe;
    }

    public synchronized void addItems(
        @Nonnull String key,
        @Nonnull String targetDisplay,
        @Nonnull String actor,
        @Nonnull String reason,
        @Nonnull List<ItemStack> itemsToAdd
    ) throws IOException {
        if (itemsToAdd.isEmpty()) {
            return;
        }
        List<ItemStack> current = new ArrayList<>(loadItems(key));
        for (ItemStack item : itemsToAdd) {
            if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
                current.add(item.clone());
            }
        }
        List<String> notes = new ArrayList<>(loadNotes(key));
        notes.add("[" + Instant.now() + "] " + actor + " (+" + itemsToAdd.size() + " stacks): " + reason);
        saveAll(key, targetDisplay, current, notes);
        appendAudit("ADD", actor, targetDisplay + " (" + key + ")", "stacks=" + itemsToAdd.size(), reason);
    }

    @Nonnull
    public synchronized List<ItemStack> loadItems(@Nonnull String key) {
        Path file = fileForKey(key);
        if (!Files.exists(file)) {
            return Collections.emptyList();
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        List<?> raw = yaml.getList("items");
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> items = new ArrayList<>();
        for (Object obj : raw) {
            if (obj instanceof ItemStack stack && !stack.getType().isAir() && stack.getAmount() > 0) {
                items.add(stack.clone());
            }
        }
        return items;
    }

    @Nonnull
    public synchronized List<String> loadNotes(@Nonnull String key) {
        Path file = fileForKey(key);
        if (!Files.exists(file)) {
            return Collections.emptyList();
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        return new ArrayList<>(yaml.getStringList("notes"));
    }

    public synchronized void replaceItems(
        @Nonnull String key,
        @Nonnull String targetDisplay,
        @Nonnull String actor,
        @Nonnull List<ItemStack> remainingItems,
        int claimedStacks
    ) throws IOException {
        List<ItemStack> clean = new ArrayList<>();
        for (ItemStack item : remainingItems) {
            if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
                clean.add(item.clone());
            }
        }
        if (clean.isEmpty()) {
            Files.deleteIfExists(fileForKey(key));
        } else {
            saveAll(key, targetDisplay, clean, loadNotes(key));
        }
        if (claimedStacks > 0) {
            appendAudit("CLAIM", actor, targetDisplay + " (" + key + ")", "claimed_stacks=" + claimedStacks + ",remaining=" + clean.size(), "reclamar");
        }
    }

    public synchronized int clear(@Nonnull String key, @Nonnull String targetDisplay, @Nonnull String actor) throws IOException {
        int previous = loadItems(key).size();
        Files.deleteIfExists(fileForKey(key));
        appendAudit("CLEAR", actor, targetDisplay + " (" + key + ")", "cleared_stacks=" + previous, "limpiar");
        return previous;
    }

    /**
     * Si había devoluciones guardadas por nick (jugador offline sin UUID cacheado),
     * las migra atómicamente al archivo de su UUID real al conectarse.
     */
    public synchronized int migrateNickToUuid(@Nonnull String playerName, @Nonnull UUID uuid) throws IOException {
        String nickKey = keyForNick(playerName);
        Path nickFile = fileForKey(nickKey);
        if (!Files.exists(nickFile)) {
            return 0;
        }
        List<ItemStack> nickItems = loadItems(nickKey);
        List<String> nickNotes = loadNotes(nickKey);
        if (nickItems.isEmpty()) {
            Files.deleteIfExists(nickFile);
            return 0;
        }
        String uuidKey = keyForUuid(uuid);
        List<ItemStack> mergedItems = new ArrayList<>(loadItems(uuidKey));
        mergedItems.addAll(nickItems);
        List<String> mergedNotes = new ArrayList<>(loadNotes(uuidKey));
        mergedNotes.addAll(nickNotes);
        saveAll(uuidKey, playerName, mergedItems, mergedNotes);
        Files.deleteIfExists(nickFile);
        appendAudit("MIGRATE", "SYSTEM", playerName + " (" + nickKey + " -> " + uuidKey + ")", "stacks=" + nickItems.size(), "auto-join");
        return nickItems.size();
    }

    public synchronized void logPhysicalChest(
        @Nonnull String actor,
        @Nonnull String targetDisplay,
        @Nonnull String locationDesc,
        int stacksCount,
        @Nonnull String reason
    ) throws IOException {
        appendAudit("CHEST", actor, targetDisplay, "loc=" + locationDesc + ",stacks=" + stacksCount, reason);
    }

    private void saveAll(
        @Nonnull String key,
        @Nonnull String targetDisplay,
        @Nonnull List<ItemStack> items,
        @Nonnull List<String> notes
    ) throws IOException {
        Files.createDirectories(compensationsDir);
        Path target = fileForKey(key);
        Path tmp = compensationsDir.resolve(key + ".yml.tmp");

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("target", targetDisplay);
        yaml.set("updatedAt", Instant.now().toString());
        yaml.set("notes", notes);
        yaml.set("items", items);
        yaml.save(tmp.toFile());

        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void appendAudit(
        @Nonnull String action,
        @Nonnull String actor,
        @Nonnull String target,
        @Nonnull String details,
        @Nonnull String reason
    ) throws IOException {
        Files.createDirectories(auditLogFile.getParent());
        String line = "[" + Instant.now() + "] " + action
            + " actor=" + sanitize(actor)
            + " target=" + sanitize(target)
            + " " + details
            + " reason=" + sanitize(reason)
            + System.lineSeparator();
        Files.writeString(
            auditLogFile,
            line,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }

    @Nonnull
    private static String sanitize(@Nonnull String s) {
        return s.replace('\n', ' ').replace('\r', ' ').trim();
    }

    @Nonnull
    Path fileForKey(@Nonnull String key) {
        return compensationsDir.resolve(key + ".yml");
    }
}
