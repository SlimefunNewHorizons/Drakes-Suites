package com.drakescraft.suites.combat.luckyblocks;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

public class WorldGenerator implements Listener {

    private final LuckyBlocksModule module;
    private final List<String> blacklist;
    private final int chance;

    public WorldGenerator(LuckyBlocksModule module) {
        this.module = module;
        this.blacklist = module.getConfig().getStringList("world-blacklist");
        this.chance = module.getConfig().getInt("chance", 2);
    }

    @EventHandler
    public void onRandomSpawn(ChunkPopulateEvent e) {
        if (!module.isEnabled() || LuckyBlocksModule.isStandaloneSlimefunLuckyBlocksPresent()) {
            return;
        }
        if (blacklist != null && blacklist.contains(e.getWorld().getName())) {
            return;
        }

        Random random = ThreadLocalRandom.current();

        if (random.nextInt(100) < chance) {
            int x = e.getChunk().getX() * 16 + random.nextInt(16);
            int z = e.getChunk().getZ() * 16 + random.nextInt(16);
            int y = e.getWorld().getHighestBlockYAt(x, z);

            Block current = e.getWorld().getBlockAt(x, y, z);
            if (!current.getType().isSolid() && current.getRelative(BlockFace.DOWN).getType().isSolid()) {
                module.spawnLuckyBlock(current);
            }
        }
    }

}
