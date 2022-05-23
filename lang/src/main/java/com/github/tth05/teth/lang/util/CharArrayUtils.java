package com.github.tth05.teth.lang.util;

public class CharArrayUtils {

    public static int findLineStart(char[] source, int index) {
        int lineStart = index;
        while (lineStart > 0 && source[lineStart - 1] != '\n') {
            lineStart--;
        }
        return lineStart;
    }

    public static int findLineEnd(char[] source, int index) {
        int lineEnd = index;
        while (lineEnd < source.length && source[lineEnd] != '\n') {
            lineEnd++;
        }
        return lineEnd;
    }
}
