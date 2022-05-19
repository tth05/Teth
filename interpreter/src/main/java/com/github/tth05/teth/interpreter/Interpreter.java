package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.*;

public class Interpreter {

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
                case Expression expression -> evaluateExpression(expression);
                case Statement statement -> executeStatement(statement);
            }
        }
    }

    public IValue evaluateExpression(Expression expression) {
        switch (expression) {
            case FunctionInvocationExpression functionInvocationExpression -> {
                return evaluateFunctionInvocationExpression(functionInvocationExpression);
            }
            case LongLiteralExpression longLiteralExpression -> {
                return new NumberValue(longLiteralExpression.getValue());
            }
            case BooleanLiteralExpression booleanLiteralExpression -> {
                return new BooleanValue(booleanLiteralExpression.getValue());
            }
            case IdentifierExpression identifierExpression -> {
                if (IntrinsicFunctionValue.isIntrinsicFunction(identifierExpression.getValue())) {
                    return new IntrinsicFunctionValue(identifierExpression.getValue());
                }

                //TODO: Resolve into variable, etc.
                throw new InterpreterException("Unresolved identifier expression: " + identifierExpression);
            }
            case BinaryExpression binaryExpression -> {
                IValue left = evaluateExpression(binaryExpression.getLeft());
                IValue right = evaluateExpression(binaryExpression.getRight());

                if (!(left instanceof IBinaryOperatorInvokable invokable))
                    throw new InterpreterException("Left operand is not invokable: " + left);

                return invokable.invokeBinaryOperator(binaryExpression.getOperator(), right);
            }
            case UnaryExpression unaryExpression -> {
                IValue operand = evaluateExpression(unaryExpression.getExpression());

                if (!(operand instanceof IUnaryOperatorInvokable invokable))
                    throw new InterpreterException("Operand is not invokable: " + operand);

                return invokable.invokeUnaryOperator(unaryExpression.getOperator());
            }
            default -> throw new InterpreterException("Unsupported expression: " + expression);
        }
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

    private IValue evaluateFunctionInvocationExpression(FunctionInvocationExpression expr) {
        IValue target = evaluateExpression(expr.getTarget());
        var parameters = expr.getParameters().stream().map(this::evaluateExpression).toArray(IValue[]::new);

        switch (target) {
            case IFunction targetFunction -> targetFunction.invoke(parameters);
            default -> throw new InterpreterException("Unsupported target: " + target);
        }
        return null;
    }
}
