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
        INTRINSICS.put("list.set", (interpreter, args) -> {
            var list = (List) args[0];
            var idx = (long) args[1];
            var el = args[2];

            list.set((int) idx, el);
        });
    }

    public static void run(Interpreter interpreter, int code, IInstrunction insn) {
        try {
            switch (code) {
                case OpCodes.B_INVERT -> {
                    var value = (Boolean) interpreter.pop();
                    interpreter.push(!value);
                }
                case OpCodes.B_CONST -> {
                    var value = ((B_CONST_Insn) insn).getValue();
                    interpreter.push(value);
                }
                case OpCodes.B_AND -> {
                    var right = (Boolean) interpreter.pop();
                    var left = (Boolean) interpreter.pop();
                    interpreter.push(left && right);
                }
                case OpCodes.B_OR -> {
                    var right = (Boolean) interpreter.pop();
                    var left = (Boolean) interpreter.pop();
                    interpreter.push(left || right);
                }
                case OpCodes.LD_NEGATE -> {
                    var value = (Number) interpreter.pop();
                    if (value instanceof Long)
                        value = -value.longValue();
                    else
                        value = -value.doubleValue();

                    interpreter.push(value);
                }
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
                case OpCodes.LD_SUB -> {
                    var left = interpreter.pop();
                    var right = interpreter.pop();
                    boolean useDouble = validateLongDoubleOperands(left, right);
                    var leftNum = (Number) right;
                    var rightNum = (Number) left;

                    if (useDouble)
                        interpreter.push(leftNum.doubleValue() - rightNum.doubleValue());
                    else
                        interpreter.push(leftNum.longValue() - rightNum.longValue());
                }
                case OpCodes.LD_MUL -> {
                    var left = interpreter.pop();
                    var right = interpreter.pop();
                    boolean useDouble = validateLongDoubleOperands(left, right);
                    var leftNum = (Number) left;
                    var rightNum = (Number) right;

                    if (useDouble)
                        interpreter.push(leftNum.doubleValue() * rightNum.doubleValue());
                    else
                        interpreter.push(leftNum.longValue() * rightNum.longValue());
                }
                case OpCodes.LD_DIV -> {
                    var left = interpreter.pop();
                    var right = interpreter.pop();
                    boolean useDouble = validateLongDoubleOperands(left, right);
                    var leftNum = (Number) left;
                    var rightNum = (Number) right;

                    if (useDouble)
                        interpreter.push(leftNum.doubleValue() / rightNum.doubleValue());
                    else
                        interpreter.push(leftNum.longValue() / rightNum.longValue());
                }
                case OpCodes.L_CONST -> {
                    var value = ((L_CONST_Insn) insn).getValue();
                    interpreter.push(value);
                }
                case OpCodes.D_CONST -> {
                    var value = ((D_CONST_Insn) insn).getValue();
                    interpreter.push(value);
                }
                case OpCodes.S_CONST -> {
                    var value = ((S_CONST_Insn) insn).getValue();
                    interpreter.push(value);
                }
                case OpCodes.LOAD_LOCAL -> {
                    var localIndex = ((LOAD_LOCAL_Insn) insn).getLocalIndex();
                    interpreter.push(interpreter.loadLocal(localIndex));
                }
                case OpCodes.STORE_LOCAL -> {
                    var localIndex = ((STORE_LOCAL_Insn) insn).getLocalIndex();
                    interpreter.storeLocal(localIndex, interpreter.pop());
                }
                case OpCodes.CREATE_LIST -> {
                    //noinspection rawtypes
                    interpreter.push(new ArrayList());
                }
                case OpCodes.DUP -> {
                    interpreter.push(interpreter.peek());
                }
                case OpCodes.JUMP -> {
                    var relativeJumpOffset = ((JUMP_Insn) insn).getRelativeJumpOffset();
                    interpreter.setProgramCounter(interpreter.getProgramCounter() + relativeJumpOffset);
                }
                case OpCodes.JUMP_IF_FALSE -> {
                    var relativeJumpOffset = ((JUMP_IF_FALSE_Insn) insn).getRelativeJumpOffset();
                    var condition = interpreter.pop();
                    if (!(condition instanceof Boolean b))
                        throw new IllegalArgumentException("JUMP_IF_FALSE: Top of stack is not a boolean");
                    if (b)
                        return;
                    interpreter.setProgramCounter(interpreter.getProgramCounter() + relativeJumpOffset);
                }
                case OpCodes.INVOKE -> {
                    var invokeInsn = (INVOKE_Insn) insn;
                    var paramCount = invokeInsn.getParamCount();

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
                    var argCount = name.equals("print") || name.equals("long.toString") ? 1 : name.equals("list.set") ? 3 : 2;
                    intrinsic.accept(interpreter, collectFunctionArguments(interpreter, argCount));
                }
                case OpCodes.CREATE_OBJECT -> {
                    var createInsn = (CREATE_OBJECT_Insn) insn;
                    var fields = new Object[createInsn.getFieldCount()];
                    for (int i = 0; i < fields.length; i++)
                        fields[i] = interpreter.pop();
                    interpreter.push(new ObjectValue(fields));
                }
                case OpCodes.LOAD_MEMBER -> {
                    var loadMemberInsn = (LOAD_MEMBER_Insn) insn;
                    var object = ((ObjectValue) interpreter.pop());
                    var fieldIndex = loadMemberInsn.getFieldIndex();
                    interpreter.push(object.getField(fieldIndex));
                }
                case OpCodes.STORE_MEMBER -> {
                    var storeMemberInsn = (STORE_MEMBER_Insn) insn;
                    var object = ((ObjectValue) interpreter.pop());
                    var value = interpreter.pop();
                    var fieldIndex = storeMemberInsn.getFieldIndex();
                    object.setField(fieldIndex, value);
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
                case OpCodes.LD_LESS_EQUAL, OpCodes.LD_LESS -> {
                    var right = interpreter.pop();
                    var left = interpreter.pop();
                    boolean useDouble = validateLongDoubleOperands(left, right);
                    var leftNum = (Number) left;
                    var rightNum = (Number) right;

                    boolean result;
                    if (code == OpCodes.LD_LESS)
                        result = useDouble ? leftNum.doubleValue() < rightNum.doubleValue() : leftNum.longValue() < rightNum.longValue();
                    else
                        result = useDouble ? leftNum.doubleValue() <= rightNum.doubleValue() : leftNum.longValue() <= rightNum.longValue();

                    interpreter.push(result);
                }
                case OpCodes.LD_EQUAL -> {
                    var right = interpreter.pop();
                    var left = interpreter.pop();
                    boolean useDouble = validateLongDoubleOperands(left, right);
                    var leftNum = (Number) left;
                    var rightNum = (Number) right;

                    boolean result = useDouble ? leftNum.doubleValue() == rightNum.doubleValue() : leftNum.longValue() == rightNum.longValue();
                    interpreter.push(result);
                }
                case OpCodes.EXIT -> interpreter.exit();
                default -> throw new IllegalArgumentException("Unsupported opcode: " + code);
            }
        } catch (Exception e) {
            System.err.printf("Error while execution instruction %s at address %d%n", insn.getDebugString(), interpreter.getProgramCounter());
            throw e;
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
