package com.drakescraft.suites.bio.flowerpower;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Catálogo canónico de ítems, flores brillantes, amuletos y maquinaria vegetal de FlowerPower.
 */
public final class FlowerPowerItemsRegistry {

    private static final Map<String, ItemStack> ITEMS = new HashMap<>();

    static {
        // Multi-bloques y Contenedores
        register("MAGIC_BASIN", createItem(Material.CAULDRON, "MAGIC_BASIN",
                ChatColor.AQUA + "" + ChatColor.BOLD + "Cuenca Mágica",
                "Receptáculo místico para la síntesis de artefactos botánicos.",
                "Haz clic derecho con una Varita Mágica para activarla."));
        register("EXPERIENCE_CAULDRON", createItem(Material.CAULDRON, "EXPERIENCE_CAULDRON",
                ChatColor.GREEN + "" + ChatColor.BOLD + "Caldero de Experiencia",
                "Almacena esferas de experiencia condensadas.",
                "Clic derecho para depositar, Agacharse + Clic derecho para retirar."));

        // Flores Centelleantes
        register("GLISTENING_POPPY", createGlowingItem(Material.POPPY, "GLISTENING_POPPY",
                ChatColor.RED + "Amapola Centelleante", "Irradia polen fosforescente enriquecido con bio-esencia."));
        register("GLISTENING_DANDELION", createGlowingItem(Material.DANDELION, "GLISTENING_DANDELION",
                ChatColor.YELLOW + "Diente de León Centelleante", "Flota con esporas solares cargadas de energía."));
        register("GLISTENING_OXEYE_DAISY", createGlowingItem(Material.OXEYE_DAISY, "GLISTENING_OXEYE_DAISY",
                ChatColor.WHITE + "Margarita Centelleante", "Refleja la luz lunar con una pureza cristalina."));
        register("GLISTENING_ALLIUM", createGlowingItem(Material.ALLIUM, "GLISTENING_ALLIUM",
                ChatColor.LIGHT_PURPLE + "Allium Centelleante", "Emite pulsos aromáticos que aceleran la floración circundante."));

        // Herramientas y Reactivos
        register("MAGICAL_WAND", createGlowingItem(Material.BLAZE_ROD, "MAGICAL_WAND",
                ChatColor.DARK_PURPLE + "Varita Mágica Botánica",
                "Canaliza el pulso de la Cuenca Mágica para iniciar transmutaciones."));
        register("MAGIC_CREAM", createGlowingItem(Material.MAGMA_CREAM, "MAGIC_CREAM",
                ChatColor.GOLD + "Crema Mágica Concentrada",
                "Sustancia viscosa con potentes cualidades catalizadoras botánicas."));
        register("OVERGROWTH_SEED", createGlowingItem(Material.WHEAT_SEEDS, "OVERGROWTH_SEED",
                ChatColor.DARK_AQUA + "Semilla de Sobrecrecimiento",
                "Al aplicarse sobre flores compatibles genera múltiples clones florecientes."));

        // Cristales Florales
        register("RED_CRYSTAL", createGlowingItem(Material.RED_GLAZED_TERRACOTTA, "RED_CRYSTAL",
                ChatColor.RED + "Cristal Rubí Floral", "Cristalización de pétalos carmesí sometidos a alta presión."));
        register("YELLOW_CRYSTAL", createGlowingItem(Material.YELLOW_GLAZED_TERRACOTTA, "YELLOW_CRYSTAL",
                ChatColor.YELLOW + "Cristal Topacio Floral", "Condensado solar extraído del néctar de flores doradas."));
        register("WHITE_CRYSTAL", createGlowingItem(Material.WHITE_GLAZED_TERRACOTTA, "WHITE_CRYSTAL",
                ChatColor.WHITE + "Cristal Opalescente Floral", "Estructura mineralizada de rocío botánico puro."));
        register("PURPLE_CRYSTAL", createGlowingItem(Material.PURPLE_GLAZED_TERRACOTTA, "PURPLE_CRYSTAL",
                ChatColor.DARK_PURPLE + "Cristal Amatista Floral", "Imbuido con la resonancia aromática del allium místico."));

        // Amuletos (Charms)
        for (CharmType charm : CharmType.values()) {
            register(charm.getSlimefunId(), createGlowingItem(Material.SUGAR, charm.getSlimefunId(),
                    charm.getFormattedName(),
                    "Equip it in your offhand to gain its bonus.",
                    "Attribute: " + ChatColor.YELLOW + charm.getAttribute().getKey().getKey()));
        }

        // Artefactos Supremos
        register("EXPERIENCE_TOME", createGlowingItem(Material.ENCHANTED_BOOK, "EXPERIENCE_TOME",
                ChatColor.GOLD + "Tomo de Experiencia Ancestral",
                "Grimorio viviente capaz de contener hasta 1,000,000 de experiencia."));
        register("INFINITY_APPLE", createGlowingItem(Material.ENCHANTED_GOLDEN_APPLE, "INFINITY_APPLE",
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Manzana Infinita",
                "Fruta imperecedera que sacia el hambre eternamente sin consumirse jamás."));
        register("INFINITY_BANDAGE", createGlowingItem(Material.PAPER, "INFINITY_BANDAGE",
                ChatColor.GREEN + "" + ChatColor.BOLD + "Vendaje Infinito",
                "Compresa encantada que cicatriza heridas continuamente al portarse."));
        register("RECALL_CHARM", createGlowingItem(Material.ENDER_EYE, "RECALL_CHARM",
                ChatColor.AQUA + "Amuleto de Retorno",
                "Vincula tu alma a un santuario para regresar instantáneamente ante el peligro."));
    }

    private static void register(String id, ItemStack item) {
        ITEMS.put(id, item);
    }

    @Nullable
    public static ItemStack getItem(String id) {
        ItemStack item = ITEMS.get(id);
        return item != null ? item.clone() : null;
    }

    public static Map<String, ItemStack> getAllItems() {
        return Collections.unmodifiableMap(ITEMS);
    }

    private static ItemStack createItem(Material material, String slimefunId, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.GRAY + line);
                }
                meta.setLore(loreList);
            }
            SuiteItemPdcBridge.setSlimefunId(meta, slimefunId);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createGlowingItem(Material material, String slimefunId, String displayName, String... lore) {
        ItemStack item = createItem(material, slimefunId, displayName, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private FlowerPowerItemsRegistry() {}
}
