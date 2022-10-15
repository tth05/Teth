package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.parser.SourceFileUnit;

public interface IModuleLoader {

    String toUniquePath(String relativeToUniquePath, String path);

    SourceFileUnit loadModule(String uniquePath);

    default void initializeModule(SourceFileUnit unit) {
    }
}
