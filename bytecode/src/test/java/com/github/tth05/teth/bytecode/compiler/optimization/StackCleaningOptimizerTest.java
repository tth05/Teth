package com.github.tth05.teth.bytecode.compiler.optimization;

import com.github.tth05.teth.bytecode.compiler.internal.PlaceholderInvokeInsn;
import com.github.tth05.teth.bytecode.op.*;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.TypeExpression;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class StackCleaningOptimizerTest implements OpCodes {

    @Test
    public void testOptimizeUnusedAssignment() {
        var list = list(
                new LOAD_LOCAL_Insn(0),
                new L_CONST_Insn(1),
                new L_ADD_Insn(),
                new DUP_Insn(),
                new DUP_Insn(),
                new DUP_Insn(),
                new DUP_Insn(),
                new STORE_LOCAL_Insn(0)
        );

        new StackCleaningOptimizer().method(list);

        assertArrayEquals(new byte[]{
                LOAD_LOCAL,
                L_CONST,
                L_ADD,
                STORE_LOCAL
        }, toOpCodes(list));
    }

    @Test
    public void testOptimizeMath() {
        var list = list(
                new L_CONST_Insn(1),
                new L_CONST_Insn(1),
                new L_ADD_Insn()
        );

        new StackCleaningOptimizer().method(list);

        assertArrayEquals(new byte[]{}, toOpCodes(list));
    }

    @Test
    public void testKeepInvokeDependencies() {
        var list = list(
                new L_CONST_Insn(1),

                new L_CONST_Insn(1),
                new L_CONST_Insn(1),
                new L_ADD_Insn(),
                new L_CONST_Insn(1),
                new L_CONST_Insn(1),
                createPlaceholderInvokeInsn(3, true),

                new L_ADD_Insn()
        );

        new StackCleaningOptimizer().method(list);

        assertArrayEquals(new byte[]{
                L_CONST,
                L_CONST,
                L_ADD,
                L_CONST,
                L_CONST,
                INVOKE,
                POP
        }, toOpCodes(list));
    }

    private static PlaceholderInvokeInsn createPlaceholderInvokeInsn(int paramCount, boolean returnsValue) {
        var parameters = IntStream.range(0, paramCount).mapToObj(i -> new FunctionDeclaration.ParameterDeclaration(null, null, null)).toList();
        var returnExpr = returnsValue ? new TypeExpression(null, null) : null;
        return new PlaceholderInvokeInsn(new FunctionDeclaration(null, null, null, List.of(), parameters, returnExpr, null, false));
    }

    @Test
    public void testFixUpJumps() {
        var list = list(
                // if
                new B_CONST_Insn(true),
                new JUMP_IF_FALSE_Insn(4),
                // body
                new L_CONST_Insn(1),
                new L_CONST_Insn(1),
                new L_ADD_Insn(),
                new JUMP_Insn(3),
                // else
                new L_CONST_Insn(1),
                new L_CONST_Insn(1),
                new L_ADD_Insn(),
                new JUMP_Insn(3),
                new JUMP_Insn(5),
                new EXIT_Insn()
        );

        new StackCleaningOptimizer().method(list);

        assertArrayEquals(new byte[]{
                B_CONST,
                JUMP_IF_FALSE,
                JUMP,
                JUMP,
                JUMP,
                EXIT
        }, toOpCodes(list));
        assertEquals(0, ((JUMP_IF_FALSE_Insn) list.get(1)).getRelativeJumpOffset());
        assertEquals(0, ((JUMP_Insn) list.get(2)).getRelativeJumpOffset());
        assertEquals(3, ((JUMP_Insn) list.get(3)).getRelativeJumpOffset());
        assertEquals(5, ((JUMP_Insn) list.get(4)).getRelativeJumpOffset());
    }

    @Test
    public void testLoop() {
        var list = list(
                // let i = 0
                new L_CONST_Insn(0),
                new STORE_LOCAL_Insn(0),
                // while i < 10
                new LOAD_LOCAL_Insn(0),
                new L_CONST_Insn(10),
                new L_LESS_Insn(),
                new JUMP_IF_FALSE_Insn(6),
                // body
                // i = i + 1
                new LOAD_LOCAL_Insn(0),
                new L_CONST_Insn(1),
                new L_ADD_Insn(),
                new DUP_Insn(),
                new STORE_LOCAL_Insn(0),
                // jump back
                new JUMP_Insn(-10),
                new EXIT_Insn()
        );

        new StackCleaningOptimizer().method(list);

        assertArrayEquals(new byte[]{
                L_CONST,
                STORE_LOCAL,
                LOAD_LOCAL,
                L_CONST,
                L_LESS,
                JUMP_IF_FALSE,
                LOAD_LOCAL,
                L_CONST,
                L_ADD,
                STORE_LOCAL,
                JUMP,
                EXIT
        }, toOpCodes(list));
        assertEquals(5, ((JUMP_IF_FALSE_Insn) list.get(5)).getRelativeJumpOffset());
        assertEquals(-9, ((JUMP_Insn) list.get(10)).getRelativeJumpOffset());
    }

    private static List<IInstrunction> list(IInstrunction... instructions) {
        return new ArrayList<>(List.of(instructions));
    }

    private static byte[] toOpCodes(List<IInstrunction> instructions) {
        var opCodes = new byte[instructions.size()];
        for (int i = 0; i < instructions.size(); i++)
            opCodes[i] = instructions.get(i).getOpCode();
        return opCodes;
    }
}
