package com.drakescraft.suites.combat.warfare;

import com.drakescraft.suites.core.pdc.SuiteItemPdcBridge;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registro de items, armamento balístico, exoesqueletos y municiones de SlimefunWarfare.
 * Preserva 100% las claves PDC de Slimefun: `slimefun:slimefun_item`.
 */
@Getter
public class WarfareRegistry {

    private final Set<String> weapons = new HashSet<>();
    private final Set<String> powerSuits = new HashSet<>();
    private final Set<String> machines = new HashSet<>();
    private final Set<String> minerals = new HashSet<>();
    private final Map<String, Double> weaponDamage = new HashMap<>();

    public WarfareRegistry() {
        registerWeapons();
        registerPowerSuits();
        registerMachines();
        registerMinerals();
    }

    private void registerWeapons() {
        Collections.addAll(weapons,
                "ENERGY_BLADE",
                "ENERGY_RIFLE",
                "PISTOL",
                "SHOTGUN",
                "SNIPER_RIFLE",
                "GRENADE",
                "NUCLEAR_BOMB",
                "BULLET",
                "RADIO"
        );

        weaponDamage.put("ENERGY_BLADE", 12.0);
        weaponDamage.put("ENERGY_RIFLE", 14.0);
        weaponDamage.put("PISTOL", 7.0);
        weaponDamage.put("SHOTGUN", 15.0);
        weaponDamage.put("SNIPER_RIFLE", 22.0);
        weaponDamage.put("GRENADE", 25.0);
    }

    private void registerPowerSuits() {
        Collections.addAll(powerSuits,
                "POWER_SUIT_HELMET",
                "POWER_SUIT_CHESTPLATE",
                "POWER_SUIT_LEGGINGS",
                "POWER_SUIT_BOOTS",
                "MODULE_MANIPULATOR"
        );
    }

    private void registerMachines() {
        Collections.addAll(machines,
                "ELEMENTAL_REACTOR",
                "BOOMINATOR_9000",
                "EXPLOSIVE_SYNTHESIZER",
                "BULLET_PRESS",
                "AIR_LIQUEFIER",
                "METEOR_ATTRACTOR"
        );
    }

    private void registerMinerals() {
        Collections.addAll(minerals,
                "ARSENIC",
                "MONAZITE",
                "LANTHANUM"
        );
    }

    public boolean isWarfareWeapon(String id) {
        return id != null && weapons.contains(id.toUpperCase());
    }

    public boolean isPowerSuitPiece(String id) {
        return id != null && powerSuits.contains(id.toUpperCase());
    }

    public boolean isWarfareMachine(String id) {
        return id != null && machines.contains(id.toUpperCase());
    }

    public double getWeaponDamage(String id) {
        if (id == null) return 5.0;
        return weaponDamage.getOrDefault(id.toUpperCase(), 5.0);
    }

    public ItemStack createEnergyBlade() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b⚡ Quantum Energy Blade");
            meta.getPersistentDataContainer().set(SuiteItemPdcBridge.SLIMEFUN_ITEM_KEY, PersistentDataType.STRING, "ENERGY_BLADE");
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBullet(int amount) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7.50 Caliber Ballistic Round");
            meta.getPersistentDataContainer().set(SuiteItemPdcBridge.SLIMEFUN_ITEM_KEY, PersistentDataType.STRING, "BULLET");
            item.setItemMeta(meta);
        }
        return item;
    }
}
