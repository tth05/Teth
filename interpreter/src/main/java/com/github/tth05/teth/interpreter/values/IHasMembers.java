package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.environment.Environment;

public interface IHasMembers {

    IValue getMember(Environment environment, String name);

    boolean hasMember(String name);
}
