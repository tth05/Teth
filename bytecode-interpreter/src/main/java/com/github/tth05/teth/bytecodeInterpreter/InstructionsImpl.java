package com.github.tth05.teth.bytecodeInterpreter;

import com.github.tth05.teth.bytecode.compiler.OpCodes;
import com.github.tth05.teth.bytecode.decoder.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class InstructionsImpl {

    private static final Object[] ARGS_ARRAY = new Object[32];
    private static final Map<String, BiConsumer<Interpreter, Object[]>> INTRINSICS = new HashMap<>();
    static {
        INTRINSICS.put("print", (interpreter, args) -> {
            var list = (List) args[0];
            var first = true;
            for (Object o : list) {
                System.out.print((first ? "" : " ") + o);
                first = false;
            }

            System.out.println();
        });
        INTRINSICS.put("string.concat", (interpreter, args) -> {
            var left = ((String) args[0]);
            var right = ((String) args[1]);

            interpreter.push(left + right);
        });
        INTRINSICS.put("long.toString", (interpreter, args) -> {
            var val = (Long) args[0];

            interpreter.push(val.toString());
        });
        INTRINSICS.put("list.add", (interpreter, args) -> {
            var list = (List) args[0];
            var el = args[1];

            list.add(el);
        });
        INTRINSICS.put("list.get", (interpreter, args) -> {
            var list = (List) args[0];
            var el = (long) args[1];

            interpreter.push(list.get((int) el));
        });
    }

    public static void run(Interpreter interpreter, int code, IInstrunction insn) {
        switch (code) {
            case OpCodes.LD_ADD -> {
                var left = interpreter.pop();
                var right = interpreter.pop();
                boolean useDouble = validateLongDoubleOperands(left, right);
                var leftNum = (Number) left;
                var rightNum = (Number) right;

                if (useDouble)
                    interpreter.push(leftNum.doubleValue() + rightNum.doubleValue());
                else
                    interpreter.push(leftNum.longValue() + rightNum.longValue());
            }
            case OpCodes.L_CONST -> {
                var value = ((L_CONST_Insn) insn).getValue();
                interpreter.push(value);
            }
            case OpCodes.S_CONST -> {
                var value = ((S_CONST_Insn) insn).getValue();
                interpreter.push(value);
            }
            case OpCodes.LOAD_LOCAL -> {
                var localIndex = ((LOAD_LOCAL_Insn) insn).getLocalIndex();
                interpreter.push(interpreter.local(localIndex));
            }
            case OpCodes.CREATE_LIST -> {
                //noinspection rawtypes
                interpreter.push(new ArrayList());
            }
            case OpCodes.DUP -> {
                interpreter.push(interpreter.peek());
            }
            case OpCodes.JUMP_IF_TRUE -> {
                var relativeJumpOffset = ((JUMP_IF_TRUE_Insn) insn).getRelativeJumpOffset();
                var condition = interpreter.pop();
                if (!(condition instanceof Boolean b))
                    throw new IllegalArgumentException("JUMP_IF_TRUE: Top of stack is not a boolean");
                if (!b)
                    return;
                interpreter.setProgramCounter(interpreter.getProgramCounter() + relativeJumpOffset);
            }
            case OpCodes.INVOKE -> {
                var invokeInsn = (INVOKE_Insn) insn;
                var paramCount = invokeInsn.getParamCount();
                if (invokeInsn.isInstanceFunction())
                    throw new UnsupportedOperationException();

                interpreter.initLocalsFromStack(paramCount, invokeInsn.getLocalsCount());
                interpreter.saveReturnAddress();
                interpreter.setProgramCounter(invokeInsn.getAbsoluteJumpAddress());
                interpreter.createStackBoundary();
            }
            case OpCodes.INVOKE_INTRINSIC -> {
                //TODO: Pre-process instructions to convert this into a resolved INVOKE_INTRINSIC instruction
                var name = ((INVOKE_INTRINSIC_Insn) insn).getIntrinsicName();
                var intrinsic = INTRINSICS.get(name);
                //TODO: Get intrinsic function instance from analyzer or lang core?
                var argCount = name.equals("print") || name.equals("long.toString") ? 1 : 2;
                intrinsic.accept(interpreter, collectFunctionArguments(interpreter, argCount));
            }
            case OpCodes.RETURN -> {
                var shouldReturnValue = ((RETURN_Insn) insn).shouldReturnValue();
                var returnValue = shouldReturnValue ? interpreter.pop() : null;
                while (!interpreter.stackAtBoundary())
                    interpreter.pop();
                if (shouldReturnValue)
                    interpreter.replaceStackBoundary(returnValue);
                else
                    interpreter.popStackBoundary();
                interpreter.jumpToReturnAddress();
            }
            case OpCodes.LD_LESS_EQUAL -> {
                var right = interpreter.pop();
                var left = interpreter.pop();
                boolean useDouble = validateLongDoubleOperands(left, right);
                var leftNum = (Number) left;
                var rightNum = (Number) right;

                var result = useDouble ? leftNum.doubleValue() <= rightNum.doubleValue() : leftNum.longValue() <= rightNum.longValue();
                interpreter.push(result);
            }
            case OpCodes.EXIT -> interpreter.exit();
            default -> throw new IllegalArgumentException("Unsupported opcode: " + code);
        }
    }


    private static Object[] collectFunctionArguments(Interpreter interpreter, int argCount) {
        //TODO: Should this validate arg types?
        for (int i = argCount - 1; i >= 0; i--)
            ARGS_ARRAY[i] = interpreter.pop();

        return ARGS_ARRAY;
    }

    private static boolean validateLongDoubleOperands(Object left, Object right) {
        if (!(left instanceof Number) || !(right instanceof Number))
            throw new IllegalStateException("LD_ADD: Top of stack did not contain two numbers");

        return left instanceof Double || right instanceof Double;
    }
}
