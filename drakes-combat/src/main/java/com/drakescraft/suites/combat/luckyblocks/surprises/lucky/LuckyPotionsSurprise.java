package com.drakescraft.suites.combat.luckyblocks.surprises.lucky;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.drakescraft.suites.combat.luckyblocks.LuckyBlocksModule;
import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;

public final class LuckyPotionsSurprise implements Surprise {
	
	@Override
	public String getName() {
		return "Lucky Potions";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.FUCHSIA, new PotionEffect(PotionEffectType.REGENERATION, 45, 0), true));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.SILVER, new PotionEffect(PotionEffectType.SPEED, 180, 0), true));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.ORANGE, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 180, 0), true));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.RED, new PotionEffect(PotionEffectType.INSTANT_HEALTH, 0, 0), true));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.NAVY, new PotionEffect(PotionEffectType.NIGHT_VISION, 180, 0), true));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.MAROON, new PotionEffect(PotionEffectType.STRENGTH, 180, 0), true));
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.LUCKY;
	}

}
