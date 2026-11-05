package com.drakescraft.suites.core.runtime;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shared runtime registry for item-granted flight (e.g. Angel Gem) and permanent flight permissions,
 * preventing area flight blocks (such as Antigravity / Flying Bubbles) from revoking protected flight.
 */
public final class FlightStateRegistry {

    private static final Set<UUID> PROTECTED_FLIGHT_USERS = Collections.synchronizedSet(new HashSet<>());

    private FlightStateRegistry() {
    }

    public static void markProtectedFlight(UUID uuid) {
        if (uuid != null) {
            PROTECTED_FLIGHT_USERS.add(uuid);
        }
    }

    public static void unmarkProtectedFlight(UUID uuid) {
        if (uuid != null) {
            PROTECTED_FLIGHT_USERS.remove(uuid);
        }
    }

    public static boolean isRegisteredProtected(UUID uuid) {
        return uuid != null && PROTECTED_FLIGHT_USERS.contains(uuid);
    }

    public static void clearAll() {
        PROTECTED_FLIGHT_USERS.clear();
    }

    public static boolean hasPermanentFlightPermission(Player player) {
        if (player == null) {
            return false;
        }
        return player.hasPermission("cmi.command.fly")
                || player.hasPermission("essentials.fly")
                || player.hasPermission("drakescraft.vip.fly");
    }

    /**
     * Returns true when area-based flight emitters must NOT disable the player's flight.
     */
    public static boolean isFlightProtected(Player player) {
        if (player == null) {
            return false;
        }
        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
            return true;
        }
        return isRegisteredProtected(player.getUniqueId()) || hasPermanentFlightPermission(player);
    }
}
