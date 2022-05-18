package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.ast.*;

public class Interpreter {

    public void execute(SourceFileUnit ast) {
        try {
            executeStatementList(ast.getStatements());
        } catch (InterpreterException e) {
            System.err.println(e.getMessage());
        }
    }

    private void executeStatementList(StatementList statements) {
        for (Statement statement : statements) {
            switch (statement) {
                case Expression expression -> {
                    evaluateExpression(expression);
                }
                default -> throw new InterpreterException("Unsupported statement: " + statement);
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

                return invokable.invokeOperator(binaryExpression.getOperator(), right);
            }
            default -> throw new InterpreterException("Unsupported expression: " + expression);
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
