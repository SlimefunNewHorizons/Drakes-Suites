package com.drakescraft.suites.core;

import com.drakescraft.suites.core.command.CommandModalityGate.ModalityRule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandModalityRuleTest {
    private final ModalityRule blacklist = new ModalityRule("test", List.of(".*"),
            List.of("/sf cheat", "/warp minas", "/pay"), List.of(), false, null);
    private final ModalityRule whitelist = new ModalityRule("test", List.of(".*"),
            List.of("/sf give"), List.of("/sf", "/warp spawn"), true, null);

    @ParameterizedTest
    @ValueSource(strings = {"/sf cheat", "/sf cheat STONE 64", "/SLIMEFUN:SF CHEAT STONE",
            "  /sf\tcheat   STONE  ", "/warp minas", "/essentials:warp minas player",
            "/pay player 10", "/essentials:pay player 10"})
    void blocksConfiguredCommandsAndArgumentPrefixes(String command) {
        assertTrue(blacklist.isCommandBlocked(command), command);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/sf", "/sf guide", "/sf cheating", "/warp minas2",
            "/warp other", "/payday", "/warp namespace:minas"})
    void keepsUnrelatedCommandsAndArgumentBoundaries(String command) {
        assertFalse(blacklist.isCommandBlocked(command), command);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/sf guide", "/warp spawn", "/ESSENTIALS:WARP\tSPAWN player"})
    void whitelistAcceptsOnlyConfiguredCommandPrefixes(String command) {
        assertFalse(whitelist.isCommandBlocked(command), command);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/sf give DIAMOND", "/slimefun:sf give DIAMOND",
            "/warp", "/warp minas", "/warp spawn2", "/warp namespace:spawn", "/unknown"})
    void blacklistOverridesWhitelistWithoutAllowingArgumentBypass(String command) {
        assertTrue(whitelist.isCommandBlocked(command), command);
    }
}
