package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.lang.parser.Type;

public interface IValue {

    String getDebugString();

    Type getType();
}
