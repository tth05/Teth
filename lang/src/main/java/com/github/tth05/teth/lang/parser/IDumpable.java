package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public interface IDumpable {

    void dump(ASTDumpBuilder builder);

    default String dumpToString() {
        var b = new ASTDumpBuilder();
        dump(b);
        return b.toString();
    }
}
