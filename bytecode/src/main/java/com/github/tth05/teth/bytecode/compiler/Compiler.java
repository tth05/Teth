package com.github.tth05.teth.bytecode.compiler;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.bytecode.decoder.*;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.*;

public class Compiler {

    private SourceFileUnit mainUnit;

    public void setMainUnit(SourceFileUnit unit) {
        this.mainUnit = unit;
    }

    public CompilationResult compile() {
        if (this.mainUnit == null)
            throw new IllegalStateException("No main unit set");

        var analyzer = new Analyzer(this.mainUnit);
        var problems = analyzer.analyze();
        if (!problems.isEmpty())
            return new CompilationResult(problems);

        var generator = new BytecodeGeneratorVisitor(analyzer);
        generator.visit(this.mainUnit);
        return new CompilationResult(generator.toArray());
    }

    private static class BytecodeGeneratorVisitor extends ASTVisitor {

        private static final FunctionDeclaration GLOBAL_FUNCTION = new FunctionDeclaration(null, null, null, null, null);

        private final Map<FunctionDeclaration, List<IInstrunction>> functionInsnMap = new IdentityHashMap<>();
        private final Analyzer analyzer;

        private List<IInstrunction> currentFunctionInsn = new ArrayList<>();

        public BytecodeGeneratorVisitor(Analyzer analyzer) {
            this.analyzer = analyzer;
        }

        public IInstrunction[] toArray() {
            var i = 0;
            var insns = new IInstrunction[this.functionInsnMap.values().stream().mapToInt(List::size).sum()];
            var functionOffsets = new IdentityHashMap<FunctionDeclaration, Integer>();

            // Ensure global function comes first
            var sortedFunctions = this.functionInsnMap.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey() != GLOBAL_FUNCTION)).toList();
            for (var entry : sortedFunctions) {
                var function = entry.getKey();
                var insnList = entry.getValue();

                functionOffsets.put(function, i);
                for (var insn : insnList)
                    insns[i++] = insn;
            }

            for (int j = 0; j < insns.length; j++) {
                var insn = insns[j];
                if (!(insn instanceof PlaceholderInvokeInsn placeholder))
                    continue;

                var function = placeholder.target;
                var offset = functionOffsets.get(function);

                // TODO: Compute locals count
                insns[j] = new INVOKE_Insn(false, function.getParameters().size(), 0, offset);
            }

            return insns;
        }

        @Override
        public void visit(SourceFileUnit unit) {
            this.functionInsnMap.put(GLOBAL_FUNCTION, this.currentFunctionInsn);
            super.visit(unit);
            this.currentFunctionInsn.add(new EXIT_Insn());
        }

        @Override
        public void visit(FunctionDeclaration declaration) {
            var parentFunction = this.currentFunctionInsn;
            this.currentFunctionInsn = new ArrayList<>();
            this.functionInsnMap.put(declaration, this.currentFunctionInsn);

            super.visit(declaration);

            this.currentFunctionInsn = parentFunction;
        }

        @Override
        public void visit(FunctionInvocationExpression invocation) {
            super.visit(invocation);

            var reference = (FunctionDeclaration) this.analyzer.resolvedReference(((IDeclarationReference) invocation.getTarget()));
            if (reference.isIntrinsic()) {
                this.currentFunctionInsn.add(new INVOKE_INTRINSIC_Insn(reference.getNameExpr().getValue()));
            } else {
                this.currentFunctionInsn.add(new PlaceholderInvokeInsn(
                        reference
                ));
            }
        }

        @Override
        public void visit(ReturnStatement returnStatement) {
            super.visit(returnStatement);

            this.currentFunctionInsn.add(new RETURN_Insn(returnStatement.getValueExpr() != null));
        }

        @Override
        public void visit(BinaryExpression expression) {
            super.visit(expression);

            switch (expression.getOperator()) {
                case OP_ADD -> this.currentFunctionInsn.add(new LD_ADD_Insn());
                case OP_LESS_EQUAL -> this.currentFunctionInsn.add(new LD_LESS_EQUAL_Insn());
                default -> throw new UnsupportedOperationException();
            }
        }

        @Override
        public void visit(LongLiteralExpression longLiteralExpression) {
            this.currentFunctionInsn.add(new L_CONST_Insn(longLiteralExpression.getValue()));
        }

        @Override
        public void visit(DoubleLiteralExpression doubleLiteralExpression) {
            this.currentFunctionInsn.add(new D_CONST_Insn(doubleLiteralExpression.getValue()));
        }

        @Override
        public void visit(BooleanLiteralExpression booleanLiteralExpression) {
            this.currentFunctionInsn.add(new B_CONST_Insn(booleanLiteralExpression.getValue()));
        }

        @Override
        public void visit(StringLiteralExpression stringLiteralExpression) {
            this.currentFunctionInsn.add(new S_CONST_Insn(stringLiteralExpression.getValue()));
        }

        @Override
        public void visit(ListLiteralExpression listLiteralExpression) {
            this.currentFunctionInsn.add(new CREATE_LIST_Insn());
            listLiteralExpression.getInitializers().forEach(e -> {
                this.currentFunctionInsn.add(new DUP_Insn());
                e.accept(this);
                this.currentFunctionInsn.add(new INVOKE_INTRINSIC_Insn("list.add"));
            });
        }
    }

    private static final class PlaceholderInvokeInsn implements IInstrunction {

        private final FunctionDeclaration target;

        private PlaceholderInvokeInsn(FunctionDeclaration target) {
            this.target = target;
        }

        @Override
        public byte getOpCode() {
            throw new UnsupportedOperationException();
        }
    }
}
