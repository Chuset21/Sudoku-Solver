package com.sudoku.util;

import java.util.HashMap;

public final class CoordinateMap<V> extends HashMap<Integer, V> {
    private static final int ROW_MULTIPLIER = 7;
    private static final int COL_MULTIPLIER = 11;

    public void putWithCoordinates(int row, int col, V value) {
        this.put(hashCoordinates(row, col), value);
    }

    public V getWithCoordinates(int row, int col) {
        return this.get(hashCoordinates(row, col));
    }

    private int hashCoordinates(int row, int col) {
        return row * ROW_MULTIPLIER + col * COL_MULTIPLIER;
    }
}
