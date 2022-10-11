package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.IHasName;
import com.github.tth05.teth.lang.parser.ast.ITopLevelDeclaration;
import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.HashMap;
import java.util.Map;

public class ModuleCache {

    private final Map<String, SourceFileUnitIndex> unitIndexMap = new HashMap<>();
    private IModuleLoader moduleLoader;

    public void setModuleLoader(IModuleLoader loader) {
        this.moduleLoader = loader;
    }

    public void addModule(SourceFileUnit unit) {
        this.unitIndexMap.put(unit.getModuleName(), new SourceFileUnitIndex(unit));
    }

    public boolean hasModule(String name) {
        loadModule(name);
        return this.unitIndexMap.get(name) != null;
    }

    public Statement findExportedDeclaration(String moduleName, String name) {
        loadModule(moduleName);

        var index = this.unitIndexMap.get(moduleName);
        if (index == null)
            return null;

        return index.findExportedDeclaration(name);
    }

    private void loadModule(String name) {
        if (hasResolvedModule(name) || this.moduleLoader == null)
            return;

        var module = this.moduleLoader.loadModule(name);
        if (module == null) {
            this.unitIndexMap.put(name, null);
            return;
        }

        if (!name.equals(module.getModuleName()))
            throw new IllegalStateException("Module loader returned a module with a different name than requested. Requested: '%s', got: '%s'".formatted(name, module.getModuleName()));

        addModule(module);
        this.moduleLoader.initializeModule(module);
    }

    private boolean hasResolvedModule(String name) {
        return this.unitIndexMap.containsKey(name);
    }

    private static class SourceFileUnitIndex {

        private final Map<String, Statement> exportedStatementsMap;

        public SourceFileUnitIndex(SourceFileUnit unit) {
            this.exportedStatementsMap = new HashMap<>(unit.getStatements().size());
            unit.getStatements().stream()
                    .filter(s -> s instanceof ITopLevelDeclaration && s instanceof IHasName)
                    .filter(s -> ((IHasName) s).getNameExpr().getValue() != null)
                    .forEach(s -> this.exportedStatementsMap.put(((IHasName) s).getNameExpr().getValue(), s));
        }

        public Statement findExportedDeclaration(String name) {
            return this.exportedStatementsMap.get(name);
        }
    }
}
