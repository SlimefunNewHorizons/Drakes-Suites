package com.drakescraft.suites.core.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Gateway de control y restricción de comandos por modalidad/mundo.
 * Asegura que comandos como /sf no se ejecuten en Clásico, o /is no se ejecute en OneBlock.
 */
public class CommandModalityGate implements Listener {

    public static class ModalityRule {
        private final String name;
        private final List<Pattern> worldPatterns = new ArrayList<>();
        private final List<String> blockedCommands = new ArrayList<>();
        private final List<String> allowedCommands = new ArrayList<>();
        private final boolean whitelistMode;
        private final String blockedMessage;

        public ModalityRule(String name, List<String> regexes, List<String> blockedCommands, List<String> allowedCommands, boolean whitelistMode, String blockedMessage) {
            this.name = name;
            this.whitelistMode = whitelistMode;
            if (regexes != null) {
                for (String regex : regexes) {
                    try {
                        this.worldPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
                    } catch (Exception ignored) {
                    }
                }
            }
            if (blockedCommands != null) {
                for (String cmd : blockedCommands) {
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        this.blockedCommands.add(normalizeCommandLine(cmd));
                    }
                }
            }
            if (allowedCommands != null) {
                for (String cmd : allowedCommands) {
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        this.allowedCommands.add(normalizeCommandLine(cmd));
                    }
                }
            }
            this.blockedMessage = blockedMessage != null ? blockedMessage : "<red>❌ Este comando no está disponible en esta modalidad.</red>";
        }

        public boolean matchesWorld(String worldName) {
            if (worldName == null) return false;
            for (Pattern pattern : worldPatterns) {
                if (pattern.matcher(worldName).matches()) {
                    return true;
                }
            }
            return false;
        }

        public boolean isCommandBlocked(String commandLine) {
            if (commandLine == null) return false;
            String trimmed = commandLine.trim();
            if (trimmed.isEmpty()) return false;

            String normalizedLine = normalizeCommandLine(trimmed);

            // En modo lista blanca, TODO comando no permitido explícitamente se bloquea
            if (whitelistMode) {
                boolean isAllowed = false;
                for (String allowed : allowedCommands) {
                    if (matchesCommand(normalizedLine, allowed)) {
                        isAllowed = true;
                        break;
                    }
                }
                if (!isAllowed) {
                    return true;
                }
            }

            // Comprobación de lista negra (funciona con y sin prefijo de plugin)
            for (String blocked : blockedCommands) {
                if (matchesCommand(normalizedLine, blocked)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean matchesCommand(String commandLine, String configuredCommand) {
            return commandLine.equals(configuredCommand) || commandLine.startsWith(configuredCommand + " ");
        }

        private static String normalizeCommandLine(String commandLine) {
            String[] parts = commandLine.trim().split("\\s+", 2);
            String label = normalizeCommandLabel(parts[0]);
            // Only the label has a plugin namespace; colons in arguments are literal.
            return parts.length == 1 ? label
                    : label + " " + parts[1].trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        }

        public static String normalizeCommandLabel(String token) {
            if (token == null) return "";
            String clean = token.trim().toLowerCase(Locale.ROOT);
            while (clean.startsWith("/")) {
                clean = clean.substring(1).trim();
            }
            int colonIdx = clean.lastIndexOf(':');
            if (colonIdx >= 0) {
                clean = clean.substring(colonIdx + 1).trim();
            }
            return "/" + clean;
        }

        public String getName() {
            return name;
        }

        public String getBlockedMessage() {
            return blockedMessage;
        }

        public List<String> getBlockedCommands() {
            return Collections.unmodifiableList(blockedCommands);
        }

        public List<String> getAllowedCommands() {
            return Collections.unmodifiableList(allowedCommands);
        }

        public boolean isWhitelistMode() {
            return whitelistMode;
        }
    }

    private final List<ModalityRule> rules = new ArrayList<>();
    private boolean enabled = true;

    public CommandModalityGate() {
    }

    public void reload(FileConfiguration config) {
        rules.clear();
        if (config == null) {
            return;
        }

        this.enabled = config.getBoolean("command-gate.enabled", true);
        ConfigurationSection section = config.getConfigurationSection("command-gate.rules");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection ruleSec = section.getConfigurationSection(key);
                if (ruleSec != null) {
                    List<String> patterns = ruleSec.getStringList("world-patterns");
                    List<String> blocked = ruleSec.getStringList("blocked-commands");
                    List<String> allowed = ruleSec.getStringList("allowed-commands");
                    boolean whitelist = ruleSec.getBoolean("whitelist-mode", false);
                    String msg = ruleSec.getString("blocked-message");
                    rules.add(new ModalityRule(key, patterns, blocked, allowed, whitelist, msg));
                }
            }
        }
    }

    public void addRule(ModalityRule rule) {
        if (rule != null) {
            rules.add(rule);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!enabled) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("drakescore.commandgate.bypass")) {
            return;
        }

        String worldName = player.getWorld().getName();
        String message = event.getMessage();

        for (ModalityRule rule : rules) {
            if (rule.matchesWorld(worldName)) {
                if (rule.isCommandBlocked(message)) {
                    event.setCancelled(true);
                    String primaryCmd = message.split(" ")[0];
                    String response = rule.getBlockedMessage()
                            .replace("{command}", primaryCmd)
                            .replace("{modality}", rule.getName());
                    player.sendMessage(MiniMessage.miniMessage().deserialize(response));
                    return;
                }
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<ModalityRule> getRules() {
        return Collections.unmodifiableList(rules);
    }
}
