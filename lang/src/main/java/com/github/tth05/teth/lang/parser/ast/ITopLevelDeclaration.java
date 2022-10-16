package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.SourceFileUnit;

public sealed interface ITopLevelDeclaration permits FunctionDeclaration, StructDeclaration, UseStatement {

    SourceFileUnit getContainingUnit();
}
