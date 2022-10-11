package com.github.tth05.teth.bytecodeInterpreter;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface UncheckedBiConsumer<T, U> extends BiConsumer<T, U> {

    @Override
    default void accept(T t, U u) {
        try {
            uncheckedAccept(t, u);
        } catch (Throwable th) {
            rethrow(th);
        }
    }

    void uncheckedAccept(T t, U u) throws Throwable;

    private static <X extends Throwable> void rethrow(Throwable t) throws X {
        throw (X) t;
    }
}
