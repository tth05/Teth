package com.github.tth05.teth.lang.diagnostics;

import com.github.tth05.teth.lang.util.CharArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProblemPrettyPrinter {

    private final List<Context> contexts = new ArrayList<>(2);
    private final char[] source;

    private boolean useAnsiColors;

    public ProblemPrettyPrinter(char[] source) {
        this.source = source;
    }

    public static ProblemPrettyPrinter ofSource(char[] source) {
        return new ProblemPrettyPrinter(source);
    }

    public ProblemPrettyPrinter useAnsiColors(boolean useAnsiColors) {
        this.useAnsiColors = useAnsiColors;
        return this;
    }

    public Context withContext(int offset, int offsetEnd, boolean includeSurroundingLines) {
        var context = new Context(this.source, offset, offsetEnd, includeSurroundingLines);
        this.contexts.add(context);
        return context;
    }


    @Override
    public String toString() {
        return this.contexts.stream().map(Context::toString).collect(Collectors.joining("\n"));
    }

    public class Context {

        private final List<Highlight> highlights = new ArrayList<>(1);
        private final List<Message> messages = new ArrayList<>(1);
        private final List<Line> lines = new ArrayList<>(5);

        private Context(char[] source, int offset, int offsetEnd, boolean includeSurroundingLines) {
            var start = CharArrayUtils.getLineStart(source, offset);
            if (start != 0 && includeSurroundingLines)
                this.lines.add(new Line(CharArrayUtils.getLineStart(source, start - 1), start));

            var i = start;
            while (i < offsetEnd) {
                var end = CharArrayUtils.getLineEnd(source, i);
                this.lines.add(new Line(i, end));
                i = end + 1;
            }

            if (i < source.length - 1 && includeSurroundingLines)
                this.lines.add(new Line(i, CharArrayUtils.getLineEnd(source, i)));
        }

        public Context addHighlight(int offset, int offsetEnd, int color) {
            this.highlights.add(new Highlight(offset, offsetEnd, color));
            return this;
        }

        public Context addMessage(int offset, String message, int color) {
            if (this.lines.isEmpty())
                throw new IllegalStateException();
            if (offset < this.lines.get(0).offset || offset > this.lines.get(this.lines.size() - 1).offsetEnd)
                throw new IllegalArgumentException("Offset out of bounds");

            var eof = offset == ProblemPrettyPrinter.this.source.length;
            var line = eof ? this.lines.get(this.lines.size() - 1) : this.lines.stream().filter(l -> l.offset <= offset && offset < l.offsetEnd).findFirst().orElseThrow();
            var index = this.lines.indexOf(line);
            if (this.messages.stream().anyMatch(m -> m.lineIndex == index))
                throw new IllegalArgumentException("Line already has a message");

            this.messages.add(new Message(index, eof ? line.offsetEnd - line.offset : offset - line.offset, message, color));
            return this;
        }

        public ProblemPrettyPrinter end() {
            return ProblemPrettyPrinter.this;
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();
            var useAnsiColors = ProblemPrettyPrinter.this.useAnsiColors;

            var lineNumber = CharArrayUtils.getLineNumber(ProblemPrettyPrinter.this.source, this.lines.get(0).offset) + 1;
            var lineNumberWidth = String.valueOf(lineNumber + this.lines.size() - 1).length();

            for (int i = 0; i < this.lines.size(); i++, lineNumber++) {
                var line = this.lines.get(i);
                // Add line prefix
                {
                    builder
                            .append(useAnsiColors ? "\u001b[0;36m" : "")
                            .append(String.format("%" + lineNumberWidth + "d", lineNumber))
                            .append(" | ")
                            .append(useAnsiColors ? "\u001b[0m" : "");
                }

                // Add line contents
                {
                    var marker = builder.length();
                    builder
                            .append(CharArrayUtils.getLineContents(ProblemPrettyPrinter.this.source, line.offset))
                            .append("\n");
                    if (useAnsiColors) {
                        // Only supports a single highlight per line
                        this.highlights.stream()
                                .filter(h -> line.offset <= h.offset && h.offset <= line.offsetEnd || // Start in line
                                             line.offset <= h.offsetEnd && h.offsetEnd <= line.offsetEnd || // End in line
                                             h.offset <= line.offset && line.offsetEnd <= h.offsetEnd) // Surrounds line
                                .findFirst().ifPresent(h -> {
                                    var start = h.offset < line.offset ? 0 : h.offset - line.offset;
                                    var end = h.offsetEnd > line.offsetEnd ? line.offsetEnd - line.offset : h.offsetEnd - line.offset;
                                    builder
                                            .insert(marker + end, "\u001b[0m")
                                            .insert(marker + start, "\u001b[" + h.color + "m");
                                });
                    }
                }

                var finalI = i;
                // Add message to line
                {
                    this.messages.stream().filter(m -> m.lineIndex == finalI).findFirst().ifPresent(message -> {
                        builder
                                .append(" ".repeat(lineNumberWidth))
                                .append(useAnsiColors ? "\u001b[0;36m" : "")
                                .append(" | ")
                                .append(useAnsiColors ? "\u001b[0m" : "")
                                .append(" ".repeat(message.column))
                                .append(useAnsiColors ? "\u001b[0;" + message.color + "m" : "")
                                .append("^ ")
                                .append(message.message)
                                .append(useAnsiColors ? "\u001b[0m" : "")
                                .append("\n");
                    });
                }
            }

            return builder.toString();
        }

        private record Highlight(int offset, int offsetEnd, int color) {}

        private record Line(int offset, int offsetEnd) {}

        private record Message(int lineIndex, int column, String message, int color) {}
    }
}
