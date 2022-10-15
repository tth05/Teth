package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.analyzer.module.DelegateModuleLoader;
import com.github.tth05.teth.analyzer.module.IModuleLoader;
import com.github.tth05.teth.analyzer.module.ModuleCache;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.type.TypeCache;
import com.github.tth05.teth.analyzer.visitor.NameAnalysis;
import com.github.tth05.teth.analyzer.visitor.ReturnStatementVerifier;
import com.github.tth05.teth.analyzer.visitor.TypeAnalysis;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IDeclarationReference;
import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.*;

public class Analyzer {

    private final Map<IDeclarationReference, Statement> resolvedReferences = new IdentityHashMap<>();
    private final Map<FunctionDeclaration, Integer> functionLocalsCount = new IdentityHashMap<>();

    private final ModuleCache moduleCache = new ModuleCache();
    private final TypeCache typeCache = new TypeCache();

    private List<AnalyzerResult> results;
    private Deque<AnalysisState> additionalUnits;
    private SourceFileUnit entryPoint;
    private boolean analyzeEntryPointOnly;

    public Analyzer(SourceFileUnit entryPoint) {
        this.entryPoint = internalizeUnit(entryPoint);
        this.moduleCache.addModule(this.entryPoint);
    }

    public void setModuleLoader(IModuleLoader loader) {
        this.moduleCache.setModuleLoader(new DelegateModuleLoader(loader) {
            @Override
            public SourceFileUnit loadModule(String uniquePath) {
                var unit = loader.loadModule(uniquePath);
                if (unit == null)
                    return null;

                unit = internalizeUnit(unit);
                return unit;
            }

            @Override
            public void initializeModule(SourceFileUnit unit) {
                var state = preAnalyzeUnit(unit);
                if (Analyzer.this.analyzeEntryPointOnly) {
                    Analyzer.this.results.add(new AnalyzerResult(unit.getUniquePath(), state.getProblems()));
                    return;
                }

                Analyzer.this.additionalUnits.addLast(new AnalysisState(unit, state));
            }
        });
    }

    public void setAnalyzeEntryPointOnly(boolean value) {
        this.analyzeEntryPointOnly = value;
    }

    public List<AnalyzerResult> analyze() {
        if (this.entryPoint == null)
            throw new IllegalStateException("Cannot run analysis twice");

        if (!this.analyzeEntryPointOnly)
            this.additionalUnits = new ArrayDeque<>();

        this.results = new ArrayList<>();
        this.results.add(0, analyzeUnit(this.entryPoint, preAnalyzeUnit(this.entryPoint)));

        if (!this.analyzeEntryPointOnly) {
            while (!this.additionalUnits.isEmpty()) {
                var element = this.additionalUnits.pop();
                this.results.add(analyzeUnit(element.unit(), element.state()));
            }
        }

        var results = this.results;
        this.entryPoint = null;
        this.results = null;
        this.additionalUnits = null;
        return results;
    }

    private NameAnalysis preAnalyzeUnit(SourceFileUnit unit) {
        var nameAnalysis = new NameAnalysis(this, this.resolvedReferences, this.functionLocalsCount);
        nameAnalysis.preDeclVisit(unit);
        return nameAnalysis;
    }

    private AnalyzerResult analyzeUnit(SourceFileUnit unit, NameAnalysis nameAnalysis) {
        // Safe, because the map is ordered
        nameAnalysis.visit(unit);
        var problems = nameAnalysis.getProblems();

        var typeAnalysis = new TypeAnalysis(this.typeCache, this.resolvedReferences);
        typeAnalysis.visit(unit);
        problems.merge(typeAnalysis.getProblems());

        var returnStatementVerifier = new ReturnStatementVerifier();
        returnStatementVerifier.visit(unit);
        problems.merge(returnStatementVerifier.getProblems());

        return new AnalyzerResult(unit.getUniquePath(), problems);
    }

    public Statement resolvedReference(IDeclarationReference reference) {
        return this.resolvedReferences.get(reference);
    }

    public int functionLocalsCount(FunctionDeclaration function) {
        return Objects.requireNonNull(this.functionLocalsCount.getOrDefault(function, null), "Function locals count not set");
    }

    public boolean hasModule(String name) {
        return this.moduleCache.hasModule(name);
    }

    public String toUniquePath(String relativeToUniquePath, String path) {
        return this.moduleCache.toUniquePath(relativeToUniquePath, path);
    }

    public Statement findExportedDeclaration(String uniquePath, String name) {
        return this.moduleCache.findExportedDeclaration(uniquePath, name);
    }

    private SourceFileUnit internalizeUnit(SourceFileUnit unit) {
        // Don't modify the original unit
        var newStatements = new StatementList(unit.getStatements());
        Prelude.injectStatements(newStatements);
        return new SourceFileUnit(unit.getUniquePath(), newStatements);
    }

    private record AnalysisState(SourceFileUnit unit, NameAnalysis state) {}
}
