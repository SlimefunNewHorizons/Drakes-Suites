package com.drakescraft.suites.combat.luckyblocks.surprises.neutral;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;

public final class PotatOSSurprise implements Surprise {
	
	@Override
	public String getName() {
		return "PotatOS";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().dropItemNaturally(l, new CustomItemStack(Material.POTATO, "&e&lPotatOS"));
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.NEUTRAL;
	}

}
