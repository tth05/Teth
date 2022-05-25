package com.github.tth05.teth.lang.util;

import java.util.Arrays;

public class CharArrayUtils {

    public static int getLineNumber(char[] source, int offset) {
        int lineNumber = 0;
        for (int i = 0; i < offset; i++) {
            if (source[i] == '\n')
                lineNumber++;
        }
        return lineNumber;
    }

    public static int getLineStart(char[] source, int index) {
        int lineStart = index;
        while (lineStart > 0 && source[lineStart - 1] != '\n') {
            lineStart--;
        }
        return lineStart;
    }

    public static int getLineEnd(char[] source, int index) {
        int lineEnd = index;
        while (lineEnd < source.length && source[lineEnd] != '\n') {
            lineEnd++;
        }
        return lineEnd;
    }

    public static String getLineContents(char[] source, int offset) {
        return new String(Arrays.copyOfRange(
                source,
                CharArrayUtils.getLineStart(source, offset),
                CharArrayUtils.getLineEnd(source, offset))
        );
    }
}
