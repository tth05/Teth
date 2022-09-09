package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.visitor.NameAnalysis;
import com.github.tth05.teth.analyzer.visitor.ReturnStatementVerifier;
import com.github.tth05.teth.analyzer.visitor.TypeAnalysis;
import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.*;

public class Analyzer {

    private final Map<IDeclarationReference, Statement> resolvedReferences = new IdentityHashMap<>();
    private final Map<FunctionDeclaration, Integer> functionLocalsCount = new IdentityHashMap<>();

    private final Map<String, SourceFileUnitIndex> unitIndexMap;

    public Analyzer(List<SourceFileUnit> units) {
        this.unitIndexMap = new LinkedHashMap<>(units.size());
        for (SourceFileUnit unit : units)
            this.unitIndexMap.put(unit.getModuleName(), new SourceFileUnitIndex(unit));
    }

    public ProblemList analyze() {
        var problems = new ProblemList();

        //TODO: The multi step analysis should advance at the same time for all input units

        this.unitIndexMap.forEach((name, index) -> {
            try {
                var unit = index.getUnit();
                Prelude.injectStatements(unit.getStatements());
                new NameAnalysis(this, this.resolvedReferences, this.functionLocalsCount).visit(unit);
                new TypeAnalysis(this.resolvedReferences).visit(unit);
                new ReturnStatementVerifier().visit(unit);
            } catch (TypeResolverException | ValidationException e) {
                problems.add(e.asProblem());
            }
        });

        return problems;
    }

    public Statement resolvedReference(IDeclarationReference reference) {
        return this.resolvedReferences.get(reference);
    }

    public int functionLocalsCount(FunctionDeclaration function) {
        return Objects.requireNonNull(this.functionLocalsCount.getOrDefault(function, null), "Function locals count not set");
    }

    public boolean hasModule(String name) {
        return this.unitIndexMap.containsKey(name);
    }

    public Statement findExportedDeclaration(String moduleName, String name) {
        var index = this.unitIndexMap.get(moduleName);
        if (index == null)
            return null;

        return index.findExportedDeclaration(name);
    }

    private static class SourceFileUnitIndex {

        private final SourceFileUnit unit;
        private final Map<String, Statement> exportedStatementsMap;

        public SourceFileUnitIndex(SourceFileUnit unit) {
            this.unit = unit;
            this.exportedStatementsMap = new HashMap<>(unit.getStatements().size());
            unit.getStatements().stream()
                    .filter(s -> s instanceof ITopLevelDeclaration && s instanceof IHasName)
                    .forEach(s -> this.exportedStatementsMap.put(((IHasName) s).getNameExpr().getValue(), s));
        }

        public SourceFileUnit getUnit() {
            return this.unit;
        }

        public Statement findExportedDeclaration(String name) {
            return this.exportedStatementsMap.get(name);
        }
    }
}
