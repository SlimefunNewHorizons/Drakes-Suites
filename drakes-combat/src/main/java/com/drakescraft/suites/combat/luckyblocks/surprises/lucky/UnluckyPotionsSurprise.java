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

public final class UnluckyPotionsSurprise implements Surprise {
	
	@Override
	public String getName() {
		return "Unlucky Potions";
	}

	@Override
	public void activate(Random random, Player p, Location l) {
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.GREEN, new PotionEffect(PotionEffectType.POISON, 45, 0), false));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.PURPLE, new PotionEffect(PotionEffectType.WEAKNESS, 90, 0), false));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.GRAY, new PotionEffect(PotionEffectType.SLOWNESS, 90, 0), false));
		l.getWorld().dropItemNaturally(l, LuckyBlocksModule.createPotion(Color.MAROON, new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 0, 0), false));
	}

	@Override
	public LuckLevel getLuckLevel() {
		return LuckLevel.LUCKY;
	}

}
