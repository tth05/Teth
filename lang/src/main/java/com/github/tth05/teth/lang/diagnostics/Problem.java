package com.github.tth05.teth.lang.diagnostics;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.CharArrayUtils;

public record Problem(ISpan span, String message) {

    public String prettyPrint(boolean useAnsiColors) {
        StringBuilder sb = new StringBuilder();
        var source = this.span.getSource();
        var offset = this.span.getOffset();
        var line = this.span.getStartLine();
        var startColumn = this.span.getStartColumn();

        // Append previous line
        if (line != 0) {
            appendLine(sb, source, CharArrayUtils.getLineStart(source, offset) - 1, line - 1 + 1, useAnsiColors);
            sb.append("\n");
        }

        // Append current line
        if (useAnsiColors)
            appendLineWithHighlight(sb, source, offset, line + 1, startColumn, this.span.getEndColumn(), 31);
        else
            appendLine(sb, source, offset, line + 1, useAnsiColors);

        var lineStartLength = ((line + 1) + "").length();

        // Append error message
        sb.append("\n");
        appendLinePrefix(sb, " ".repeat(lineStartLength), useAnsiColors);
        sb.append(" ".repeat(startColumn));
        sb.append(addAnsiHighlight("^ " + this.message, 0, this.message.length() + 2, 31));

        // Append next line
        var currentLineEnd = CharArrayUtils.getLineEnd(source, offset);
        if (currentLineEnd < source.length - 1 /* NULL */ - 1) {
            sb.append("\n");
            appendLine(sb, source, currentLineEnd + 1, line + 1 + 1, useAnsiColors);
        }
        return sb.toString();
    }

    private static void appendLinePrefix(StringBuilder sb, String lineNumber, boolean useAnsiColors) {
        if (useAnsiColors)
            sb.append("\u001b[0;36m");
        sb.append(lineNumber).append(" | ");
        if (useAnsiColors)
            sb.append("\u001b[0m");
    }

    private static void appendLine(StringBuilder sb, char[] source, int offset, int lineNumber, boolean useAnsiColors) {
        appendLinePrefix(sb, lineNumber + "", useAnsiColors);
        sb.append(CharArrayUtils.getLineContents(source, offset));
    }

    private static void appendLineWithHighlight(StringBuilder sb, char[] source, int offset, int lineNumber,
                                                int highlightStart, int highlightEnd, int ansiColor) {
        appendLinePrefix(sb, lineNumber + "", true);
        sb.append(addAnsiHighlight(CharArrayUtils.getLineContents(source, offset), highlightStart, highlightEnd, ansiColor));
    }

    private static StringBuilder addAnsiHighlight(String str, int highlightStart, int highlightEnd, int ansiColor) {
        var lineContents = new StringBuilder(str);

        lineContents.insert(Math.min(lineContents.length(), highlightEnd), "\u001b[0m");
        lineContents.insert(highlightStart, "\u001b[0;" + ansiColor + "m");
        return lineContents;
    }
}
