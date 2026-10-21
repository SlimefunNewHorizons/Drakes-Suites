package com.drakescraft.suites.onechunk;

import java.util.List;
import java.util.Objects;

final class ColumnProfile {
    private final List<String> progression;

    ColumnProfile(List<String> progression) {
        if (progression == null || progression.isEmpty()) {
            throw new IllegalArgumentException("progression must not be empty");
        }
        this.progression = List.copyOf(progression.stream().filter(Objects::nonNull).toList());
        if (this.progression.isEmpty()) {
            throw new IllegalArgumentException("progression must contain a material");
        }
    }

    String materialAt(long progress) {
        return progression.get(Math.floorMod(progress, progression.size()));
    }

    int columnBlockCount(int minY, int maxY) {
        if (minY > maxY) {
            throw new IllegalArgumentException("minY must not exceed maxY");
        }
        return Math.multiplyExact(256, Math.addExact(Math.subtractExact(maxY, minY), 1));
    }
}
