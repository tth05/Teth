package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.visitor.NameAnalysis;
import com.github.tth05.teth.analyzer.visitor.ReturnStatementVerifier;
import com.github.tth05.teth.analyzer.visitor.TypeAnalysis;
import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IDeclarationReference;
import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class Analyzer {

    private final Map<IDeclarationReference, Statement> resolvedReferences = new IdentityHashMap<>();
    private final Map<FunctionDeclaration, Integer> functionLocalsCount = new IdentityHashMap<>();

    private final SourceFileUnit unit;

    public Analyzer(SourceFileUnit unit) {
        this.unit = unit;
    }

    public ProblemList analyze() {
        try {
            Prelude.injectStatements(this.unit.getStatements());
            new NameAnalysis(this.resolvedReferences, this.functionLocalsCount).visit(this.unit);
            new TypeAnalysis(this.resolvedReferences).visit(this.unit);
            new ReturnStatementVerifier().visit(this.unit);
            return ProblemList.of();
        } catch (TypeResolverException | ValidationException e) {
            return ProblemList.of(e.asProblem());
        }
    }

    public Statement resolvedReference(IDeclarationReference identifierExpression) {
        return this.resolvedReferences.get(identifierExpression);
    }

    public int functionLocalsCount(FunctionDeclaration function) {
        return Objects.requireNonNull(this.functionLocalsCount.getOrDefault(function, null), "Function locals count not set");
    }
}
