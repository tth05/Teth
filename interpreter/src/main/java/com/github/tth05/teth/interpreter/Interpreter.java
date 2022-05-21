package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.interpreter.environment.Environment;
import com.github.tth05.teth.interpreter.values.*;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.*;

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
                return new LongValue(longLiteralExpression.getValue());
            }
            case BooleanLiteralExpression booleanLiteralExpression -> {
                return new BooleanValue(booleanLiteralExpression.getValue());
            }
            case IdentifierExpression identifierExpression -> {
                var local = this.environment.currentScope().getLocalVariable(identifierExpression.getValue());
                if (local != null)
                    return local;

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
        IFunction target = evaluateFunctionInvocationTargetExpression(expr.getTarget(), parameters);

        return target.invoke(this, parameters);
    }

    private IFunction evaluateFunctionInvocationTargetExpression(Expression target, IValue[] parameters) {
        switch (target) {
            case IdentifierExpression identifierExpression -> {
                var local = this.environment.lookupFunction(identifierExpression.getValue(), parameters);
                if (local != null)
                    return local;

                throw new InterpreterException("Unresolved identifier: " + identifierExpression.getValue());
            }
            case FunctionInvocationExpression functionInvocationExpression -> {
                var value = evaluateFunctionInvocationExpression(functionInvocationExpression);
                if (!(value instanceof IFunction f))
                    throw new InterpreterException("Function invocation did not evaluate to a function: " + value);
                return f;
            }
            default -> throw new InterpreterException("Function invocation target is not a function: " + target);
        }
    }
}
