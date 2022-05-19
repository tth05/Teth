package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.parser.ast.BinaryExpression;

public interface IBinaryOperatorInvokable {


    IValue invokeBinaryOperator(BinaryExpression.Operator operator, IValue arg);
}
