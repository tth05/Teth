package com.github.tth05.teth.lang.source;

public class InMemorySource implements ISource {

    private final String moduleName;
    private final char[] contents;

    public InMemorySource(String moduleName, String contents) {
        this(moduleName, contents.toCharArray());
    }

    public InMemorySource(String moduleName, char[] contents) {
        this.moduleName = moduleName;
        this.contents = contents;
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public char[] getContents() {
        return this.contents;
    }
}
