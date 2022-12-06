package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.span.Span;

public class DelegateModuleLoader implements IModuleLoader {

    private final IModuleLoader delegate;

    public DelegateModuleLoader(IModuleLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toUniquePath(String relativeToUniquePath, Span path) {
        return this.delegate.toUniquePath(relativeToUniquePath, path);
    }

    @Override
    public SourceFileUnit loadModule(String uniquePath) {
        return this.delegate.loadModule(uniquePath);
    }

    @Override
    public void initializeModule(SourceFileUnit unit) {
        this.delegate.initializeModule(unit);
    }
}
