package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.interpreter.environment.Environment;
import com.github.tth05.teth.interpreter.values.*;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Interpreter {

    private final Environment environment = new Environment();

    public void execute(SourceFileUnit ast) throws InterpreterException {
        executeStatementList(ast.getStatements());
    }

    private IValue executeStatementList(StatementList statements) {
        for (Statement s : statements) {
            switch (s) {
                case Expression expression -> evaluateExpression(expression);
                case Statement statement -> {
                    var functionReturnValue = executeStatement(statement);
                    if (functionReturnValue != null)
                        return functionReturnValue;
                }
            }
        }

        return null;
    }

    public IValue evaluateExpression(Expression expression) throws InterpreterException {
        return switch (expression) {
            case FunctionInvocationExpression functionInvocationExpression ->
                    evaluateFunctionInvocationExpression(functionInvocationExpression);
            case MemberAccessExpression memberAccessExpression -> {
                IValue target = evaluateExpression(memberAccessExpression.getTarget());
                var memberName = memberAccessExpression.getMemberNameExpr();

                yield extractMember(target, memberName);
            }
            case VariableAssignmentExpression variableAssignmentExpression -> {
                var nameExpr = variableAssignmentExpression.getTargetNameExpr();
                if (!this.environment.currentScope().hasLocalVariable(nameExpr.getValue()))
                    throw new InterpreterException(nameExpr.getSpan(), "Variable " + nameExpr.getValue() + " is not defined");

                IValue value = evaluateExpression(variableAssignmentExpression.getExpr());
                this.environment.currentScope().setLocalVariable(nameExpr.getValue(), value);
                yield value;
            }
            case ListLiteralExpression listLiteralExpression -> {
                var initializers = listLiteralExpression.getInitializers();
                var values = initializers.stream().map(this::evaluateExpression).collect(Collectors.toList());
                if (values.isEmpty())
                    throw new InterpreterException(listLiteralExpression.getSpan(), "Empty list literals are unsupported until type inference is implemented");

                yield new ListValue(values.get(0).getType(), values);
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

                throw new InterpreterException(identifierExpression.getSpan(), "Unable to resolve identifier to a value");
            }
            case BinaryExpression binaryExpression -> {
                IValue left = evaluateExpression(binaryExpression.getLeft());
                IValue right = evaluateExpression(binaryExpression.getRight());

                if (!(left instanceof IBinaryOperatorInvokable invokable))
                    throw new InterpreterException(binaryExpression.getLeft().getSpan(), "Left operand does not support binary operations: " + left);

                yield invokable.invokeBinaryOperator(binaryExpression.getOperator(), right);
            }
            case UnaryExpression unaryExpression -> {
                IValue operand = evaluateExpression(unaryExpression.getExpression());

                if (!(operand instanceof IUnaryOperatorInvokable invokable))
                    throw new InterpreterException(unaryExpression.getSpan(), "Operand does not support unary operations: " + operand);

                yield invokable.invokeUnaryOperator(unaryExpression.getOperator());
            }
            default -> throw new InterpreterException(expression.getSpan(), "Unsupported expression");
        };
    }

    public IValue executeStatement(Statement statement) {
        switch (statement) {
            case FunctionDeclaration functionDeclaration ->
                    this.environment.currentScope().setLocalVariable(functionDeclaration.getNameExpr().getValue(), new FunctionDeclarationValue(functionDeclaration));
            case VariableDeclaration localVariableDeclaration ->
                    this.environment.currentScope().setLocalVariable(localVariableDeclaration.getNameExpr().getValue(), evaluateExpression(localVariableDeclaration.getExpression()));
            case BlockStatement blockStatement -> {
                this.environment.enterSubScope();
                var returnValue = executeStatementList(blockStatement.getStatements());
                this.environment.exitScope();
                return returnValue;
            }
            case IfStatement ifStatement -> {
                IValue condition = evaluateExpression(ifStatement.getCondition());
                if (!(condition instanceof BooleanValue booleanValue))
                    throw new InterpreterException(ifStatement.getCondition().getSpan(), "Condition did not evaluate to boolean: " + condition);

                if (booleanValue.getValue())
                    return executeStatement(ifStatement.getBody());
                else if (ifStatement.getElseStatement() != null)
                    return executeStatement(ifStatement.getElseStatement());
            }
            case ReturnStatement returnStatement -> {
                return evaluateExpression(returnStatement.getValueExpr());
            }
            default -> throw new InterpreterException(statement.getSpan(), "Unsupported statement");
        }

        return null;
    }

    public IValue callFunction(FunctionDeclaration functionDeclaration, IValue[] arguments) {
        //TODO varargs
        this.environment.enterScope();
        var parameters = functionDeclaration.getParameters();
        for (int i = 0; i < parameters.size(); i++)
            this.environment.currentScope().setLocalVariable(parameters.get(i).name().getValue(), arguments[i]);
        var returnValue = executeStatement(functionDeclaration.getBody());
        this.environment.exitScope();
        return returnValue;
    }

    private IValue evaluateFunctionInvocationExpression(FunctionInvocationExpression expr) {
        var parameters = expr.getParameters().stream().map(this::evaluateExpression).toArray(IValue[]::new);
        var targetExpr = expr.getTarget();

        IValue target;
        if (targetExpr instanceof MemberAccessExpression memberAccessExpression) {
            var self = evaluateExpression(memberAccessExpression.getTarget());
            target = extractMember(self, memberAccessExpression.getMemberNameExpr());
            // :^)
            parameters = Stream.concat(Stream.of(self), Stream.of(parameters)).toArray(IValue[]::new);
        } else {
            target = evaluateExpression(targetExpr);
        }

        if (!(target instanceof IFunction invokable))
            throw new InterpreterException(targetExpr.getSpan(), "Target did not evaluate to function: '" + target.getDebugString() + "'");
        if (!invokable.isApplicable(parameters))
            throw new InterpreterException(targetExpr.getSpan(), "Target '" + target.getDebugString() + "' is not applicable to: " + Arrays.toString(parameters));

        return invokable.invoke(this, parameters);
    }

    private IValue extractMember(IValue target, IdentifierExpression memberName) {
        var targetName = memberName.getValue();
        if (!(target instanceof IHasMembers hasMembers) || !hasMembers.hasMember(targetName))
            throw new InterpreterException(memberName.getSpan(), "Member with name '" + memberName + "' not found on: " + target);

        var member = hasMembers.getMember(this.environment, targetName);
        if (member == null)
            throw new InterpreterException(memberName.getSpan(), "Member with name '" + memberName + "' not found on: " + target);

        return member;
    }
}
