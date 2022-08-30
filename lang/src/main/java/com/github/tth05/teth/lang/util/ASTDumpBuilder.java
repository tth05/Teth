package com.github.tth05.teth.lang.util;

import com.github.tth05.teth.lang.parser.IDumpable;

import java.util.List;

public class ASTDumpBuilder {

    private final StringBuilder builder = new StringBuilder();
    private int indentationLevel;
    private boolean isAtLineStart = true;

    public ASTDumpBuilder startBlock() {
        this.indentationLevel++;
        return this;
    }

    public ASTDumpBuilder endBlock() {
        this.indentationLevel--;
        return this;
    }

    public ASTDumpBuilder newLine() {
        this.builder.append("\n");
        this.isAtLineStart = true;
        return this;
    }

    public ASTDumpBuilder append(String s) {
        if (this.isAtLineStart)
            this.builder.append("    ".repeat(this.indentationLevel));

        this.builder.append(s);
        this.isAtLineStart = false;
        return this;
    }

    public ASTDumpBuilder appendAttribute(String name) {
        append(name).append(" = ");
        return this;
    }

    public ASTDumpBuilder appendAttribute(String name, String value) {
        appendAttribute(name);
        append(value);
        return this;
    }

    public ASTDumpBuilder appendAttribute(String name, List<? extends IDumpable> list) {
        appendAttribute(name);
        append("[").newLine().startBlock();
        for (var dumpable : list) {
            dumpable.dump(this);
            newLine();
        }
        endBlock().append("]");
        return this;
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }
}
