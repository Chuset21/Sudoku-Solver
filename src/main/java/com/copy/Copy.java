package com.copy;

import java.util.Arrays;

public class Copy {
    public static byte[][] deepCopy(byte[][] original) {
        final byte[][] result = new byte[original.length][original.length];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return result;
    }
}
