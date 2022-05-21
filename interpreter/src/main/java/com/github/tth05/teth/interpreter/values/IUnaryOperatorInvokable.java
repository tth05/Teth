package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.values.IValue;
import com.github.tth05.teth.lang.parser.ast.UnaryExpression;

public interface IUnaryOperatorInvokable {

    IValue invokeUnaryOperator(UnaryExpression.Operator operator);
}
