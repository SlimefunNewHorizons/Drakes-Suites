package com.drakescraft.suites.core.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class VaultEconomyCommandTest {

    @Test
    void parsesAmountsSafely() {
        assertEquals(150.5D, VaultEconomyCommand.parsePositiveAmount("150.5"));
        assertNull(VaultEconomyCommand.parsePositiveAmount("0"));
        assertNull(VaultEconomyCommand.parsePositiveAmount("-5"));
        assertNull(VaultEconomyCommand.parsePositiveAmount("NaN"));
        assertNull(VaultEconomyCommand.parsePositiveAmount("Infinity"));
        assertEquals(0.0D, VaultEconomyCommand.parseNonNegativeAmount("0"));
    }

    @Test
    void applySetAdjustsBalanceViaDelta() {
        AtomicReference<Double> bal = new AtomicReference<>(100.0D);
        Economy fake = (Economy) Proxy.newProxyInstance(
                Economy.class.getClassLoader(),
                new Class<?>[]{Economy.class},
                (proxy, m, args) -> switch (m.getName()) {
                    case "getBalance" -> bal.get();
                    case "depositPlayer" -> {
                        double amt = (double) args[args.length - 1];
                        bal.set(bal.get() + amt);
                        yield new EconomyResponse(amt, bal.get(), ResponseType.SUCCESS, null);
                    }
                    case "withdrawPlayer" -> {
                        double amt = (double) args[args.length - 1];
                        bal.set(bal.get() - amt);
                        yield new EconomyResponse(amt, bal.get(), ResponseType.SUCCESS, null);
                    }
                    default -> null;
                }
        );

        EconomyResponse r1 = VaultEconomyCommand.applySet(fake, null, 250.0D);
        assertTrue(r1.transactionSuccess());
        assertEquals(250.0D, bal.get());

        EconomyResponse r2 = VaultEconomyCommand.applySet(fake, null, 40.0D);
        assertTrue(r2.transactionSuccess());
        assertEquals(40.0D, bal.get());
    }
}
