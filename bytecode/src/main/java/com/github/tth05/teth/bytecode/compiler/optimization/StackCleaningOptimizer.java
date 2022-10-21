package com.github.tth05.teth.bytecode.compiler.optimization;

import com.github.tth05.teth.bytecode.compiler.internal.PlaceholderInvokeInsn;
import com.github.tth05.teth.bytecode.op.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StackCleaningOptimizer implements IOptimizer, OpCodes {

    @Override
    public void method(List<IInstrunction> instructions) {
        optimizePart(new HashSet<>(), instructions, 0);
    }

    private static void optimizePart(Set<Integer> visitedStarts, List<IInstrunction> instructions, int start) {
        if (!visitedStarts.add(start))
            return;

        var unusedStackValues = new ArrayDeque<Marker>();

        loop:
        for (int i = start; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            switch (instruction.getOpCode()) {
                // 2 -> 1
                case LD_ADD, LD_SUB, LD_MUL, LD_DIV, LD_POW, LD_EQUAL, LD_LESS, LD_LESS_EQUAL, LD_GREATER, LD_GREATER_EQUAL, B_OR, B_AND -> {
                    unusedStackValues.pop();
                    unusedStackValues.pop();
                    unusedStackValues.push(new Marker(i, instruction));
                }
                // 2 -> 0
                case STORE_MEMBER -> {
                    unusedStackValues.pop();
                    unusedStackValues.pop();
                }
                // 1 -> 1
                case LD_NEGATE, B_INVERT, LOAD_MEMBER -> {
                    unusedStackValues.pop();
                    unusedStackValues.push(new Marker(i, instruction));
                }
                // 1 -> 0
                case STORE_LOCAL, POP -> unusedStackValues.pop();
                case JUMP_IF_FALSE -> {
                    unusedStackValues.pop();

                    var a = i + 1;
                    var b = i + ((JUMP_IF_FALSE_Insn) instruction).getRelativeJumpOffset() + 1;
                    // Ensure we visit the one with the higher offset first, otherwise the removal of instructions will mess up the
                    // offsets for the second visit
                    if (a < b) {
                        var temp = a;
                        a = b;
                        b = temp;
                    }

                    optimizePart(visitedStarts, instructions, a);
                    optimizePart(visitedStarts, instructions, b);
                    break loop;
                }
                case RETURN -> {
                    if (((RETURN_Insn) instruction).shouldReturnValue())
                        unusedStackValues.pop();
                    break loop;
                }
                // 0 -> 1
                case S_CONST, L_CONST, D_CONST, B_CONST, CREATE_LIST, LOAD_LOCAL, DUP ->
                        unusedStackValues.push(new Marker(i, instruction));
                // 0 -> 0
                case JUMP -> {
                    optimizePart(visitedStarts, instructions, i + 1 + ((JUMP_Insn) instruction).getRelativeJumpOffset());
                    break loop;
                }
                case EXIT -> {
                    break loop;
                }
                // DYNAMIC CASES
                case INVOKE -> {
                    var invoke = (PlaceholderInvokeInsn) instruction;
                    for (var j = 0; j < invoke.getParamCount(); j++)
                        unusedStackValues.pop();
                    if (invoke.returnsValue())
                        unusedStackValues.push(new Marker(i, instruction));
                }
                case INVOKE_INTRINSIC -> {
                    var invoke = (INVOKE_INTRINSIC_Insn) instruction;
                    var function = invoke.getFunctionDeclaration();
                    var paramCount = function.getParameters().size() + (function.isInstanceFunction() ? 1 : 0);
                    for (var j = 0; j < paramCount; j++)
                        unusedStackValues.pop();
                    if (function.getReturnTypeExpr() != null)
                        unusedStackValues.push(new Marker(i, instruction));
                }
                case CREATE_OBJECT -> {
                    var createObject = (CREATE_OBJECT_Insn) instruction;
                    for (var j = 0; j < createObject.getFieldCount(); j++)
                        unusedStackValues.pop();
                    unusedStackValues.push(new Marker(i, instruction));
                }
                default -> throw new IllegalStateException("Unsupported opcode: " + instruction.getOpCode());
            }
        }

        while (!unusedStackValues.isEmpty()) {
            var value = unusedStackValues.pop();
            var i = value.index();
            var instruction = value.instruction();

            var opCode = instruction.getOpCode();
            if (!isRemovable(opCode)) {
                instructions.add(i + 1, new POP_Insn());
                fixJumps(instructions, i, false);
            } else {
                // If the bytecode was
                //  LOAD_LOCAL
                //  DUP
                //  STORE_LOCAL
                // then the DUP instruction is the one that should be removed, but the unused value list will
                // contain the LOAD_LOCAL instruction
                if (i < instructions.size() - 1 && instructions.get(i + 1).getOpCode() == DUP) {
                    instructions.remove(i + 1);
                    fixJumps(instructions, i + 1, true);
                } else {
                    // Mark dependencies for removal as well, if they are removable.
                    // If the bytecode was
                    //  L_CONST
                    //  L_CONST
                    //  LD_ADD
                    // then it makes sense to also remove both L_CONST instructions to properly clean the stack
                    var dependencies = getDependencies(instructions, i);
                    if (dependencies != null) {
                        for (int k = dependencies.length - 1; k >= 0; k--)
                            unusedStackValues.push(dependencies[k]);
                    }

                    // Remove the instruction
                    instructions.remove(i);
                    fixJumps(instructions, i, true);
                }
            }
        }
    }

    private static void fixJumps(List<IInstrunction> instructions, int instructionIndex, boolean removedInstruction) {
        // NOTE: This method is quite inefficient

        for (int i = 0; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            if (!(instruction instanceof IJumpInstruction jumpInstruction))
                continue;

            var jumpOffset = jumpInstruction.getRelativeJumpOffset();
            var jumpTarget = i + jumpOffset + 1;
            if (i < instructionIndex && instructionIndex <= jumpTarget) { // Crosses barrier LTR
                jumpTarget += removedInstruction ? -1 : 1;
            } else if (i >= instructionIndex && jumpTarget <= instructionIndex) { // Crosses barrier RTL
                jumpTarget += removedInstruction ? 1 : -1;
            } else {
                continue;
            }

            var newRelativeJumpTarget = jumpTarget - i - 1;

            switch (instruction.getOpCode()) {
                case JUMP -> instructions.set(i, new JUMP_Insn(newRelativeJumpTarget));
                case JUMP_IF_FALSE -> instructions.set(i, new JUMP_IF_FALSE_Insn(newRelativeJumpTarget));
            }
        }
    }

    private static Marker[] getDependencies(List<IInstrunction> instructions, int index) {
        var instruction = instructions.get(index);
        var dependencyCount = getPopCount(instruction);
        if (dependencyCount == 0)
            return null;

        var dependencies = new Marker[dependencyCount];
        for (int j = 0, dependencyIndex = index - 1; j < dependencyCount; j++) {
            var dependency = instructions.get(dependencyIndex);
            dependencies[j] = new Marker(dependencyIndex, dependency);

            dependencyIndex -= getPopCount(dependency);
            dependencyIndex--;
        }

        return dependencies;
    }

    private static int getPopCount(IInstrunction instruction) {
        return switch (instruction.getOpCode()) {
            // 2 -> 1
            case LD_ADD, LD_SUB, LD_MUL, LD_DIV, LD_POW, LD_EQUAL, LD_LESS, LD_LESS_EQUAL, LD_GREATER, LD_GREATER_EQUAL,
                    B_OR, B_AND -> 2;
            // 2 -> 0
            case STORE_MEMBER -> 2;
            // 1 -> 1
            case LD_NEGATE, B_INVERT, LOAD_MEMBER -> 1;
            // 1 -> 0
            case STORE_LOCAL, POP -> 1;
            case JUMP_IF_FALSE -> 1;
            case RETURN -> {
                if (((RETURN_Insn) instruction).shouldReturnValue())
                    yield 1;
                yield 0;
            }
            // 0 -> 1
            case S_CONST, L_CONST, D_CONST, B_CONST, CREATE_LIST, CREATE_OBJECT, LOAD_LOCAL, DUP -> 0;
            // 0 -> 0
            case JUMP, EXIT -> 0;
            // INVOKE
            case INVOKE -> ((PlaceholderInvokeInsn) instruction).getParamCount();
            case INVOKE_INTRINSIC -> {
                var invoke = (INVOKE_INTRINSIC_Insn) instruction;
                var function = invoke.getFunctionDeclaration();
                yield function.getParameters().size() + (function.isInstanceFunction() ? 1 : 0);
            }
            default -> throw new IllegalStateException("Unsupported opcode: " + instruction.getOpCode());
        };
    }

    private static boolean isRemovable(int opcode) {
        return switch (opcode) {
            // Unremovable instructions. CREATE_{OBJECT, LIST} are fine currently, but let's keep it future-proof (e.g. constructors)
            case INVOKE, INVOKE_INTRINSIC, CREATE_OBJECT, CREATE_LIST -> false;
            default -> true;
        };
    }

    private record Marker(int index, IInstrunction instruction) {}
}
