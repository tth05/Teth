package com.github.tth05.teth.bytecodeInterpreter;

import com.github.tth05.teth.bytecode.decoder.*;

import java.util.Arrays;

public class Interpreter {

    private final IInstrunction[] instructions = new IInstrunction[]{
            //main
            new S_CONST_Insn("Fib of 10 is "),
            new L_CONST_Insn(30),
            new INVOKE_Insn(false, 1, 0, 7),
            new INVOKE_INTRINSIC_Insn("long.toString"),
            new INVOKE_INTRINSIC_Insn("string.concat"),
            new INVOKE_INTRINSIC_Insn("print"),
            new EXIT_Insn(),
            //fib(long n) long
            new LOAD_LOCAL_Insn(0),
            new L_CONST_Insn(1),
            new LD_LESS_EQUAL_Insn(),
            new JUMP_IF_TRUE_Insn(11),
            //fib(n-1)
            new LOAD_LOCAL_Insn(0),
            new L_CONST_Insn(-1),
            new LD_ADD_Insn(),
            new INVOKE_Insn(false, 1, 0, 7),
            //fib(n-2)
            new LOAD_LOCAL_Insn(0),
            new L_CONST_Insn(-2),
            new LD_ADD_Insn(),
            new INVOKE_Insn(false, 1, 0, 7),
            //fib(n-1) + fib(n-2)
            new LD_ADD_Insn(),
            new RETURN_Insn(true),
            // else
            new LOAD_LOCAL_Insn(0),
            new RETURN_Insn(true)
    };
    /**
     * Avoids invokeinterface call when execution instruction.
     */
    private final int[] cachedOpCodes = Arrays.stream(this.instructions).mapToInt(IInstrunction::getOpCode).toArray();

    private final Object[] locals = new Object[2048];
    private int localsPointer = 0;

    private final Object[] stack = new Object[1024];
    private int stackPointer = 0;

    private final int[] returnAddresses = new int[1024];
    private int returnAddressesPointer = 0;

    private int programCounter = 0;

    public void execute() {
        var pc = 0;
        while ((pc = this.programCounter) != -1) {
            InstructionsImpl.run(this, this.cachedOpCodes[pc], this.instructions[pc]);
            if (this.programCounter == pc)
                this.programCounter++;
        }
    }

    public Object pop() {
        if (this.stackPointer == 0)
            throw new IllegalStateException("Stack is empty");

        var result = this.stack[this.stackPointer];
        if (result == null)
            throw new IllegalStateException("Cannot pop past stack boundary");

        this.stack[this.stackPointer--] = null;
        return result;
    }

    public void push(Object value) {
        if (value == null)
            throw new IllegalArgumentException("Cannot push null");

        if (this.stackPointer == this.stack.length - 1)
            throw new IllegalStateException("Stack is full");

        this.stack[++this.stackPointer] = value;
    }

    public Object local(int localIndex) {
        var localCount = (int) this.locals[this.localsPointer];
        if (localIndex >= localCount || localIndex < 0)
            throw new IllegalArgumentException("Local index is out of bounds");

        return this.locals[this.localsPointer - localCount + localIndex];
    }

    public void exit() {
        this.programCounter = -1;
    }

    public void jumpToReturnAddress() {
        if (this.returnAddressesPointer == 0)
            throw new IllegalStateException("No return address available");

        var returnAddress = this.returnAddresses[this.returnAddressesPointer];
        if (returnAddress == 0)
            throw new IllegalStateException("Top return address is invalid");

        setProgramCounter(returnAddress);
        this.returnAddresses[this.returnAddressesPointer--] = 0;

        var localCount = (int) this.locals[this.localsPointer];
        for (int i = 0; i < localCount + 1; i++)
            this.locals[this.localsPointer--] = null;
    }

    public void saveReturnAddress() {
        if (this.returnAddressesPointer == this.returnAddresses.length - 1)
            throw new IllegalStateException("Return address stack is full");

        this.returnAddresses[++this.returnAddressesPointer] = this.programCounter + 1;
    }

    public boolean stackAtBoundary() {
        return this.stack[this.stackPointer] == null;
    }

    public void createStackBoundary() {
        this.stack[++this.stackPointer] = null;
    }

    public void replaceStackBoundary(Object returnValue) {
        if (returnValue == null)
            throw new IllegalArgumentException();

        if (!stackAtBoundary())
            throw new IllegalStateException("Stack is not at boundary");

        this.stack[this.stackPointer] = returnValue;
    }

    public void popStackBoundary() {
        if (!stackAtBoundary())
            throw new IllegalStateException("Stack is not at boundary");

        this.stackPointer--;
    }

    public void setProgramCounter(int pc) {
        if (pc < 0 || pc > this.instructions.length)
            throw new IllegalArgumentException("Program counter is out of bounds");

        this.programCounter = pc;
    }

    public int getProgramCounter() {
        return this.programCounter;
    }

    public void initLocalsFrom(Object[] parameterValues, int paramCount, int localCount) {
        localCount += paramCount;

        for (int i = 0; i < paramCount; i++)
            this.locals[++this.localsPointer] = parameterValues[i];

        this.locals[++this.localsPointer] = localCount;
    }
}
