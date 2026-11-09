package com.drakescraft.suites.core.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class EconomyRouterTest {

    static Economy fake(String tag, double[] bal) {
        return (Economy) Proxy.newProxyInstance(Economy.class.getClassLoader(), new Class<?>[] { Economy.class },
                (p, m, a) -> {
                    switch (m.getName()) {
                        case "getBalance": return bal[0];
                        case "withdrawPlayer":
                            double amt = (double) a[a.length - 1];
                            bal[0] -= amt;
                            return new EconomyResponse(amt, bal[0], EconomyResponse.ResponseType.SUCCESS, tag);
                        case "getName": return tag;
                        case "toString": return tag;
                        case "hashCode": return tag.hashCode();
                        case "equals": return p == a[0];
                        default: return m.getReturnType() == boolean.class ? true : null;
                    }
                });
    }

    @Test
    void siguePerezosamenteAInvSwitcher() {
        double[] ess = { 1000 }, inv = { 50 };
        Economy e = fake("Essentials", ess), i = fake("InvSwitcher", inv);
        List<List<EconomyRouter.Candidate>> estado = new java.util.ArrayList<>();
        estado.add(List.of(new EconomyRouter.Candidate("Essentials", e)));
        Economy r = EconomyRouter.create(() -> EconomyRouter.pick(estado.get(0)), Logger.getAnonymousLogger());
        assertEquals(1000.0, r.getBalance("x")); // InvSwitcher aun no registrado
        estado.set(0, List.of(new EconomyRouter.Candidate("Essentials", e), new EconomyRouter.Candidate("BentoBox", i),
                new EconomyRouter.Candidate("DrakesCore", r)));
        assertEquals(50.0, r.getBalance("x")); // registrado tarde: el router lo usa
        assertTrue(r.withdrawPlayer("x", 20.0).transactionSuccess());
        assertEquals(30.0, inv[0]);
        assertEquals(1000.0, ess[0]);
    }

    @Test
    void bancosNoSoportadosYSinProveedor() {
        Economy r = EconomyRouter.create(() -> null, Logger.getAnonymousLogger());
        assertFalse(r.hasBankSupport());
        assertEquals(EconomyResponse.ResponseType.NOT_IMPLEMENTED, r.bankBalance("b").type);
        assertTrue(r.getBanks().isEmpty());
        assertEquals(0.0, r.getBalance("x"));
        assertFalse(r.withdrawPlayer("x", 1).transactionSuccess());
    }

    @Test
    void ignoraElPropioRouter() {
        Economy r = EconomyRouter.create(() -> null, Logger.getAnonymousLogger());
        assertNull(EconomyRouter.pick(List.of(new EconomyRouter.Candidate("DrakesCore", r))));
    }
}
