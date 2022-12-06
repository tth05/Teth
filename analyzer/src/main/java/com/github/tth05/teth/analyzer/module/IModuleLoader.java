package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.span.Span;

public interface IModuleLoader {

    String toUniquePath(String relativeToUniquePath, Span path);

    SourceFileUnit loadModule(String uniquePath);

    default void initializeModule(SourceFileUnit unit) {
    }
}
