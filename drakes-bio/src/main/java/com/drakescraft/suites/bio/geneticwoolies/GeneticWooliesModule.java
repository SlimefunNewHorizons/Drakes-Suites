package com.drakescraft.suites.bio.geneticwoolies;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Módulo nativo Genetic Woolies para DrakesBio.
 * Implementa ovejas genéticas con herencia mendeliana de color (16 DyeColor),
 * rendimiento de lana (1..3), pureza/mutación y persistencia segura vía PDC.
 */
public class GeneticWooliesModule extends AbstractSuiteModule implements Listener {

    private static final long SHEAR_ANTIDUPE_WINDOW_MS = 1_000L;

    private NamespacedKey dnaKey;
    private double mutationChance = 0.25;
    private int maxBonusWool = 2;
    private boolean applyCustomName = true;
    private Set<String> worldsExcluded = new HashSet<>(List.of("laboratorio"));
    private final Map<UUID, Long> recentShearTimestamps = new ConcurrentHashMap<>();

    public GeneticWooliesModule(JavaPlugin plugin) {
        super(plugin, "genetic_woolies", "Genetic Woolies Mendelian Sheep");
        this.dnaKey = new NamespacedKey(plugin, "wool_dna");
    }

    @Override
    public void onEnable() {
        this.dnaKey = new NamespacedKey(plugin, "wool_dna");
        this.mutationChance = clampChance(config.getDouble("mutation-chance", 0.25));
        this.maxBonusWool = Math.max(0, Math.min(4, config.getInt("max-bonus-wool", 2)));
        this.applyCustomName = config.getBoolean("apply-custom-name", true);

        Set<String> excluded = new HashSet<>();
        List<String> configuredWorlds = config.getStringList("worlds-excluded");
        if (configuredWorlds.isEmpty()) {
            excluded.add("laboratorio");
        } else {
            for (String w : configuredWorlds) {
                if (w != null && !w.isBlank()) {
                    excluded.add(w.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        this.worldsExcluded = excluded;
        this.recentShearTimestamps.clear();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("Genetic Woolies habilitado. Colores: " + WoolColorBreedRegistry.getRegisteredColorCount()
                + " | Reglas de cruce: " + WoolColorBreedRegistry.getRuleCount()
                + " | Mutación: " + String.format(Locale.ROOT, "%.1f", mutationChance * 100.0) + "%"
                + " | Max bonus lana: " + maxBonusWool
                + " | Mundos excluidos: " + worldsExcluded);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        recentShearTimestamps.clear();
        logInfo("Genetic Woolies deshabilitado limpiamente.");
    }

    @Nonnull
    public NamespacedKey getDnaKey() {
        return dnaKey;
    }

    public double getMutationChance() {
        return mutationChance;
    }

    public int getMaxBonusWool() {
        return maxBonusWool;
    }

    public boolean isApplyCustomName() {
        return applyCustomName;
    }

    @Nonnull
    public Set<String> getWorldsExcluded() {
        return Collections.unmodifiableSet(worldsExcluded);
    }

    public boolean isWorldExcluded(@Nullable World world) {
        if (world == null) {
            return true;
        }
        String name = world.getName().toLowerCase(Locale.ROOT);
        for (String rule : worldsExcluded) {
            if (rule.equals(name) || name.startsWith(rule + "_")) {
                return true;
            }
            if (rule.endsWith("*") && name.startsWith(rule.substring(0, rule.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public WoolDNA readDna(@Nonnull Sheep sheep) {
        return WoolDNA.readFrom(sheep, dnaKey);
    }

    public void writeDna(@Nonnull Sheep sheep, @Nonnull WoolDNA dna) {
        dna.writeTo(sheep, dnaKey);
    }

    @Nonnull
    public WoolDNA breed(@Nonnull WoolDNA parentA, @Nonnull WoolDNA parentB) {
        return breed(parentA, parentB, ThreadLocalRandom.current());
    }

    @Nonnull
    public WoolDNA breed(@Nonnull WoolDNA parentA, @Nonnull WoolDNA parentB, @Nonnull Random random) {
        return parentA.breed(parentB, this.mutationChance, random);
    }

    /**
     * Calcula la cantidad de lana extra a otorgar al esquilar una oveja genética,
     * aplicando el tope anti-dupe {@code max-bonus-wool}.
     */
    public int calculateBonusWool(@Nullable WoolDNA dna) {
        if (dna == null) {
            return 0;
        }
        int rawBonus = Math.max(0, dna.getYield() - 1);
        return Math.min(rawBonus, this.maxBonusWool);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getEntity() instanceof Sheep child)) {
            return;
        }
        if (!(event.getMother() instanceof Sheep mother) || !(event.getFather() instanceof Sheep father)) {
            return;
        }
        if (isWorldExcluded(child.getWorld())) {
            return;
        }

        WoolDNA motherDna = readDna(mother);
        if (motherDna == null) {
            motherDna = WoolDNA.fromVanillaSheep(mother);
        }
        WoolDNA fatherDna = readDna(father);
        if (fatherDna == null) {
            fatherDna = WoolDNA.fromVanillaSheep(father);
        }

        WoolDNA childDna = breed(motherDna, fatherDna);
        applyDnaToSheep(child, childDna);
    }

    /**
     * Aplica el fenotipo, PDC y nombre sutil opcional a una cría o oveja genética.
     */
    public void applyDnaToSheep(@Nonnull Sheep sheep, @Nonnull WoolDNA dna) {
        DyeColor expressed = dna.getExpressedColor();
        sheep.setColor(expressed);
        writeDna(sheep, dna);

        if (applyCustomName) {
            String colorLabel = formatColorName(expressed);
            Component label = Component.text("Baby Woolie ", NamedTextColor.GRAY)
                    .append(Component.text("[" + colorLabel + " · Y" + dna.getYield() + "]", NamedTextColor.AQUA));
            sheep.customName(label);
            sheep.setCustomNameVisible(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerShear(PlayerShearEntityEvent event) {
        if (event.getEntity() instanceof Sheep sheep) {
            handleShearBonus(sheep);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockShear(BlockShearEntityEvent event) {
        if (event.getEntity() instanceof Sheep sheep) {
            handleShearBonus(sheep);
        }
    }

    /**
     * Otorga el bonus de lana del color genético respetando mundos excluidos
     * y protecciones anti-dupe (oveja ya esquilada / ventana de doble disparo).
     */
    int handleShearBonus(@Nonnull Sheep sheep) {
        if (sheep.isDead() || sheep.isSheared() || isWorldExcluded(sheep.getWorld())) {
            return 0;
        }
        WoolDNA dna = readDna(sheep);
        int bonus = calculateBonusWool(dna);
        if (bonus <= 0 || dna == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        UUID id = sheep.getUniqueId();
        Long last = recentShearTimestamps.get(id);
        if (last != null && (now - last) < SHEAR_ANTIDUPE_WINDOW_MS) {
            return 0;
        }
        recentShearTimestamps.put(id, now);
        pruneShearCacheIfNeeded(now);

        Material woolMaterial = WoolColorBreedRegistry.getWoolMaterial(dna.getExpressedColor());
        ItemStack bonusStack = new ItemStack(woolMaterial, bonus);
        sheep.getWorld().dropItemNaturally(sheep.getLocation(), bonusStack);
        return bonus;
    }

    private void pruneShearCacheIfNeeded(long now) {
        if (recentShearTimestamps.size() > 512) {
            recentShearTimestamps.entrySet().removeIf(e -> (now - e.getValue()) > SHEAR_ANTIDUPE_WINDOW_MS * 10);
        }
    }

    private static double clampChance(double value) {
        if (Double.isNaN(value) || value < 0.0) {
            return 0.0;
        }
        return Math.min(1.0, value);
    }

    private static String formatColorName(@Nonnull DyeColor color) {
        String[] words = color.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            if (!words[i].isEmpty()) {
                sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
            }
        }
        return sb.toString();
    }
}
