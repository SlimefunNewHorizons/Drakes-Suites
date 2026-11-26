package com.drakescraft.suites.combat.luckyblocks.surprises.lucky;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;

public final class GoldenAppleSurprise implements Surprise {
	
	@Override
	public String getName() {
		return "Golden Apples";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().dropItemNaturally(l, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
		l.getWorld().dropItemNaturally(l, new ItemStack(Material.GOLDEN_APPLE, 3));
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.LUCKY;
	}

}
