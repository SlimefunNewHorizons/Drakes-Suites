package com.drakescraft.suites.bio.geneticwoolies;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

/**
 * Representa un par de alelos mendelianos para un rasgo genético de oveja
 * (rendimiento 'y' o pureza 'p').
 * Mayúscula = alelo dominante, minúscula = alelo recesivo.
 */
public final class WoolGene {

    private final char[] alleles;

    public WoolGene(char first, char second) {
        if (Character.toLowerCase(first) != Character.toLowerCase(second)) {
            throw new IllegalArgumentException("Los alelos de un mismo locus deben compartir letra base: " + first + ", " + second);
        }
        this.alleles = new char[] {first, second};
        Arrays.sort(this.alleles);
    }

    /**
     * Construye un gen a partir de su letra base y su estado mendeliano:
     * 0 = doble recesivo (xx), 1 = heterocigoto (Xx), 3 = doble dominante (XX).
     */
    public WoolGene(char locus, int state) {
        char lower = Character.toLowerCase(locus);
        char upper = Character.toUpperCase(locus);
        if (state >= 3) {
            this.alleles = new char[] {upper, upper};
        } else if (state >= 1) {
            this.alleles = new char[] {upper, lower};
        } else {
            this.alleles = new char[] {lower, lower};
        }
    }

    /**
     * Retorna el estado mendeliano del gen:
     * 0 = doble recesivo (xx)
     * 1 = heterocigoto (Xx)
     * 3 = doble dominante (XX)
     */
    public int getState() {
        boolean firstDom = Character.isUpperCase(alleles[0]);
        boolean secondDom = Character.isUpperCase(alleles[1]);
        if (firstDom && secondDom) {
            return 3;
        }
        if (firstDom || secondDom) {
            return 1;
        }
        return 0;
    }

    /**
     * Nivel escalar de 1 a 3 derivado del estado mendeliano:
     * 0 -> 1, 1 -> 2, 3 -> 3.
     */
    public int getLevel() {
        return switch (getState()) {
            case 3 -> 3;
            case 1 -> 2;
            default -> 1;
        };
    }

    public boolean isDominant() {
        return getState() > 0;
    }

    public boolean isHomozygous() {
        return alleles[0] == alleles[1];
    }

    public char getLocus() {
        return Character.toLowerCase(alleles[0]);
    }

    @Nonnull
    public char[] getAlleles() {
        return new char[] {alleles[0], alleles[1]};
    }

    /**
     * Segregación meiótica: dona uno de los dos alelos al azar.
     */
    public char split(@Nonnull Random random) {
        return random.nextBoolean() ? alleles[0] : alleles[1];
    }

    public char split() {
        return split(ThreadLocalRandom.current());
    }

    /**
     * Cruza este gen con el de un compañero, con opción de mutación de un alelo.
     */
    @Nonnull
    public WoolGene breed(@Nonnull WoolGene partner, double mutationChance, @Nonnull Random random) {
        char a = this.split(random);
        char b = partner.split(random);
        if (mutationChance > 0.0 && random.nextDouble() < mutationChance) {
            if (random.nextBoolean()) {
                a = flipCase(a);
            } else {
                b = flipCase(b);
            }
        }
        return new WoolGene(a, b);
    }

    private static char flipCase(char c) {
        return Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);
    }

    @Nonnull
    public static WoolGene parse(@Nonnull String raw, char expectedLocus) {
        if (raw.length() != 2) {
            throw new IllegalArgumentException("Notación de gen inválida: " + raw);
        }
        char a = raw.charAt(0);
        char b = raw.charAt(1);
        char lower = Character.toLowerCase(expectedLocus);
        if (Character.toLowerCase(a) != lower || Character.toLowerCase(b) != lower) {
            throw new IllegalArgumentException("Locus inesperado en gen '" + raw + "', se esperaba '" + lower + "'");
        }
        return new WoolGene(a, b);
    }

    @Override
    public String toString() {
        return new String(alleles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WoolGene that)) return false;
        return Arrays.equals(this.alleles, that.alleles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alleles[0], alleles[1]);
    }
}
