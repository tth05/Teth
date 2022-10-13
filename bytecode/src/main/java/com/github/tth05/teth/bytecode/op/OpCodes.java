package com.github.tth05.teth.bytecode.op;

/**
 * <p>
 * Stack for values
 * - Value1
 * - Value2
 * - null <- Marks boundary between method calls
 * - Value1
 * - Value2
 * - ...
 * Local variables (including params) array
 * - Local one <- May be uninitialized (null), cannot be accessed if so
 * - Local two
 * - Local count
 * - Local one
 * - Local count
 * Return address array
 * - Return address1
 * - Return address2
 * - Return address3
 */
public interface OpCodes {

    /**
     * Negates a long/double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1 -> result
     * </code></pre>
     */
    byte LD_NEGATE = 0;
    /**
     * Adds two longs/doubles. If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte LD_ADD = 1;
    /**
     * Subtracts two longs/doubles. If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte LD_SUB = 27;
    /**
     * Multiplies two longs/doubles.  If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte LD_MUL = 2;
    /**
     * Divides two longs/doubles.  If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte LD_DIV = 3;
    byte LD_EQUAL = 4;
    byte LD_LESS = 5;
    byte LD_LESS_EQUAL = 6;
    byte LD_GREATER = 7;
    byte LD_GREATER_EQUAL = 8;
    /**
     * Logical ORs two booleans.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte B_OR = 9;
    /**
     * Logical ANDs two booleans.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte B_AND = 10;
    /**
     * Logically inverts a boolean.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value -> result
     * </code></pre>
     */
    byte B_INVERT = 11;
    /**
     * Invokes a function.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * (thisObject), param1, param2,..., paramN ->
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>1 byte is instance function</li>
     *   <li>1 byte param count</li>
     *   <li>1 byte locals count of method being called</li>
     *   <li>4 byte absolute jump address</li>
     * </ul>
     */
    byte INVOKE = 12;
    /**
     * Invokes an intrinsic function.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * (thisObject), param1, param2,..., paramN ->
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>4 byte intrinsic name length</li>
     *   <li>N bytes intrinsic name</li>
     * </ul>
     */
    byte INVOKE_INTRINSIC = 13;
    /**
     * Returns from a function.
     * <br>
     * Clears the stack until boundary is reached. Replaces boundary with top of stack if a return value is required.
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>1 byte returns value</li>
     * </ul>
     */
    byte RETURN = 14;
    /**
     * Performs an unconditional jump.
     * <br>
     * Leaves the stack untouched.
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>4 byte relative jump offset</li>
     * </ul>
     */
    byte JUMP = 15;
    /**
     * Performs a conditional jump.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value ->
     * </code></pre>
     * <br>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>4 byte relative jump offset if true</li>
     * </ul>
     */
    byte JUMP_IF_FALSE = 16;
    /**
     * Pushes a long constant onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> value
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>4 byte long constant</li>
     * </ul>
     */
    byte L_CONST = 17;
    /**
     * Pushes a double constant onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> value
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>4 byte double constant</li>
     * </ul>
     */
    byte D_CONST = 18;
    /**
     * Pushes a boolean constant onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> value
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>1 byte boolean constant</li>
     * </ul>
     */
    byte B_CONST = 19;
    /**
     * Pushes a string constant onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> value
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>4 byte string constant length</li>
     *   <li><strong>n</strong> bytes string constant bytes</li>
     * </ul>
     */
    byte S_CONST = 20;
    /**
     * Pushes an empty list onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> value
     * </code></pre>
     */
    byte CREATE_LIST = 21;
    /**
     * Pushes a new object instance onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> instance
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>2 byte object field count</li>
     * </ul>
     */
    byte CREATE_OBJECT = 28;
    byte LOAD_LOCAL = 22;
    byte STORE_LOCAL = 23;
    /**
     * Pushes a member of an instance onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * instance -> value
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>2 byte member index</li>
     * </ul>
     */
    byte LOAD_MEMBER = 24;
    /**
     * Stores a value from the stack into a member of an instance.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * instance, value ->
     * </code></pre>
     * Encoding:
     * <ul>
     *   <li>1 byte op code</li>
     *   <li>2 byte member index</li>
     * </ul>
     */
    byte STORE_MEMBER = 29;
    byte EXIT = 25;
    byte DUP = 26;
    byte POP = 30;
}
