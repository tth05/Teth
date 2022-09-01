package com.github.tth05.teth.lang.diagnostics;

import com.github.tth05.teth.lang.span.ISpan;

public record Problem(ISpan span, String message) {

    public String prettyPrint(boolean useAnsiColors) {
        return ProblemPrettyPrinter.ofSource(this.span.source().getContents())
                .useAnsiColors(useAnsiColors)
                .withContext(this.span.offset(), this.span.offsetEnd(), true)
                .addHighlight(this.span.offset(), this.span.offsetEnd(), 31)
                .addMessage(this.span.offset(), this.message, 31)
                .end()
                .toString();
    }
}
