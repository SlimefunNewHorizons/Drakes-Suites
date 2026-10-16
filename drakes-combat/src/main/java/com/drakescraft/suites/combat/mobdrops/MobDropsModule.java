package com.drakescraft.suites.combat.mobdrops;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Módulo SFMobDrops para DrakesCombat.
 * Permite calibrar tablas de botín y drops personalizados de Slimefun al abatir mobs.
 * Compatible con Paper 1.21.11 (Java 21) y Purpur 26.X (Java 21/25).
 */
public class MobDropsModule extends AbstractSuiteModule implements Listener {

    private final Map<EntityType, List<MobDropRule>> dropRules = new ConcurrentHashMap<>();

    public MobDropsModule(JavaPlugin plugin) {
        super(plugin, "sfmobdrops", "SFMobDrops Custom Loot Tables");
    }

    @Override
    public void onEnable() {
        dropRules.clear();
        loadDefaultRules();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("SFMobDrops cargado con reglas para " + dropRules.size() + " tipos de entidades.");
    }

    @Override
    public void onDisable() {
        dropRules.clear();
    }

    private void loadDefaultRules() {
        // Reglas canónicas por defecto para mobs habituales
        addRule(new MobDropRule(EntityType.ZOMBIE, "IRON_DUST", Material.IRON_NUGGET, 15.0, 1, 2));
        addRule(new MobDropRule(EntityType.SKELETON, "COPPER_DUST", Material.BONE, 12.0, 1, 2));
        addRule(new MobDropRule(EntityType.CREEPER, "SULFUR", Material.GUNPOWDER, 20.0, 1, 3));
        addRule(new MobDropRule(EntityType.SPIDER, "SILVER_DUST", Material.STRING, 10.0, 1, 1));
        addRule(new MobDropRule(EntityType.ENDERMAN, "ENDER_LUMP_1", Material.ENDER_PEARL, 8.0, 1, 1));
        addRule(new MobDropRule(EntityType.BLAZE, "MAGNESIUM_DUST", Material.BLAZE_POWDER, 15.0, 1, 2));
        addRule(new MobDropRule(EntityType.WITHER_SKELETON, "CARBON", Material.COAL, 25.0, 1, 2));
    }

    public void addRule(MobDropRule rule) {
        dropRules.computeIfAbsent(rule.getEntityType(), k -> new ArrayList<>()).add(rule);
    }

    public List<MobDropRule> getRulesFor(EntityType type) {
        return dropRules.getOrDefault(type, Collections.emptyList());
    }

    public Map<EntityType, List<MobDropRule>> getAllRules() {
        return Collections.unmodifiableMap(dropRules);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        List<MobDropRule> rules = dropRules.get(entity.getType());
        if (rules == null || rules.isEmpty()) {
            return;
        }

        for (MobDropRule rule : rules) {
            double roll = ThreadLocalRandom.current().nextDouble(100.0);
            if (roll <= rule.getChancePercent()) {
                int amount = ThreadLocalRandom.current().nextInt(rule.getMinAmount(), rule.getMaxAmount() + 1);
                ItemStack drop = createDropItem(rule, amount);
                if (drop != null) {
                    e.getDrops().add(drop);
                }
            }
        }
    }

    public ItemStack createDropItem(MobDropRule rule, int amount) {
        ItemStack item = new ItemStack(rule.getFallbackMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            CrossVersionAdapter.setDisplayName(meta, "<gradient:#ffaa00:#ff5500>" + formatName(rule.getSlimefunId()) + "</gradient>");
            CrossVersionAdapter.setLore(meta, Arrays.asList(
                    "&7Loot recovered from fallen creatures.",
                    "&8» &fID: &e" + rule.getSlimefunId()
            ));
            SuiteItemPdcBridge.setSlimefunId(meta, rule.getSlimefunId());
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatName(String id) {
        if (id == null) return "Unknown Item";
        String[] parts = id.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
