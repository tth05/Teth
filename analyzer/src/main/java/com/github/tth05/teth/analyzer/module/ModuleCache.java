package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.IHasName;
import com.github.tth05.teth.lang.parser.ast.ITopLevelDeclaration;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.span.Span;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleCache {

    private static final Matcher MODULE_PATH_MATCHER = Pattern.compile("^((\\.{2}|/|[^/.]+?)/)*?[^/.]+$").matcher("");

    private final Map<String, SourceFileUnitIndex> unitIndexMap = new HashMap<>();
    private IModuleLoader moduleLoader;

    public void setModuleLoader(IModuleLoader loader) {
        this.moduleLoader = loader;
    }

    public void addModule(SourceFileUnit unit) {
        this.unitIndexMap.put(unit.getUniquePath(), new SourceFileUnitIndex(unit));
    }

    public boolean hasModule(String uniquePath) {
        loadModule(uniquePath);
        return this.unitIndexMap.get(uniquePath) != null;
    }

    public Statement findExportedDeclaration(String uniquePath, Span name) {
        loadModule(uniquePath);

        var index = this.unitIndexMap.get(uniquePath);
        if (index == null)
            return null;

        return index.findExportedDeclaration(name);
    }

    public String toUniquePath(String relativeToUniquePath, Span moduleName) {
        return this.moduleLoader == null ? moduleName.getText() : this.moduleLoader.toUniquePath(relativeToUniquePath, moduleName);
    }

    private void loadModule(String path) {
        if (path == null || path.isBlank() || hasResolvedModule(path) || this.moduleLoader == null)
            return;

        var module = this.moduleLoader.loadModule(path);
        if (module == null) {
            this.unitIndexMap.put(path, null);
            return;
        }

        if (!path.equals(module.getUniquePath()))
            throw new IllegalStateException("Module loader returned a module with a different path than requested. Requested: '%s', got: '%s'".formatted(path, module.getUniquePath()));

        addModule(module);
        this.moduleLoader.initializeModule(module);
    }

    private boolean hasResolvedModule(String path) {
        return this.unitIndexMap.containsKey(path);
    }

    public static boolean isValidModulePath(Span path) {
        if (path == null || path.length() < 1)
            return false;

        MODULE_PATH_MATCHER.reset(path);
        return MODULE_PATH_MATCHER.matches();
    }

    private static class SourceFileUnitIndex {

        private final Map<Span, Statement> exportedStatementsMap;

        public SourceFileUnitIndex(SourceFileUnit unit) {
            this.exportedStatementsMap = new HashMap<>(unit.getStatements().size());
            unit.getStatements().stream()
                    .filter(s -> s instanceof ITopLevelDeclaration && s instanceof IHasName)
                    .filter(s -> ((IHasName) s).getNameExpr().getSpan() != null)
                    .forEach(s -> this.exportedStatementsMap.put(((IHasName) s).getNameExpr().getSpan(), s));
        }

        public Statement findExportedDeclaration(Span name) {
            return this.exportedStatementsMap.get(name);
        }
    }
}
