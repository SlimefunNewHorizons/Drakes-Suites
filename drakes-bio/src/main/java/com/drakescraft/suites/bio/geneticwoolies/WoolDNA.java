package com.drakescraft.suites.bio.geneticwoolies;

import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Sheep;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Genoma mendeliano ligero para ovejas genéticas (Genetic Woolies).
 * Contiene:
 * - Par de alelos de color (dominante/recesivo sobre los 16 DyeColor).
 * - Gen de rendimiento de lana (locus 'y', nivel 1..3).
 * - Gen de pureza/mutación (locus 'p', modula estabilidad vs. tasa de mutación).
 * - Generación del linaje.
 *
 * Persistencia 100% segura mediante PersistentDataContainer (PDC), sin NBT frágil.
 */
public final class WoolDNA {

    private static final String FORMAT_VERSION = "v1";
    public static final char YIELD_LOCUS = 'y';
    public static final char PURITY_LOCUS = 'p';

    private final DyeColor dominantColor;
    private final DyeColor recessiveColor;
    private final WoolGene yieldGene;
    private final WoolGene purityGene;
    private final int generation;

    public WoolDNA(
            @Nonnull DyeColor colorAlleleA,
            @Nonnull DyeColor colorAlleleB,
            @Nonnull WoolGene yieldGene,
            @Nonnull WoolGene purityGene,
            int generation) {
        Objects.requireNonNull(colorAlleleA, "colorAlleleA no puede ser nulo");
        Objects.requireNonNull(colorAlleleB, "colorAlleleB no puede ser nulo");
        Objects.requireNonNull(yieldGene, "yieldGene no puede ser nulo");
        Objects.requireNonNull(purityGene, "purityGene no puede ser nulo");

        this.dominantColor = WoolColorBreedRegistry.resolveDominantColor(colorAlleleA, colorAlleleB);
        this.recessiveColor = WoolColorBreedRegistry.resolveRecessiveColor(colorAlleleA, colorAlleleB);
        this.yieldGene = yieldGene;
        this.purityGene = purityGene;
        this.generation = Math.max(0, generation);
    }

    /**
     * Crea ADN salvaje/base a partir de una oveja vanilla.
     */
    @Nonnull
    public static WoolDNA fromVanillaSheep(@Nonnull Sheep sheep) {
        DyeColor color = sheep.getColor() != null ? sheep.getColor() : DyeColor.WHITE;
        return new WoolDNA(
                color,
                color,
                new WoolGene(YIELD_LOCUS, 0),
                new WoolGene(PURITY_LOCUS, 1),
                0
        );
    }

    /**
     * Crea un ADN homocigoto de color con parámetros explícitos.
     */
    @Nonnull
    public static WoolDNA pure(@Nonnull DyeColor color, int yieldState, int purityState, int generation) {
        return new WoolDNA(
                color,
                color,
                new WoolGene(YIELD_LOCUS, yieldState),
                new WoolGene(PURITY_LOCUS, purityState),
                generation
        );
    }

    @Nonnull
    public DyeColor getExpressedColor() {
        return dominantColor;
    }

    @Nonnull
    public DyeColor getDominantColor() {
        return dominantColor;
    }

    @Nonnull
    public DyeColor getRecessiveColor() {
        return recessiveColor;
    }

    public boolean isColorHomozygous() {
        return dominantColor == recessiveColor;
    }

    @Nonnull
    public WoolGene getYieldGene() {
        return yieldGene;
    }

    /**
     * Rendimiento de lana de 1 a 3.
     */
    public int getYield() {
        return yieldGene.getLevel();
    }

    @Nonnull
    public WoolGene getPurityGene() {
        return purityGene;
    }

    /**
     * Nivel de pureza de 1 a 3 (1 = inestable/alta mutación, 2 = normal, 3 = puro/estable).
     */
    public int getPurity() {
        return purityGene.getLevel();
    }

    /**
     * Multiplicador de probabilidad de mutación derivado del gen de pureza.
     */
    public double getMutationMultiplier() {
        return switch (purityGene.getState()) {
            case 3 -> 0.5;
            case 1 -> 1.0;
            default -> 1.5;
        };
    }

    public int getGeneration() {
        return generation;
    }

    @Nonnull
    public DyeColor splitColor(@Nonnull Random random) {
        return random.nextBoolean() ? dominantColor : recessiveColor;
    }

    /**
     * Cruza este genoma con el de un compañero aplicando segregación mendeliana
     * y probabilidad configurable de mutación.
     */
    @Nonnull
    public WoolDNA breed(@Nonnull WoolDNA partner, double baseMutationChance, @Nonnull Random random) {
        Objects.requireNonNull(partner, "partner no puede ser nulo");
        Objects.requireNonNull(random, "random no puede ser nulo");

        double avgMultiplier = (this.getMutationMultiplier() + partner.getMutationMultiplier()) * 0.5;
        double effectiveMutation = Math.max(0.0, Math.min(1.0, baseMutationChance * avgMultiplier));

        DyeColor gameteA = this.splitColor(random);
        DyeColor gameteB = partner.splitColor(random);

        DyeColor mutatedColor = WoolColorBreedRegistry.rollMutation(gameteA, gameteB, effectiveMutation, random);
        DyeColor childColorA;
        DyeColor childColorB;
        if (mutatedColor != null) {
            // Mutación fijada homocigota para que el nuevo color se exprese y sea heredable
            childColorA = mutatedColor;
            childColorB = mutatedColor;
        } else {
            childColorA = gameteA;
            childColorB = gameteB;
        }

        double traitMutation = effectiveMutation * 0.5;
        WoolGene childYield = this.yieldGene.breed(partner.yieldGene, traitMutation, random);
        WoolGene childPurity = this.purityGene.breed(partner.purityGene, traitMutation, random);
        int childGen = Math.max(this.generation, partner.generation) + 1;

        return new WoolDNA(childColorA, childColorB, childYield, childPurity, childGen);
    }

    @Nonnull
    public WoolDNA breed(@Nonnull WoolDNA partner, double baseMutationChance) {
        return breed(partner, baseMutationChance, ThreadLocalRandom.current());
    }

    /**
     * Serializa el genoma a una cadena compacta y versionada para PDC.
     * Formato: v1:DOMINANT/RECESSIVE:Yy:Pp:g3
     */
    @Nonnull
    public String serialize() {
        return FORMAT_VERSION + ":"
                + dominantColor.name() + "/" + recessiveColor.name() + ":"
                + yieldGene + ":"
                + purityGene + ":"
                + "g" + generation;
    }

    /**
     * Deserializa de forma segura una cadena PDC. Retorna null si el formato es inválido.
     */
    @Nullable
    public static WoolDNA deserialize(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split(":");
        if (parts.length != 5 || !FORMAT_VERSION.equals(parts[0])) {
            return null;
        }
        String[] colors = parts[1].split("/");
        if (colors.length != 2) {
            return null;
        }
        if (!parts[4].startsWith("g") || parts[4].length() < 2) {
            return null;
        }
        try {
            DyeColor c1 = DyeColor.valueOf(colors[0]);
            DyeColor c2 = DyeColor.valueOf(colors[1]);
            WoolGene yield = WoolGene.parse(parts[2], YIELD_LOCUS);
            WoolGene purity = WoolGene.parse(parts[3], PURITY_LOCUS);
            int gen = Integer.parseInt(parts[4].substring(1));
            if (gen < 0) {
                return null;
            }
            return new WoolDNA(c1, c2, yield, purity, gen);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public void writeTo(@Nonnull PersistentDataHolder holder, @Nonnull NamespacedKey key) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, serialize());
    }

    @Nullable
    public static WoolDNA readFrom(@Nonnull PersistentDataHolder holder, @Nonnull NamespacedKey key) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        if (!pdc.has(key, PersistentDataType.STRING)) {
            return null;
        }
        return deserialize(pdc.get(key, PersistentDataType.STRING));
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WoolDNA that)) return false;
        return generation == that.generation
                && dominantColor == that.dominantColor
                && recessiveColor == that.recessiveColor
                && yieldGene.equals(that.yieldGene)
                && purityGene.equals(that.purityGene);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dominantColor, recessiveColor, yieldGene, purityGene, generation);
    }
}
