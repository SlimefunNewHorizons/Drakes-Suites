package com.drakescraft.suites.combat.luckyblocks.surprises.lucky;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;

public final class IronBlockSurprise implements Surprise {
	
	@Override
	public String getName() {
		return "Iron Block";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().spawnFallingBlock(l.add(0.5, 5, 0.5), Material.IRON_BLOCK.createBlockData());
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.LUCKY;
	}

}
