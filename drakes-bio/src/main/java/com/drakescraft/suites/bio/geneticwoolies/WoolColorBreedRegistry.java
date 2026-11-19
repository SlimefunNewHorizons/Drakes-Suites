package com.drakescraft.suites.bio.geneticwoolies;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Registro de reglas de cruce, dominancia y tiers entre los 16 colores de lana (DyeColor).
 * Estructura progresiva: Base (Tier 0) -> Secundarios (Tier 1) -> Raros/Prismáticos (Tier 2).
 */
public final class WoolColorBreedRegistry {

    public enum ColorTier {
        BASE(0),
        SECONDARY(1),
        RARE(2);

        private final int rank;

        ColorTier(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }
    }

    private record ColorPair(DyeColor first, DyeColor second) {
        static ColorPair of(DyeColor a, DyeColor b) {
            return a.ordinal() <= b.ordinal() ? new ColorPair(a, b) : new ColorPair(b, a);
        }
    }

    private static final Map<DyeColor, ColorTier> TIERS = new EnumMap<>(DyeColor.class);
    private static final Map<ColorPair, DyeColor> CROSS_RULES = new HashMap<>();

    static {
        // Tier 0: Colores Base (6)
        TIERS.put(DyeColor.WHITE, ColorTier.BASE);
        TIERS.put(DyeColor.BLACK, ColorTier.BASE);
        TIERS.put(DyeColor.BROWN, ColorTier.BASE);
        TIERS.put(DyeColor.RED, ColorTier.BASE);
        TIERS.put(DyeColor.YELLOW, ColorTier.BASE);
        TIERS.put(DyeColor.BLUE, ColorTier.BASE);

        // Tier 1: Colores Secundarios (6)
        TIERS.put(DyeColor.GRAY, ColorTier.SECONDARY);
        TIERS.put(DyeColor.LIGHT_GRAY, ColorTier.SECONDARY);
        TIERS.put(DyeColor.ORANGE, ColorTier.SECONDARY);
        TIERS.put(DyeColor.GREEN, ColorTier.SECONDARY);
        TIERS.put(DyeColor.PURPLE, ColorTier.SECONDARY);
        TIERS.put(DyeColor.PINK, ColorTier.SECONDARY);

        // Tier 2: Colores Raros / Prismáticos (4)
        TIERS.put(DyeColor.LIME, ColorTier.RARE);
        TIERS.put(DyeColor.LIGHT_BLUE, ColorTier.RARE);
        TIERS.put(DyeColor.CYAN, ColorTier.RARE);
        TIERS.put(DyeColor.MAGENTA, ColorTier.RARE);

        // Reglas Base -> Secundarios
        registerCross(DyeColor.WHITE, DyeColor.BLACK, DyeColor.GRAY);
        registerCross(DyeColor.WHITE, DyeColor.GRAY, DyeColor.LIGHT_GRAY);
        registerCross(DyeColor.RED, DyeColor.YELLOW, DyeColor.ORANGE);
        registerCross(DyeColor.YELLOW, DyeColor.BLUE, DyeColor.GREEN);
        registerCross(DyeColor.RED, DyeColor.BLUE, DyeColor.PURPLE);
        registerCross(DyeColor.RED, DyeColor.WHITE, DyeColor.PINK);
        registerCross(DyeColor.BROWN, DyeColor.YELLOW, DyeColor.ORANGE);
        registerCross(DyeColor.BROWN, DyeColor.BLACK, DyeColor.GRAY);

        // Reglas Secundarios / Base -> Raros / Prismáticos
        registerCross(DyeColor.GREEN, DyeColor.WHITE, DyeColor.LIME);
        registerCross(DyeColor.GREEN, DyeColor.YELLOW, DyeColor.LIME);
        registerCross(DyeColor.BLUE, DyeColor.WHITE, DyeColor.LIGHT_BLUE);
        registerCross(DyeColor.BLUE, DyeColor.LIGHT_GRAY, DyeColor.LIGHT_BLUE);
        registerCross(DyeColor.BLUE, DyeColor.GREEN, DyeColor.CYAN);
        registerCross(DyeColor.LIGHT_BLUE, DyeColor.GREEN, DyeColor.CYAN);
        registerCross(DyeColor.PURPLE, DyeColor.PINK, DyeColor.MAGENTA);
        registerCross(DyeColor.ORANGE, DyeColor.PINK, DyeColor.MAGENTA);
        registerCross(DyeColor.CYAN, DyeColor.PURPLE, DyeColor.MAGENTA);
    }

    private WoolColorBreedRegistry() {
    }

    private static void registerCross(DyeColor a, DyeColor b, DyeColor result) {
        CROSS_RULES.put(ColorPair.of(a, b), result);
    }

    @Nonnull
    public static ColorTier getTier(@Nonnull DyeColor color) {
        return TIERS.getOrDefault(color, ColorTier.BASE);
    }

    /**
     * Determina el color fenotípico expresado entre dos alelos de color.
     * Regla de dominancia mendeliana:
     * - Menor tier (BASE > SECONDARY > RARE) domina sobre colores más raros.
     * - A igual tier, desempata de forma determinista por ordinal.
     */
    @Nonnull
    public static DyeColor resolveDominantColor(@Nonnull DyeColor a, @Nonnull DyeColor b) {
        if (a == b) {
            return a;
        }
        int rankA = getTier(a).getRank();
        int rankB = getTier(b).getRank();
        if (rankA != rankB) {
            return rankA < rankB ? a : b;
        }
        return a.ordinal() <= b.ordinal() ? a : b;
    }

    /**
     * Retorna el alelo recesivo entre dos alelos de color.
     */
    @Nonnull
    public static DyeColor resolveRecessiveColor(@Nonnull DyeColor a, @Nonnull DyeColor b) {
        DyeColor dominant = resolveDominantColor(a, b);
        return dominant == a ? b : a;
    }

    /**
     * Busca si existe una regla de mutación/cruce para un par de colores.
     */
    @Nonnull
    public static Optional<DyeColor> findCrossResult(@Nonnull DyeColor a, @Nonnull DyeColor b) {
        if (a == b) {
            return Optional.empty();
        }
        return Optional.ofNullable(CROSS_RULES.get(ColorPair.of(a, b)));
    }

    /**
     * Evalúa el cruce entre dos alelos aplicando la probabilidad efectiva de mutación.
     * Si la mutación ocurre y existe regla, retorna el color mutado; de lo contrario retorna null.
     */
    @Nullable
    public static DyeColor rollMutation(@Nonnull DyeColor a, @Nonnull DyeColor b, double mutationChance, @Nonnull Random random) {
        if (mutationChance <= 0.0) {
            return null;
        }
        Optional<DyeColor> candidate = findCrossResult(a, b);
        if (candidate.isEmpty()) {
            return null;
        }
        double clamped = Math.min(1.0, mutationChance);
        return random.nextDouble() < clamped ? candidate.get() : null;
    }

    public static int getRegisteredColorCount() {
        return TIERS.size();
    }

    public static int getRuleCount() {
        return CROSS_RULES.size();
    }

    @Nonnull
    public static Map<DyeColor, ColorTier> getAllTiers() {
        return Collections.unmodifiableMap(TIERS);
    }

    /**
     * Traduce un DyeColor al bloque de lana correspondiente.
     */
    @Nonnull
    public static Material getWoolMaterial(@Nonnull DyeColor color) {
        return switch (color) {
            case WHITE -> Material.WHITE_WOOL;
            case ORANGE -> Material.ORANGE_WOOL;
            case MAGENTA -> Material.MAGENTA_WOOL;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case LIME -> Material.LIME_WOOL;
            case PINK -> Material.PINK_WOOL;
            case GRAY -> Material.GRAY_WOOL;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_WOOL;
            case CYAN -> Material.CYAN_WOOL;
            case PURPLE -> Material.PURPLE_WOOL;
            case BLUE -> Material.BLUE_WOOL;
            case BROWN -> Material.BROWN_WOOL;
            case GREEN -> Material.GREEN_WOOL;
            case RED -> Material.RED_WOOL;
            case BLACK -> Material.BLACK_WOOL;
        };
    }
}
