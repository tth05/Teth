package com.github.tth05.teth.analyzer.completion;

public record CompletionItem(String text, com.github.tth05.teth.analyzer.completion.CompletionItem.Type type) {

    public enum Type {
        STRUCT,
        FUNCTION;
    }
}
