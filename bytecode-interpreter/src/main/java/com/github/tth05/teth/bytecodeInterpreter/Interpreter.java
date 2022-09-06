package com.github.tth05.teth.bytecodeInterpreter;

import com.github.tth05.teth.bytecode.op.*;
import com.github.tth05.teth.bytecode.program.StructData;
import com.github.tth05.teth.bytecode.program.TethProgram;

import java.util.Arrays;

public class Interpreter {

    private final TethProgram program;

    private final Object[] locals = new Object[2048];
    private int localsPointer = 0;

    private final Object[] stack = new Object[1024];
    private int stackPointer = 0;

    private final int[] returnAddresses = new int[1024];
    private int returnAddressesPointer = 0;

    private int programCounter = 0;

    public Interpreter(TethProgram program) {
        this.program = program;
    }

    public void execute() {
        // These locals exist for micro-optimization
        var cachedOpCodes = Arrays.stream(this.program.getInstructions()).mapToInt(IInstrunction::getOpCode).toArray();
        var instructions = this.program.getInstructions();

        var pc = 0;
        while ((pc = this.programCounter) != -1) {
            InstructionsImpl.run(this, cachedOpCodes[pc], instructions[pc]);
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

    public Object peek() {
        if (this.stackPointer == 0)
            throw new IllegalStateException("Stack is empty");

        var result = this.stack[this.stackPointer];
        if (result == null)
            throw new IllegalStateException("Cannot peek on stack boundary");

        return result;
    }

    public Object loadLocal(int localIndex) {
        var localCount = (int) this.locals[this.localsPointer];
        if (localIndex >= localCount || localIndex < 0)
            throw new IllegalArgumentException("Local index is out of bounds");

        return this.locals[this.localsPointer - localCount + localIndex];
    }

    public void storeLocal(int localIndex, Object value) {
        var localCount = (int) this.locals[this.localsPointer];
        if (localIndex >= localCount || localIndex < 0)
            throw new IllegalArgumentException("Local index is out of bounds");

        this.locals[this.localsPointer - localCount + localIndex] = value;
    }

    public void exit() {
        this.programCounter = -2;
    }

    public void jumpToReturnAddress() {
        if (this.returnAddressesPointer == 0)
            throw new IllegalStateException("No return address available");

        var returnAddress = this.returnAddresses[this.returnAddressesPointer];
        if (returnAddress == 0)
            throw new IllegalStateException("Top return address is invalid");

        setProgramCounter(returnAddress - 1);
        this.returnAddresses[this.returnAddressesPointer--] = 0;

        var locals = this.locals;
        var localCount = (int) locals[this.localsPointer] + 1;
        for (int i = 0; i < localCount; i++)
            locals[this.localsPointer--] = null;
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
        if (pc < 0 || pc > this.program.getInstructions().length)
            throw new IllegalArgumentException("Program counter is out of bounds");

        this.programCounter = pc;
    }

    public int getProgramCounter() {
        return this.programCounter;
    }

    public void initLocalsFromStack(int paramCount, int localCount) {
        localCount += paramCount;

        for (int i = paramCount; i >= 1; i--)
            this.locals[this.localsPointer + i] = pop();

        this.localsPointer += localCount;
        this.locals[++this.localsPointer] = localCount;
    }

    public StructData getStructData(int structId) {
        return this.program.getStructData()[structId];
    }
}
