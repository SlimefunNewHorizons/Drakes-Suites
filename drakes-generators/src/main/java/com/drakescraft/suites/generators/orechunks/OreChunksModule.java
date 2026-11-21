package com.drakescraft.suites.generators.orechunks;

import com.drakescraft.suites.core.compat.CrossVersionAdapter;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Módulo Slimefun OreChunks para DrakesGenerators.
 * Consolida el procesamiento, enriquecimiento geológico y fundición de fragmentos minerales (Ore Chunks).
 * Preserva al 100% las claves PDC de Slimefun (ej. IRON_ORE_CHUNK, GOLD_ORE_CHUNK).
 * Compatible con Paper 1.21.11 (Java 21) y Purpur 26.X (Java 21/25).
 */
public class OreChunksModule extends AbstractSuiteModule implements Listener {

    private boolean naturalDropsEnabled = true;
    private double naturalDropChance = 5.0; // 5% chance al minar ores normales
    private final Map<OreChunkType, ItemStack> itemCache = new EnumMap<>(OreChunkType.class);

    public OreChunksModule(JavaPlugin plugin) {
        super(plugin, "ore_chunks", "Slimefun OreChunks Mineral Processing");
    }

    static boolean isStandaloneSlimefunOreChunksPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("SlimefunOreChunks") != null;
    }

    @Override
    public void onEnable() {
        this.naturalDropsEnabled = config.getBoolean("natural-drops-enabled", true);
        this.naturalDropChance = config.getDouble("natural-drop-chance-percent", 5.0);

        // Precalentar caché de ítems
        for (OreChunkType type : OreChunkType.values()) {
            itemCache.put(type, buildOreChunkItem(type));
        }

        if (isStandaloneSlimefunOreChunksPresent()) {
            logInfo("Plugin standalone SlimefunOreChunks detectado; cediendo listener de minería al addon dedicado.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logInfo("OreChunks cargado con 11 tipos de minerales canónicos. Drops naturales: " + naturalDropsEnabled);
    }

    @Override
    public void onDisable() {
        itemCache.clear();
    }

    public ItemStack getOreChunkItem(OreChunkType type) {
        ItemStack cached = itemCache.get(type);
        return cached != null ? cached.clone() : buildOreChunkItem(type);
    }

    public ItemStack buildOreChunkItem(OreChunkType type) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            CrossVersionAdapter.setDisplayName(skullMeta, "<gold>" + type.getDisplayName() + "</gold>");
            List<String> lore = Arrays.asList(
                    "&7Fragmento concentrado de mineral geológico.",
                    "&7Tratamiento en Trituradora o Fundición Slimefun.",
                    "",
                    "&8» &fRendimiento: &e+" + (type.getAmplifier() * 25) + "%",
                    "&8» &fID: &e" + type.getSlimefunId()
            );
            CrossVersionAdapter.setLore(skullMeta, lore);
            SuiteItemPdcBridge.setSlimefunId(skullMeta, type.getSlimefunId());

            // Aplicar perfil / skin de cabeza si el runtime lo soporta de forma segura
            applyHeadTexture(skullMeta, type.getTextureHash());
            item.setItemMeta(skullMeta);
        }
        return item;
    }

    private void applyHeadTexture(SkullMeta meta, String textureHash) {
        try {
            // Paper / Purpur 1.21.11+ y 26.X API moderna de PlayerProfile
            var playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
            var textures = playerProfile.getTextures();
            textures.setSkin(URI.create("http://textures.minecraft.net/texture/" + textureHash).toURL());
            playerProfile.setTextures(textures);
            meta.setPlayerProfile(playerProfile);
        } catch (Throwable t) {
            // Fallback grácil silencioso si no hay red o si la API es anterior
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOreMine(BlockBreakEvent e) {
        if (!naturalDropsEnabled || isStandaloneSlimefunOreChunksPresent()) return;
        Player player = e.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }
        Block b = e.getBlock();
        Material mat = b.getType();

        OreChunkType matched = matchOreBlock(mat);
        if (matched != null) {
            double roll = ThreadLocalRandom.current().nextDouble(100.0);
            if (roll <= naturalDropChance) {
                ItemStack drop = getOreChunkItem(matched);
                b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), drop);
            }
        }
    }

    private OreChunkType matchOreBlock(Material mat) {
        return switch (mat) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> OreChunkType.IRON;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> OreChunkType.GOLD;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> OreChunkType.COPPER;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> ThreadLocalRandom.current().nextBoolean() ? OreChunkType.ALUMINUM : OreChunkType.TIN;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> OreChunkType.COBALT;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> OreChunkType.NICKEL;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> OreChunkType.SILVER;
            default -> null;
        };
    }
}
