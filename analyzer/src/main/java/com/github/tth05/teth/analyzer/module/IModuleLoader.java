package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.parser.SourceFileUnit;

public interface IModuleLoader {

    SourceFileUnit loadModule(String moduleName);

    default void initializeModule(SourceFileUnit unit) {
    }
}
