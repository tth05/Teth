package com.github.tth05.teth.lang.util;

public class NumberUtils {

    public static long parseLong(String text, long fallback) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
