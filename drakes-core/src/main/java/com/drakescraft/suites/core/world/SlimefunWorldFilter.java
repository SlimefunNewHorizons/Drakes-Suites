package com.drakescraft.suites.core.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Filtro de restricción de mundos para Slimefun.
 * Garantiza que modalidades como Survival Clásico estén 100% aisladas de tecnología y magia Slimefun.
 */
public class SlimefunWorldFilter {

    private final Set<String> disabledWorlds = new HashSet<>();
    private String restrictionMessage = "<red>❌ Slimefun technology and magic are strictly disabled in this game mode.</red>";

    public SlimefunWorldFilter() {
    }

    public SlimefunWorldFilter(List<String> worlds, String message) {
        setDisabledWorlds(worlds);
        if (message != null && !message.isEmpty()) {
            this.restrictionMessage = message;
        }
    }

    public void reload(FileConfiguration config) {
        disabledWorlds.clear();
        if (config != null) {
            List<String> list = config.getStringList("slimefun.disabled-worlds");
            setDisabledWorlds(list);
            this.restrictionMessage = config.getString("slimefun.restriction-message", this.restrictionMessage);
        }
    }

    public void setDisabledWorlds(List<String> worlds) {
        disabledWorlds.clear();
        if (worlds != null) {
            for (String w : worlds) {
                if (w != null && !w.trim().isEmpty()) {
                    disabledWorlds.add(w.trim().toLowerCase());
                }
            }
        }
    }

    public boolean isSlimefunAllowed(World world) {
        if (world == null) {
            return true;
        }
        return isSlimefunAllowed(world.getName());
    }

    public boolean isSlimefunAllowed(Location location) {
        if (location == null || location.getWorld() == null) {
            return true;
        }
        return isSlimefunAllowed(location.getWorld().getName());
    }

    public boolean isSlimefunAllowed(String worldName) {
        if (worldName == null) {
            return true;
        }
        return !disabledWorlds.contains(worldName.toLowerCase());
    }

    public Set<String> getDisabledWorlds() {
        return Collections.unmodifiableSet(disabledWorlds);
    }

    public String getRestrictionMessage() {
        return restrictionMessage;
    }
}
