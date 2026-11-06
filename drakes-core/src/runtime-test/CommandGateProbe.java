package com.drakescraft.suites.core.probe;

import com.drakescraft.suites.core.DrakesCorePlugin;
import com.drakescraft.suites.core.command.CommandModalityGate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

/** Synthetic events on a disposable real server, without players or production data. */
public class CommandGateProbe extends JavaPlugin {
    private int checks;

    @Override
    public void onEnable() {
        if (!Boolean.getBoolean("drakes.probe.commandgate")) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getScheduler().runTask(this, () -> {
            try {
                runChecks();
                getLogger().info("COMMAND_GATE_PROBE PASS checks=" + checks);
            } catch (Throwable failure) {
                getLogger().severe("COMMAND_GATE_PROBE FAIL " + failure);
            } finally {
                Bukkit.shutdown();
            }
        });
    }

    private void runChecks() {
        DrakesCorePlugin core = DrakesCorePlugin.getInstance();
        if (core == null || !core.isEnabled()) throw new AssertionError("Core not enabled");
        CommandModalityGate gate = core.getCommandGate();
        String[][] cases = {
                {"clasico", "/sf"}, {"clasico_nether", "/slimefun:sf guide"},
                {"clasico_the_end", "/sf"}, {"world_clasico", "/sf"},
                {"oneblock_world", "/is"}, {"oneblock_world_nether", "/bskyblock:is"},
                {"oneblock_world_the_end", "/is"}, {"world_oneblock", "/is"},
                {"bskyblock_world", "/ob"}, {"bskyblock_world_nether", "/oneblock:ob"},
                {"bskyblock_world_the_end", "/ob"}, {"world_skyblock", "/ob"},
                {"laboratorio", "/sf cheat STONE"}, {"laboratorio", "/slimefun:sf give DIAMOND"}
        };
        AtomicBoolean bypass = new AtomicBoolean();
        for (String[] test : cases) {
            Player player = playerIn(test[0], bypass);
            checkEvent(player, test[1], true);
            bypass.set(true);
            checkEvent(player, test[1], false);
            bypass.set(false);
        }
        checkEvent(playerIn("world", bypass), "/sf guide", false);
        checkEvent(playerIn("laboratorio", bypass), "/sf guide", false);
        checkEvent(playerIn("laboratorio", bypass), "/sf giveaway", false);
        gate.setEnabled(false);
        checkEvent(playerIn("clasico", bypass), "/sf", false);
        gate.setEnabled(true);
        for (String world : new String[]{"clasico", "clasico_nether", "clasico_the_end",
                "world_clasico", "world_clasico_nether", "world_clasico_the_end"}) {
            if (core.getWorldFilter().isSlimefunAllowed(world)) throw new AssertionError(world);
            checks++;
        }
    }

    private void checkEvent(Player player, String command, boolean expected) {
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() != expected) throw new AssertionError(command + ": " + expected);
        checks++;
    }

    private Player playerIn(String worldName, AtomicBoolean bypass) {
        World world = (World) Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> method.getName().equals("getName") ? worldName : null);
        return (Player) Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServer" -> Bukkit.getServer();
                    case "getWorld" -> world;
                    case "hasPermission" -> bypass.get();
                    case "isOnline" -> true;
                    case "getName" -> "CommandGateProbe";
                    default -> null;
                });
    }
}
