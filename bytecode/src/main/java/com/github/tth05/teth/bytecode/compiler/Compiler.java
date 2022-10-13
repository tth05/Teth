package com.github.tth05.teth.bytecode.compiler;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.AnalyzerResult;
import com.github.tth05.teth.analyzer.module.IModuleLoader;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.visitor.NameAnalysis;
import com.github.tth05.teth.bytecode.compiler.internal.PlaceholderInvokeInsn;
import com.github.tth05.teth.bytecode.compiler.optimization.IOptimizer;
import com.github.tth05.teth.bytecode.compiler.optimization.StackCleaningOptimizer;
import com.github.tth05.teth.bytecode.op.*;
import com.github.tth05.teth.bytecode.program.StructData;
import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.*;
import java.util.stream.Collectors;

public class Compiler {

    private final Map<StructDeclaration, Integer> structIds = new IdentityHashMap<>();
    private final Map<FunctionDeclaration, List<IInstrunction>> functionInsnMap = new IdentityHashMap<>();

    private final List<SourceFileUnit> units = new ArrayList<>();
    private final List<IOptimizer> optimizers = new ArrayList<>();
    {
        this.optimizers.add(new StackCleaningOptimizer());
    }

    private Analyzer analyzer;

    private boolean compiled;

    public void setEntryPoint(SourceFileUnit unit) {
        if (this.compiled)
            throw new IllegalStateException("Cannot set entry point after compilation");

        this.units.add(unit);
        this.analyzer = new Analyzer(unit);
    }

    public void setModuleLoader(IModuleLoader loader) {
        if (this.compiled)
            throw new IllegalStateException("Cannot set module loader after compilation");
        if (this.analyzer == null)
            throw new IllegalStateException("Cannot set module loader before setting entry point");

        this.analyzer.setModuleLoader((name) -> {
            var unit = loader.loadModule(name);
            this.units.add(unit);
            return unit;
        });
    }

    public void addOptimizer(IOptimizer optimizer) {
        this.optimizers.add(optimizer);
    }

    public CompilationResult compile() {
        if (this.compiled)
            throw new IllegalStateException("Cannot compile twice");
        if (this.units.isEmpty())
            throw new IllegalStateException("No entry point set");

        this.compiled = true;

        var analyzerResults = this.analyzer.analyze();
        if (analyzerResults.stream().anyMatch(AnalyzerResult::hasProblems))
            return new CompilationResult(analyzerResults);

        for (int i = 0; i < this.units.size(); i++) {
            var generator = new BytecodeGeneratorVisitor(this.analyzer, i != 0);
            generator.visit(this.units.get(i));
        }

        return new CompilationResult(toProgram(this.analyzer));
    }

    private TethProgram toProgram(Analyzer analyzer) {
        var i = 1;
        var functionOffsets = new IdentityHashMap<FunctionDeclaration, Integer>();

        record FunctionInsnPair(FunctionDeclaration function, List<IInstrunction> insnList) {}

        // Ensure global function comes first
        var sortedFunctions = this.functionInsnMap.entrySet().stream()
                .map(entry -> new FunctionInsnPair(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(e -> e.function() != NameAnalysis.GLOBAL_FUNCTION))
                .collect(Collectors.toCollection(ArrayList::new));
        // Optimize methods
        var totalInsnCount = 0;
        for (var entry : sortedFunctions) {
            var insnList = entry.insnList();

            for (var optimizer : this.optimizers)
                optimizer.method(insnList);

            totalInsnCount += insnList.size();
        }

        var insns = new IInstrunction[totalInsnCount + 1];

        // Compute function offsets
        for (var entry : sortedFunctions) {
            var function = entry.function();
            functionOffsets.put(function, i);

            // Copy insns to array
            var insnList = entry.insnList();
            for (var insn : insnList)
                insns[i++] = insn;
        }

        // Resolve jump addresses
        for (int j = 1; j < insns.length; j++) {
            var insn = insns[j];
            if (!(insn instanceof PlaceholderInvokeInsn placeholder))
                continue;

            var function = placeholder.target();
            var offset = functionOffsets.get(function);
            if (offset == null)
                throw new IllegalStateException("Function '%s' is referenced but not compiled".formatted(function.getNameExpr().getValue()));

            insns[j] = new INVOKE_Insn(
                    placeholder.isInstanceFunction(),
                    placeholder.getParamCount(),
                    analyzer.functionLocalsCount(function),
                    placeholder.returnsValue(),
                    offset - 1
            );
        }

        // "Invoke" global function
        insns[0] = new INVOKE_Insn(false, 0, analyzer.functionLocalsCount(NameAnalysis.GLOBAL_FUNCTION), false, 0);

        return new TethProgram(insns, generateStructData());
    }

    private StructData[] generateStructData() {
        var data = new StructData[this.structIds.size()];
        this.structIds.forEach((struct, id) -> {
            data[id] = new StructData(
                    struct.getNameExpr().getValue(),
                    struct.getFields().stream().map(f -> f.getNameExpr().getValue()).toArray(String[]::new)
            );
        });
        return data;
    }

    @SuppressWarnings("UnqualifiedFieldAccess")
    private class BytecodeGeneratorVisitor extends ASTVisitor {

        private static final FunctionDeclaration.ParameterDeclaration SELF_PLACEHOLDER = new FunctionDeclaration.ParameterDeclaration(null, null, new IdentifierExpression(null, "self"));

        private final Analyzer analyzer;
        private final boolean ignoreTopLevelCode;

        private List<IInstrunction> currentFunctionInsn = new ArrayList<>();
        private Map<IVariableDeclaration, Integer> currentFunctionLocals = new IdentityHashMap<>();

        public BytecodeGeneratorVisitor(Analyzer analyzer, boolean ignoreTopLevelCode) {
            this.analyzer = analyzer;
            this.ignoreTopLevelCode = ignoreTopLevelCode;
        }

        @Override
        public void visit(SourceFileUnit unit) {
            if (!this.ignoreTopLevelCode) {
                functionInsnMap.put(NameAnalysis.GLOBAL_FUNCTION, this.currentFunctionInsn);
                super.visit(unit);
                this.currentFunctionInsn.add(new EXIT_Insn());
            } else {
                for (Statement statement : unit.getStatements()) {
                    // TODO: Switch preview disabled
                    if (statement instanceof FunctionDeclaration decl) {
                        decl.accept(this);
                    } else if (statement instanceof StructDeclaration decl) {
                        decl.accept(this);
                    }
                }
            }
        }

        @Override
        public void visit(StructDeclaration declaration) {
            getStructId(declaration);
            super.visit(declaration);
        }

        @Override
        public void visit(FunctionDeclaration declaration) {
            if (declaration.isIntrinsic())
                return;

            var parentFunction = this.currentFunctionInsn;
            var parentLocals = this.currentFunctionLocals;
            this.currentFunctionInsn = new ArrayList<>();
            this.currentFunctionLocals = new HashMap<>();

            // Add hidden 'self' parameter
            if (declaration.isInstanceFunction())
                this.currentFunctionLocals.put(SELF_PLACEHOLDER, 0);
            // Add parameters to locals
            for (var parameter : declaration.getParameters())
                this.currentFunctionLocals.put(parameter, this.currentFunctionLocals.size());

            functionInsnMap.put(declaration, this.currentFunctionInsn);

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
                this.currentFunctionInsn.add(new INVOKE_INTRINSIC_Insn(reference));
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
            this.currentFunctionInsn.add(new CREATE_OBJECT_Insn(getStructId(structDeclaration), structDeclaration.getFields().size()));
        }

        @Override
        public void visit(VariableDeclaration declaration) {
            {
                declaration.getInitializerExpr().accept(this);
            }

            var idx = this.currentFunctionLocals.size();
            this.currentFunctionLocals.put(declaration, idx);
            this.currentFunctionInsn.add(new STORE_LOCAL_Insn(idx));
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
            if (expression.getOperator() == BinaryExpression.Operator.OP_ASSIGN) {
                visitAssignmentExpression(expression);
                return;
            }

            super.visit(expression);

            switch (expression.getOperator()) {
                case OP_ADD -> this.currentFunctionInsn.add(new LD_ADD_Insn());
                case OP_SUBTRACT -> this.currentFunctionInsn.add(new LD_SUB_Insn());
                case OP_MULTIPLY -> this.currentFunctionInsn.add(new LD_MUL_Insn());
                case OP_DIVIDE -> this.currentFunctionInsn.add(new LD_DIV_Insn());
                case OP_LESS -> this.currentFunctionInsn.add(new LD_LESS_Insn());
                case OP_LESS_EQUAL -> this.currentFunctionInsn.add(new LD_LESS_EQUAL_Insn());
                case OP_GREATER -> this.currentFunctionInsn.add(new LD_GREATER_Insn());
                case OP_GREATER_EQUAL -> this.currentFunctionInsn.add(new LD_GREATER_EQUAL_Insn());
                case OP_EQUAL -> this.currentFunctionInsn.add(new LD_EQUAL_Insn());
                case OP_NOT_EQUAL -> {
                    this.currentFunctionInsn.add(new LD_EQUAL_Insn());
                    this.currentFunctionInsn.add(new B_INVERT_Insn());
                }
                case OP_AND -> this.currentFunctionInsn.add(new B_AND_Insn());
                case OP_OR -> this.currentFunctionInsn.add(new B_OR_Insn());
                default -> throw new UnsupportedOperationException("Unsupported operator: " + expression.getOperator());
            }
        }

        public void visitAssignmentExpression(BinaryExpression expression) {
            {
                expression.getRight().accept(this);
            }
            this.currentFunctionInsn.add(new DUP_Insn());

            if (expression.getLeft() instanceof IdentifierExpression identifierExpression) {
                var idx = getLocalIndex(identifierExpression);
                this.currentFunctionInsn.add(new STORE_LOCAL_Insn(idx));
            } else if (expression.getLeft() instanceof MemberAccessExpression memberAccessExpression) {
                memberAccessExpression.getTarget().accept(this);

                var field = (StructDeclaration.FieldDeclaration) this.analyzer.resolvedReference(memberAccessExpression);
                this.currentFunctionInsn.add(new STORE_MEMBER_Insn((short) field.getIndex()));
            } else {
                throw new UnsupportedOperationException("Cannot assign to " + expression.getLeft());
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
            if (stringLiteralExpression.isSingleString()) {
                this.currentFunctionInsn.add(new S_CONST_Insn(stringLiteralExpression.asSingleString()));
                return;
            }

            for (var part : stringLiteralExpression.getParts()) {
                switch (part.getType()) {
                    case STRING -> this.currentFunctionInsn.add(new S_CONST_Insn(part.asString()));
                    case EXPRESSION -> part.asExpression().accept(this);
                }
            }

            var concatFunction = (FunctionDeclaration) Prelude.getStructForTypeName("string").getMember("concat");
            for (int i = 0; i < stringLiteralExpression.getParts().size() - 1; i++)
                this.currentFunctionInsn.add(new INVOKE_INTRINSIC_Insn(concatFunction));
        }

        @Override
        public void visit(IdentifierExpression identifierExpression) {
            var reference = this.analyzer.resolvedReference(identifierExpression);
            if (!(reference instanceof IVariableDeclaration))
                return; //TODO: Looks like a bad idea, seems to be added because of function invocations

            var varIndex = getLocalIndex(identifierExpression);
            if (varIndex == null)
                throw new IllegalStateException("Variable not found " + identifierExpression + ": [" + identifierExpression.getSpan().getStartLine() + ":" + identifierExpression.getSpan().getStartColumn() + "]");

            this.currentFunctionInsn.add(new LOAD_LOCAL_Insn(varIndex));
        }

        @Override
        public void visit(ListLiteralExpression listLiteralExpression) {
            this.currentFunctionInsn.add(new CREATE_LIST_Insn());
            for (int i = 0; i < listLiteralExpression.getInitializers().size(); i++)
                this.currentFunctionInsn.add(new DUP_Insn());

            listLiteralExpression.getInitializers().forEach(e -> {
                e.accept(this);
                this.currentFunctionInsn.add(new INVOKE_INTRINSIC_Insn((FunctionDeclaration) Prelude.LIST_STRUCT_DECLARATION.getMember("add")));
            });
        }

        private Integer getLocalIndex(IdentifierExpression identifierExpression) {
            // This only returns null for a reference to 'self' inside an instance function, because the instance of
            // the self parameter function will be different from SELF_PLACEHOLDER. Therefore, the default is 0,
            // which is the index if the 'self' parameter. All of this obviously assumes that the analyzer ran
            // successfully.
            return this.currentFunctionLocals.getOrDefault((IVariableDeclaration) this.analyzer.resolvedReference(identifierExpression), 0);
        }

        private int getStructId(StructDeclaration declaration) {
            return structIds.computeIfAbsent(declaration, k -> structIds.size());
        }
    }
}
