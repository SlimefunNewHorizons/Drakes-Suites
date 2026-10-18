package com.drakescraft.suites.generators.liquid;

import com.drakescraft.suites.core.module.AbstractSuiteModule;
import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Módulo nativo Liquid (Fluid Dynamics & Molten Metal Processing) para DrakesGenerators.
 * Gestiona metales fundidos, cubos térmicos y fluidos industriales para la matriz energética.
 */
public class LiquidModule extends AbstractSuiteModule {

    private final Map<MoltenFluidType, ItemStack> itemCache = new EnumMap<>(MoltenFluidType.class);
    private boolean asyncThermodynamicsEnabled = true;
    private int maxFluidStorageMb = 16000;

    public LiquidModule(JavaPlugin plugin) {
        super(plugin, "liquid", "Liquid Hydrocarbons & Fluid Dynamics");
    }

    @Override
    public void onEnable() {
        this.asyncThermodynamicsEnabled = getConfig().getBoolean("performance.async-thermodynamics", true);
        this.maxFluidStorageMb = getConfig().getInt("settings.max-fluid-storage-mb", 16000);

        for (MoltenFluidType type : MoltenFluidType.values()) {
            itemCache.put(type, buildMoltenItem(type));
        }

        logInfo("LiquidModule habilitado con " + MoltenFluidType.values().length + " fluidos y metales fundidos.");
    }

    @Override
    public void onDisable() {
        itemCache.clear();
        logInfo("LiquidModule deshabilitado limpiamente.");
    }

    public ItemStack getMoltenItem(MoltenFluidType type) {
        ItemStack cached = itemCache.get(type);
        return cached != null ? cached.clone() : buildMoltenItem(type);
    }

    public ItemStack buildMoltenItem(MoltenFluidType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', type.getDisplayName()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Punto de Fusión: &6" + type.getMeltingPointCelsius() + " °C"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e♨ Fluido Térmico de Alta Densidad"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&8» &fID: &e" + type.getSlimefunId()));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        SuiteItemPdcBridge.setSlimefunId(item, type.getSlimefunId());
        return item;
    }

    public boolean isMoltenFluid(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String sfId = SuiteItemPdcBridge.getSlimefunId(item);
        return MoltenFluidType.getBySlimefunId(sfId) != null;
    }

    public boolean isAsyncThermodynamicsEnabled() {
        return asyncThermodynamicsEnabled;
    }

    public int getMaxFluidStorageMb() {
        return maxFluidStorageMb;
    }
}
