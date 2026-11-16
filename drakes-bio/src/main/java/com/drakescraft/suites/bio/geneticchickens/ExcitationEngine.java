// SPDX-License-Identifier: GPL-3.0-or-later
package com.drakescraft.suites.bio.geneticchickens;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Motor de cálculo de rendimiento y aceleración para Cámaras de Excitación
 * (Tiers 1 a 4: Standard, Boosted, Ultimate, Quantum).
 * 
 * Resuelve el cuello de botella físico del ticker de Slimefun:
 * Cuando la velocidad de la máquina (ej. Quantum 25x) alcanza el límite inferior de 1 tick,
 * la energía cuántica residual se traduce en rendimiento multiplicado (2.5x sobre la Ultimate:
 * 2 ítems base + 50% de probabilidad de un 3er ítem por ciclo).
 */
public final class ExcitationEngine {

    private ExcitationEngine() {}

    /**
     * Calcula la duración en ticks del ciclo de excitación.
     *
     * @param baseTime     Tiempo base en ticks (por defecto 6).
     * @param tier         Tier del recurso de la especie (0 a 9).
     * @param dnaStrength  Fuerza del ADN del espécimen (0 a 10).
     * @param machineSpeed Velocidad de procesamiento de la máquina (1, 2, 10, 25).
     * @return Duración calculada en ticks (mínimo 1).
     */
    public static int calculateCycleTicks(int baseTime, int tier, int dnaStrength, int machineSpeed) {
        int rawTime = Math.max(1, baseTime + tier - 2 * dnaStrength);
        int speed = Math.max(1, machineSpeed);
        return Math.max(1, rawTime / speed);
    }

    /**
     * Calcula la cantidad de recursos generados por ciclo.
     * Para máquinas de velocidad cuántica (>= 25x), cuando se alcanza el suelo de 1 tick,
     * se multiplica el rendimiento en 2.5x (2 garantizados + 50% de probabilidad de 3).
     *
     * @param baseTime     Tiempo base en ticks.
     * @param tier         Tier del recurso.
     * @param dnaStrength  Fuerza del ADN.
     * @param machineSpeed Velocidad de la máquina (ej. 25 para Quantum).
     * @return Cantidad de recursos producidos en el ciclo.
     */
    public static int calculateYieldAmount(int baseTime, int tier, int dnaStrength, int machineSpeed) {
        int cycleTicks = calculateCycleTicks(baseTime, tier, dnaStrength, machineSpeed);
        if (machineSpeed >= 25 && cycleTicks <= 1) {
            // Quantum Excitation: 2.5x rendimiento respecto a Ultimate (10x)
            return 2 + (ThreadLocalRandom.current().nextDouble() < 0.5 ? 1 : 0);
        }
        return 1;
    }
}
