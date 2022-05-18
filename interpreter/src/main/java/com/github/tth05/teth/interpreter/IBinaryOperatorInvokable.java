package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.ast.BinaryExpression;

public interface IBinaryOperatorInvokable {


    IValue invokeOperator(BinaryExpression.Operator operator, IValue arg);
}
