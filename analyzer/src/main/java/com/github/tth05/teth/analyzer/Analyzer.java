package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.visitor.NameAnalysis;
import com.github.tth05.teth.analyzer.visitor.ReturnStatementVerifier;
import com.github.tth05.teth.analyzer.visitor.TypeAnalysis;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.*;

public class Analyzer {

    private final Map<IDeclarationReference, Statement> resolvedReferences = new IdentityHashMap<>();
    private final Map<FunctionDeclaration, Integer> functionLocalsCount = new IdentityHashMap<>();

    private final LinkedHashMap<String, SourceFileUnitIndex> unitIndexMap;

    public Analyzer(List<SourceFileUnit> units) {
        this.unitIndexMap = new LinkedHashMap<>(units.size());
        for (SourceFileUnit unit : units)
            this.unitIndexMap.put(unit.getModuleName(), new SourceFileUnitIndex(unit));
    }

    public List<AnalyzerResult> analyze() {
        var results = new ArrayList<AnalyzerResult>(this.unitIndexMap.size());
        var nameAnalysisStates = new ArrayList<NameAnalysis>(this.unitIndexMap.size());

        // Pre-decl visit for all units
        this.unitIndexMap.forEach((name, index) -> {
            var unit = index.getUnit();
            Prelude.injectStatements(unit.getStatements());
            var nameAnalysis = new NameAnalysis(this, this.resolvedReferences, this.functionLocalsCount);
            nameAnalysis.preDeclVisit(unit);
            // Temporary save the name analysis state
            nameAnalysisStates.add(nameAnalysis);
        });

        var i = 0;
        for (var entry : this.unitIndexMap.entrySet()) {
            var index = entry.getValue();
            var unit = index.getUnit();

            // Safe, because the map is ordered
            var nameAnalysis = nameAnalysisStates.get(i);
            nameAnalysis.visit(unit);
            var problems = nameAnalysis.getProblems();

            var typeAnalysis = new TypeAnalysis(this.resolvedReferences);
            typeAnalysis.visit(unit);
            problems.merge(typeAnalysis.getProblems());

            var returnStatementVerifier = new ReturnStatementVerifier();
            returnStatementVerifier.visit(unit);
            problems.merge(returnStatementVerifier.getProblems());

            results.add(new AnalyzerResult(entry.getKey(), problems));
            i++;
        }

        return results;
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
