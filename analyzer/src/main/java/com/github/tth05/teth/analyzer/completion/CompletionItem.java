package com.github.tth05.teth.analyzer.completion;

public record CompletionItem(String text, String tailText, String typeText,
                             com.github.tth05.teth.analyzer.completion.CompletionItem.Type type, int priority) {

    public static final int PRELUDE_PRIORITY = 0;
    public static final int TOP_LEVEL_PRIORITY = 1;
    public static final int SCOPE_STACK_PRIORITY = 2;
    public static final int LOCALS_PRIORITY = 3;

    public enum Type {
        STRUCT,
        FUNCTION,
        FIELD,
        VARIABLE,
        PARAMETER;
    }
}
