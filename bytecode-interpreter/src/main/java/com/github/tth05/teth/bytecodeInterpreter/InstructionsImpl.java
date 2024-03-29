package com.github.tth05.teth.bytecodeInterpreter;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.bytecode.op.*;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.span.Span;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InstructionsImpl {

    private static final byte[] NEW_LINE_BYTES = System.lineSeparator().getBytes(StandardCharsets.UTF_8);

    private static final Object[] ARGS_ARRAY = new Object[32];
    private static final Map<FunctionDeclaration, UncheckedBiConsumer<Interpreter, Object[]>> INTRINSICS = new HashMap<>();
    static {
        INTRINSICS.put(Prelude.getGlobalFunction(Span.fromString("print")), (interpreter, args) -> {
            var arg = args[0];
            interpreter.getOutStream().write(intrinsicToString(interpreter, arg).getBytes(StandardCharsets.UTF_8));
            interpreter.getOutStream().write(NEW_LINE_BYTES);
        });
        INTRINSICS.put(Prelude.getGlobalFunction(Span.fromString("stringify")), (interpreter, args) -> {
            interpreter.push(intrinsicToString(interpreter, args[0]));
        });
        INTRINSICS.put(Prelude.getGlobalFunction(Span.fromString("nanoTime")), (interpreter, args) -> {
            interpreter.push(System.nanoTime());
        });
        INTRINSICS.put(prelude("string", "concat"), (interpreter, args) -> {
            var left = ((String) args[0]);
            var right = ((String) args[1]);

            interpreter.push(left + right);
        });
        INTRINSICS.put(prelude("long", "toString"), (interpreter, args) -> {
            var val = (Long) args[0];

            interpreter.push(val.toString());
        });
        INTRINSICS.put(prelude("double", "toLong"), (interpreter, args) -> {
            var val = (Double) args[0];

            interpreter.push(val.longValue());
        });
        INTRINSICS.put(prelude("list", "size"), (interpreter, args) -> {
            var list = (List<Object>) args[0];

            interpreter.push((long) list.size());
        });
        INTRINSICS.put(prelude("list", "add"), (interpreter, args) -> {
            var list = (List<Object>) args[0];
            var el = args[1];

            list.add(el);
        });
        INTRINSICS.put(prelude("list", "get"), (interpreter, args) -> {
            var list = (List<Object>) args[0];
            var el = (long) args[1];

            interpreter.push(list.get((int) el));
        });
        INTRINSICS.put(prelude("list", "set"), (interpreter, args) -> {
            var list = (List<Object>) args[0];
            var idx = (long) args[1];
            var el = args[2];

            list.set((int) idx, el);
        });
    }

    public static void run(Interpreter interpreter, byte code, IInstrunction insn) {
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
                case OpCodes.L_NEGATE -> interpreter.push(-((Long) interpreter.pop()));
                case OpCodes.D_NEGATE -> interpreter.push(-((Double) interpreter.pop()));
                case OpCodes.L_ADD -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    interpreter.push(left + right);
                }
                case OpCodes.D_ADD -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    interpreter.push(left + right);
                }
                case OpCodes.L_SUB -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    interpreter.push(left - right);
                }
                case OpCodes.D_SUB -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    interpreter.push(left - right);
                }
                case OpCodes.L_MUL -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    interpreter.push(left * right);
                }
                case OpCodes.D_MUL -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    interpreter.push(left * right);
                }
                case OpCodes.L_DIV -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    interpreter.push(left / right);
                }
                case OpCodes.D_DIV -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    interpreter.push(left / right);
                }
                case OpCodes.L_POW -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    interpreter.push((long) Math.pow(left, right));
                }
                case OpCodes.D_POW -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    interpreter.push(Math.pow(left, right));
                }
                case OpCodes.L_CONST -> {
                    var value = ((L_CONST_Insn) insn).getValue();
                    interpreter.push(value);
                }
                case OpCodes.D_CONST -> {
                    var value = ((D_CONST_Insn) insn).getValue();
                    interpreter.push(value);
                }
                case OpCodes.NULL_CONST -> {
                    interpreter.push(ObjectValue.NULL);
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
                case OpCodes.DUP -> interpreter.push(interpreter.peek());
                case OpCodes.POP -> interpreter.pop();
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

                    interpreter.prepareFunctionEnter(invokeInsn.isInstanceFunction(), paramCount, invokeInsn.getLocalsCount());
                    interpreter.saveReturnAddress();
                    interpreter.setProgramCounter(invokeInsn.getAbsoluteJumpAddress());
                    interpreter.createStackBoundary();
                }
                case OpCodes.INVOKE_INTRINSIC -> {
                    //TODO: Pre-process instructions to convert this into a resolved INVOKE_INTRINSIC instruction
                    var function = ((INVOKE_INTRINSIC_Insn) insn).getFunctionDeclaration();
                    var intrinsic = INTRINSICS.get(function);
                    var argCount = function.getParameters().size() + (function.isInstanceFunction() ? 1 : 0);
                    intrinsic.accept(interpreter, collectFunctionArguments(interpreter, function.isInstanceFunction(), argCount));
                }
                case OpCodes.CREATE_OBJECT -> {
                    var createInsn = (CREATE_OBJECT_Insn) insn;

                    var fields = new Object[createInsn.getFieldCount()];
                    for (int i = fields.length - 1; i >= 0; i--)
                        fields[i] = interpreter.pop();
                    interpreter.push(new ObjectValue(createInsn.getStructId(), fields));
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
                case OpCodes.L_LESS_EQUAL, OpCodes.L_LESS -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    boolean result;
                    if (code == OpCodes.L_LESS)
                        result = left < right;
                    else
                        result = left <= right;

                    interpreter.push(result);
                }
                case OpCodes.D_LESS_EQUAL, OpCodes.D_LESS -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    boolean result;
                    if (code == OpCodes.D_LESS)
                        result = left < right;
                    else
                        result = left <= right;

                    interpreter.push(result);
                }
                case OpCodes.L_GREATER_EQUAL, OpCodes.L_GREATER -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    boolean result;
                    if (code == OpCodes.L_GREATER)
                        result = left > right;
                    else
                        result = left >= right;

                    interpreter.push(result);
                }
                case OpCodes.D_GREATER_EQUAL, OpCodes.D_GREATER -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    boolean result;
                    if (code == OpCodes.D_GREATER)
                        result = left > right;
                    else
                        result = left >= right;

                    interpreter.push(result);
                }
                case OpCodes.L_EQUAL -> {
                    var right = (Long) interpreter.pop();
                    var left = (Long) interpreter.pop();
                    interpreter.push(Objects.equals(left, right));
                }
                case OpCodes.D_EQUAL -> {
                    var right = (Double) interpreter.pop();
                    var left = (Double) interpreter.pop();
                    interpreter.push(Objects.equals(left, right));
                }
                case OpCodes.L_TO_D -> {
                    var value = (Long) interpreter.pop();
                    interpreter.push(value.doubleValue());
                }
                case OpCodes.D_TO_L -> {
                    var value = (Double) interpreter.pop();
                    interpreter.push(value.longValue());
                }
                case OpCodes.EXIT -> interpreter.exit();
                default -> interpreter.handleUnknownOpCode(code, insn);
            }
        } catch (Throwable e) {
            try {
                interpreter.getErrStream().write("Error while execution instruction %s at address %d%n".formatted(insn.getDebugString(), interpreter.getProgramCounter()).getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw e;
        }
    }

    private static Object[] collectFunctionArguments(Interpreter interpreter, boolean instanceFunction, int argCount) {
        for (int i = argCount - 1; i >= 0; i--) {
            if (instanceFunction && i == 0 && interpreter.peek() == ObjectValue.NULL)
                throw new RuntimeException("Cannot call instance function on null value");

            ARGS_ARRAY[i] = interpreter.pop();
        }

        return ARGS_ARRAY;
    }

    private static FunctionDeclaration prelude(String struct, String function) {
        return (FunctionDeclaration) Prelude.getStructForTypeName(Span.fromString(struct)).getMember(Span.fromString(function));
    }

    private static String intrinsicToString(Interpreter interpreter, Object o) {
        if (o instanceof List<?> l) {
            var sb = new StringBuilder();
            sb.append("[");
            for (var i = 0; i < l.size(); i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(intrinsicToString(interpreter, l.get(i)));
            }
            sb.append("]");
            return sb.toString();
        } else if (o instanceof ObjectValue v) {
            var structData = interpreter.getStructData(v.getStructId());
            var str = new StringBuilder(structData.name());
            str.append('(');
            for (int i = 0; i < structData.fieldNames().length; i++) {
                if (i > 0)
                    str.append(", ");

                var fieldName = structData.fieldNames()[i];
                str
                        .append(fieldName)
                        .append(": ")
                        .append(intrinsicToString(interpreter, v.getField(i)));
            }
            str.append(')');
            return str.toString();
        } else if (o == ObjectValue.NULL) {
            return "null";
        } else {
            return o.toString();
        }
    }
}
