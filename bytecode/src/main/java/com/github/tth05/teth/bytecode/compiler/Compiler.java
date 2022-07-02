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

        private final Map<FunctionDeclaration, List<IInstrunction>> functionInsnMap = new IdentityHashMap<>();
        private final Analyzer analyzer;

        private List<IInstrunction> currentFunctionInsn = new ArrayList<>();
        private Map<String, Integer> currentFunctionLocals = new HashMap<>();

        public BytecodeGeneratorVisitor(Analyzer analyzer) {
            this.analyzer = analyzer;
        }

        public IInstrunction[] toArray() {
            var i = 0;
            var insns = new IInstrunction[this.functionInsnMap.values().stream().mapToInt(List::size).sum()];
            var functionOffsets = new IdentityHashMap<FunctionDeclaration, Integer>();

            // Ensure global function comes first
            var sortedFunctions = this.functionInsnMap.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey() != Analyzer.GLOBAL_FUNCTION)).toList();
            for (var entry : sortedFunctions) {
                var function = entry.getKey();
                var insnList = entry.getValue();

                functionOffsets.put(function, i);
                for (var insn : insnList)
                    insns[i++] = insn;
            }

            // Resolve jump addresses
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
            this.functionInsnMap.put(Analyzer.GLOBAL_FUNCTION, this.currentFunctionInsn);
            super.visit(unit);
            this.currentFunctionInsn.add(new EXIT_Insn());
        }

        @Override
        public void visit(FunctionDeclaration declaration) {
            var parentFunction = this.currentFunctionInsn;
            var parentLocals = this.currentFunctionLocals;
            this.currentFunctionInsn = new ArrayList<>();
            this.currentFunctionLocals = new HashMap<>();
            // Add parameters to locals
            for (var parameter : declaration.getParameters())
                this.currentFunctionLocals.put(parameter.getNameExpr().getValue(), this.currentFunctionLocals.size());

            this.functionInsnMap.put(declaration, this.currentFunctionInsn);

            super.visit(declaration);

            // Implicit return
            if (declaration.getReturnTypeExpr() == null)
                this.currentFunctionInsn.add(new RETURN_Insn(false));
            this.currentFunctionInsn = parentFunction;
            this.currentFunctionLocals = parentLocals;
        }

        @Override
        public void visit(FunctionInvocationExpression invocation) {
            super.visit(invocation);

            var reference = (FunctionDeclaration) this.analyzer.resolvedReference(((IDeclarationReference) invocation.getTarget()));
            if (reference.isIntrinsic()) {
                this.currentFunctionInsn.add(new INVOKE_INTRINSIC_Insn(reference.getNameExpr().getValue()));
            } else {
                this.currentFunctionInsn.add(new PlaceholderInvokeInsn(reference));
            }
        }

        @Override
        public void visit(ReturnStatement returnStatement) {
            super.visit(returnStatement);

            this.currentFunctionInsn.add(new RETURN_Insn(returnStatement.getValueExpr() != null));
        }

        @Override
        public void visit(IfStatement statement) {
            statement.getCondition().accept(this);
            var generateElse = statement.getElseStatement() != null;

            // If start placeholder
            var startIndex = this.currentFunctionInsn.size();
            this.currentFunctionInsn.add(null);

            statement.getBody().accept(this);
            // Body end placeholder
            var bodyEndIndex = this.currentFunctionInsn.size();
            if (generateElse)
                this.currentFunctionInsn.add(null);

            // Jump after body if condition is false
            this.currentFunctionInsn.set(startIndex, new JUMP_IF_FALSE_Insn(this.currentFunctionInsn.size() - startIndex));

            var elseStatement = statement.getElseStatement();
            if (generateElse)
                elseStatement.accept(this);

            if (generateElse) {
                // Jump after else if condition is true
                this.currentFunctionInsn.set(bodyEndIndex, new JUMP_Insn(this.currentFunctionInsn.size() - bodyEndIndex));
            }
        }

        @Override
        public void visit(BinaryExpression expression) {
            super.visit(expression);

            switch (expression.getOperator()) {
                case OP_ADD -> this.currentFunctionInsn.add(new LD_ADD_Insn());
                case OP_SUBTRACT -> this.currentFunctionInsn.add(new LD_SUB_Insn());
                case OP_MULTIPLY -> this.currentFunctionInsn.add(new LD_MUL_Insn());
                case OP_DIVIDE -> this.currentFunctionInsn.add(new LD_DIV_Insn());
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
        public void visit(IdentifierExpression identifierExpression) {
            var reference = this.analyzer.resolvedReference(identifierExpression);
            if (!(reference instanceof IVariableDeclaration varDecl))
                return;

            var varIndex = this.currentFunctionLocals.get(varDecl.getNameExpr().getValue());
            if (varIndex == null)
                throw new IllegalStateException("Variable not found");

            this.currentFunctionInsn.add(new LOAD_LOCAL_Insn(varIndex));
        }

        @Override
        public void visit(ListLiteralExpression listLiteralExpression) {
            this.currentFunctionInsn.add(new CREATE_LIST_Insn());
            for (int i = 0; i < listLiteralExpression.getInitializers().size(); i++)
                this.currentFunctionInsn.add(new DUP_Insn());

            listLiteralExpression.getInitializers().forEach(e -> {
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

        @Override
        public String getDebugParametersString() {
            throw new UnsupportedOperationException();
        }
    }
}
