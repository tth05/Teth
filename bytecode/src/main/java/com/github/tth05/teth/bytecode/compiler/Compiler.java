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
            var i = 1;
            var insns = new IInstrunction[this.functionInsnMap.values().stream().mapToInt(List::size).sum() + 1];
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
            for (int j = 1; j < insns.length; j++) {
                var insn = insns[j];
                if (!(insn instanceof PlaceholderInvokeInsn placeholder))
                    continue;

                var function = placeholder.target;
                var offset = functionOffsets.get(function);

                insns[j] = new INVOKE_Insn(
                        function.isInstanceFunction(),
                        function.getParameters().size() + (function.isInstanceFunction() ? 1 : 0),
                        this.analyzer.functionLocalsCount(function),
                        offset - 1
                );
            }

            // "Invoke" global function
            insns[0] = new INVOKE_Insn(false, 0, this.analyzer.functionLocalsCount(Analyzer.GLOBAL_FUNCTION), 0);

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

            // Add hidden 'self' parameter
            if (declaration.isInstanceFunction())
                this.currentFunctionLocals.put("self", 0);
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
        public void visit(ObjectCreationExpression expression) {
            { // Avoid generating code for the target name
                expression.getParameters().forEach(p -> p.accept(this));
            }

            var structDeclaration = (StructDeclaration) this.analyzer.resolvedReference(expression.getTargetNameExpr());
            this.currentFunctionInsn.add(new CREATE_OBJECT_Insn((short) structDeclaration.getFields().size()));
        }

        @Override
        public void visit(VariableDeclaration declaration) {
            {
                declaration.getInitializerExpr().accept(this);
            }

            var idx = this.currentFunctionLocals.size();
            this.currentFunctionLocals.put(declaration.getNameExpr().getValue(), idx);
            this.currentFunctionInsn.add(new STORE_LOCAL_Insn(idx));
        }

        @Override
        public void visit(VariableAssignmentExpression expression) {
            {
                expression.getExpr().accept(this);
            }

            if (expression.getTargetExpr() instanceof IdentifierExpression identifierExpression) {
                var idx = this.currentFunctionLocals.get(identifierExpression.getValue());
                this.currentFunctionInsn.add(new STORE_LOCAL_Insn(idx));
            } else if (expression.getTargetExpr() instanceof MemberAccessExpression memberAccessExpression) {
                memberAccessExpression.getTarget().accept(this);

                var field = (StructDeclaration.FieldDeclaration) this.analyzer.resolvedReference(memberAccessExpression);
                this.currentFunctionInsn.add(new STORE_MEMBER_Insn((short) field.getIndex()));
            } else {
                throw new UnsupportedOperationException("Cannot assign to " + expression.getTargetExpr());
            }
        }

        @Override
        public void visit(MemberAccessExpression expression) {
            {
                expression.getTarget().accept(this);
            }

            var member = this.analyzer.resolvedReference(expression);

            if (member instanceof StructDeclaration.FieldDeclaration field) {
                this.currentFunctionInsn.add(new LOAD_MEMBER_Insn((short) field.getIndex()));
            } else if (member instanceof FunctionDeclaration) {
                // NO OP, handled by FunctionInvocationExpression
            } else {
                throw new UnsupportedOperationException();
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
            var hasElseBlock = statement.getElseStatement() != null;

            // If start placeholder
            var startIndex = this.currentFunctionInsn.size();
            this.currentFunctionInsn.add(null);

            statement.getBody().accept(this);
            // Body end placeholder
            var bodyEndIndex = this.currentFunctionInsn.size();
            if (hasElseBlock)
                this.currentFunctionInsn.add(null);

            // Jump after body if condition is false
            this.currentFunctionInsn.set(startIndex, new JUMP_IF_FALSE_Insn(this.currentFunctionInsn.size() - startIndex - 1));

            var elseStatement = statement.getElseStatement();
            if (hasElseBlock)
                elseStatement.accept(this);

            if (hasElseBlock) {
                // Jump after else if condition is true
                this.currentFunctionInsn.set(bodyEndIndex, new JUMP_Insn(this.currentFunctionInsn.size() - bodyEndIndex - 1));
            }
        }

        @Override
        public void visit(LoopStatement statement) {
            statement.getVariableDeclarations().forEach(v -> v.accept(this));

            var startIndex = this.currentFunctionInsn.size();

            var condition = statement.getCondition();
            if (condition != null)
                condition.accept(this);
            else
                this.currentFunctionInsn.add(new B_CONST_Insn(true)); // Infinite loop

            // Loop condition placeholder
            var conditionIndex = this.currentFunctionInsn.size();
            this.currentFunctionInsn.add(null);

            statement.getBody().accept(this);
            var advanceStatement = statement.getAdvanceStatement();
            if (advanceStatement != null)
                advanceStatement.accept(this);

            // Unconditional jump to start
            this.currentFunctionInsn.add(new JUMP_Insn(startIndex - this.currentFunctionInsn.size() - 1));

            // Jump after body if condition is false
            this.currentFunctionInsn.set(conditionIndex, new JUMP_IF_FALSE_Insn(this.currentFunctionInsn.size() - conditionIndex - 1));
        }

        @Override
        public void visit(BinaryExpression expression) {
            super.visit(expression);

            switch (expression.getOperator()) {
                case OP_ADD -> this.currentFunctionInsn.add(new LD_ADD_Insn());
                case OP_SUBTRACT -> this.currentFunctionInsn.add(new LD_SUB_Insn());
                case OP_MULTIPLY -> this.currentFunctionInsn.add(new LD_MUL_Insn());
                case OP_DIVIDE -> this.currentFunctionInsn.add(new LD_DIV_Insn());
                case OP_LESS -> this.currentFunctionInsn.add(new LD_LESS_Insn());
                case OP_LESS_EQUAL -> this.currentFunctionInsn.add(new LD_LESS_EQUAL_Insn());
                case OP_EQUAL -> this.currentFunctionInsn.add(new LD_EQUAL_Insn());
                case OP_NOT_EQUAL -> {
                    this.currentFunctionInsn.add(new LD_EQUAL_Insn());
                    this.currentFunctionInsn.add(new B_INVERT_Insn());
                }
                default -> throw new UnsupportedOperationException("Unsupported operator: " + expression.getOperator());
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
                return; //TODO: Looks like a bad idea

            var varIndex = this.currentFunctionLocals.get(varDecl.getNameExpr().getValue());
            if (varIndex == null)
                throw new IllegalStateException("Variable not found " + varDecl);

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
