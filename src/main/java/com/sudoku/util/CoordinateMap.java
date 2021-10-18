package com.sudoku.util;

import java.util.HashMap;

public final class CoordinateMap<V> extends HashMap<Integer, V> {
    private static final int ROW_MULTIPLIER = 7;
    private static final int COL_MULTIPLIER = 11;

    public void putWithCoordinates(byte row, byte col, V value) {
        this.put(hashCoordinates(row, col), value);
    }

    public V getWithCoordinates(byte row, byte col) {
        return this.get(hashCoordinates(row, col));
    }

    private int hashCoordinates(byte row, byte col) {
        return row * ROW_MULTIPLIER + col * COL_MULTIPLIER;
    }
}
