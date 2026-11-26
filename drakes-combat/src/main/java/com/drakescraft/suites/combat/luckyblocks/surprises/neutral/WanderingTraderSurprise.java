package com.drakescraft.suites.combat.luckyblocks.surprises.neutral;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;

public final class WanderingTraderSurprise implements Surprise {
	
	@Override
	public String getName() {
		return "Wandering Trader";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().spawnEntity(l, EntityType.WANDERING_TRADER);
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.NEUTRAL;
	}

}
