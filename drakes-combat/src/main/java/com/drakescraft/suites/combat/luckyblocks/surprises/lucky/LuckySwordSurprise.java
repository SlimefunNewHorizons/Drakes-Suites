package com.drakescraft.suites.combat.luckyblocks.surprises.lucky;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;

public final class LuckySwordSurprise implements Surprise {
	
	private final ItemStack sword;
	
	public LuckySwordSurprise() {
		sword = new CustomItemStack(Material.GOLDEN_SWORD, "&e&lLucky Sword");
		sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 10);
		sword.addUnsafeEnchantment(Enchantment.LOOTING, 10);
		sword.addUnsafeEnchantment(Enchantment.UNBREAKING, 10);
		sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 5);
	}
	
	@Override
	public String getName() {
		return "Lucky Sword";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().dropItemNaturally(l, sword.clone());
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.LUCKY;
	}

}
