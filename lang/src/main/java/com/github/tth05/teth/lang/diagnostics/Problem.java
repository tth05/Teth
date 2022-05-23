package com.github.tth05.teth.lang.diagnostics;

import com.github.tth05.teth.lang.span.ISpan;

import java.util.Arrays;

public class Problem {

    private final ISpan span;
    private final String message;

    public Problem(ISpan span, String message) {
        this.span = span;
        this.message = message;
    }

    public ISpan getSpan() {
        return this.span;
    }

    public String getMessage() {
        return this.message;
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        var source = this.getSpan().getSource();
        var offset = this.getSpan().getOffset();
        sb.append(this.span.getLine()).append(":").append(this.span.getColumn());
        var lineStartLength = sb.length();
        sb.append(" | ")
                .append(new String(Arrays.copyOfRange(
                        source,
                        findLineStart(source, offset),
                        findLineEnd(source, offset)))
                );

        sb.append("\n");
        sb.append(" ".repeat(lineStartLength)).append(" | ");
        sb.append(" ".repeat(this.span.getColumn() - 1)).append("^");
        sb.append("\n");
        sb.append(" ".repeat(lineStartLength)).append(" | ");
        // Center the message below the roof symbol
        sb.append(" ".repeat(
                Math.max(0, this.span.getColumn() - 1 - this.message.length() / 2)
        ));
        sb.append(this.message);
        return sb.toString();
    }

    private static int findLineStart(char[] source, int index) {
        int lineStart = index;
        while (lineStart > 0 && source[lineStart - 1] != '\n') {
            lineStart--;
        }
        return lineStart;
    }

    private static int findLineEnd(char[] source, int index) {
        int lineEnd = index;
        while (lineEnd < source.length && source[lineEnd] != '\n') {
            lineEnd++;
        }
        return lineEnd;
    }
}
