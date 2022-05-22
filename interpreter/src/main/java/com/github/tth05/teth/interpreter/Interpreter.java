package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.interpreter.environment.Environment;
import com.github.tth05.teth.interpreter.values.*;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.Arrays;
import java.util.stream.Stream;

public class Interpreter {

    private final Environment environment = new Environment();

    public void execute(SourceFileUnit ast) {
        try {
            executeStatementList(ast.getStatements());
        } catch (InterpreterException e) {
            System.err.println(e.getMessage());
        }
    }

    private void executeStatementList(StatementList statements) {
        for (Statement s : statements) {
            switch (s) {
                case FunctionDeclaration functionDeclaration ->
                        this.environment.currentScope().setLocalVariable(functionDeclaration.getName(), new FunctionDeclarationValue(functionDeclaration));
                case VariableDeclaration localVariableDeclaration ->
                        this.environment.currentScope().setLocalVariable(localVariableDeclaration.getName(), evaluateExpression(localVariableDeclaration.getExpression()).copy());
                case Expression expression -> evaluateExpression(expression);
                case Statement statement -> executeStatement(statement);
            }
        }
    }

    public IValue evaluateExpression(Expression expression) {
        return switch (expression) {
            case FunctionInvocationExpression functionInvocationExpression ->
                    evaluateFunctionInvocationExpression(functionInvocationExpression);
            case MemberAccessExpression memberAccessExpression -> {
                IValue target = evaluateExpression(memberAccessExpression.getTarget());
                var memberName = memberAccessExpression.getMemberName();

                yield extractMember(target, memberName);
            }
            case VariableAssignmentExpression variableAssignmentExpression -> {
                if (!this.environment.currentScope().hasLocalVariable(variableAssignmentExpression.getTarget()))
                    throw new InterpreterException("Variable " + variableAssignmentExpression.getTarget() + " is not defined");

                IValue value = evaluateExpression(variableAssignmentExpression.getExpr());
                this.environment.currentScope().setLocalVariable(variableAssignmentExpression.getTarget(), value);
                yield value.copy();
            }
            case LongLiteralExpression longLiteralExpression -> new LongValue(longLiteralExpression.getValue());
            case DoubleLiteralExpression doubleLiteralExpression -> new DoubleValue(doubleLiteralExpression.getValue());
            case StringLiteralExpression stringLiteralExpression -> new StringValue(stringLiteralExpression.getValue());
            case BooleanLiteralExpression booleanLiteralExpression ->
                    new BooleanValue(booleanLiteralExpression.getValue());
            case IdentifierExpression identifierExpression -> {
                var value = this.environment.lookupIdentifier(identifierExpression.getValue());
                if (value != null)
                    yield value;

                //TODO: Resolve into variable, etc.
                throw new InterpreterException("Unresolved identifier expression: " + identifierExpression);
            }
            case BinaryExpression binaryExpression -> {
                IValue left = evaluateExpression(binaryExpression.getLeft());
                IValue right = evaluateExpression(binaryExpression.getRight());

                if (!(left instanceof IBinaryOperatorInvokable invokable))
                    throw new InterpreterException("Left operand is not invokable: " + left);

                yield invokable.invokeBinaryOperator(binaryExpression.getOperator(), right);
            }
            case UnaryExpression unaryExpression -> {
                IValue operand = evaluateExpression(unaryExpression.getExpression());

                if (!(operand instanceof IUnaryOperatorInvokable invokable))
                    throw new InterpreterException("Operand is not invokable: " + operand);

                yield invokable.invokeUnaryOperator(unaryExpression.getOperator());
            }
            default -> throw new InterpreterException("Unsupported expression: " + expression);
        };
    }

    public void executeStatement(Statement statement) {
        switch (statement) {
            case BlockStatement blockStatement -> executeStatementList(blockStatement.getStatements());
            case IfStatement ifStatement -> {
                IValue condition = evaluateExpression(ifStatement.getCondition());
                if (!(condition instanceof BooleanValue booleanValue))
                    throw new InterpreterException("Condition did not evaluate to boolean: " + condition);

                if (booleanValue.getValue())
                    executeStatement(ifStatement.getBody());
                else if (ifStatement.getElseStatement() != null)
                    executeStatement(ifStatement.getElseStatement());
            }
            default -> throw new InterpreterException("Unsupported statement: " + statement);
        }
    }

    public IValue callFunction(FunctionDeclaration functionDeclaration, IValue[] arguments) {
        //TODO varargs
        this.environment.enterScope();
        var parameters = functionDeclaration.getParameters();
        for (int i = 0; i < parameters.size(); i++)
            this.environment.currentScope().setLocalVariable(parameters.get(i).name(), arguments[i]);
        //TODO: return value
        executeStatement(functionDeclaration.getBody());
        this.environment.exitScope();
        return null;
    }

    private IValue evaluateFunctionInvocationExpression(FunctionInvocationExpression expr) {
        var parameters = expr.getParameters().stream().map(p -> evaluateExpression(p).copy()).toArray(IValue[]::new);
        var targetExpr = expr.getTarget();

        IValue target;
        if (targetExpr instanceof MemberAccessExpression memberAccessExpression) {
            var self = evaluateExpression(memberAccessExpression.getTarget());
            target = extractMember(self, memberAccessExpression.getMemberName());
            // :^)
            parameters = Stream.concat(Stream.of(self), Stream.of(parameters)).toArray(IValue[]::new);
        } else {
            target = evaluateExpression(targetExpr);
        }

        if (!(target instanceof IFunction invokable))
            throw new InterpreterException("Target is not a function: " + target);
        if (!invokable.isApplicable(parameters))
            throw new InterpreterException("Target " + target + " is not applicable to: " + Arrays.toString(parameters));

        return invokable.invoke(this, parameters);
    }

    private IValue extractMember(IValue target, String memberName) {
        if (!(target instanceof IHasMembers hasMembers))
            throw new InterpreterException("Cannot access member of: " + target);

        var member = hasMembers.getMember(this.environment, memberName);
        if (member == null)
            throw new InterpreterException("Member " + memberName + " not found in " + target);

        return member;
    }
}
