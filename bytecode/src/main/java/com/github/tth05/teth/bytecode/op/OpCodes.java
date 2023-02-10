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

    byte L_TO_D = 0;
    byte D_TO_L = L_TO_D + 1;
    /**
     * Negates a long/double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1 -> result
     * </code></pre>
     */
    byte L_NEGATE = D_TO_L + 1;
    byte D_NEGATE = L_NEGATE + 1;
    /**
     * Adds two longs/doubles. If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte L_ADD = D_NEGATE + 1;
    byte D_ADD = L_ADD + 1;
    /**
     * Subtracts two longs/doubles. If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte L_SUB = D_ADD + 1;
    byte D_SUB = L_SUB + 1;
    /**
     * Multiplies two longs/doubles.  If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte L_MUL = D_SUB + 1;
    byte D_MUL = L_MUL + 1;
    /**
     * Divides two longs/doubles.  If any operand is a double, the result is a double.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte L_DIV = D_MUL + 1;
    byte D_DIV = L_DIV + 1;
    byte L_POW = D_DIV + 1;
    byte D_POW = L_POW + 1;
    byte L_EQUAL = D_POW + 1;
    byte D_EQUAL = L_EQUAL + 1;
    byte L_LESS = D_EQUAL + 1;
    byte D_LESS = L_LESS + 1;
    byte L_LESS_EQUAL = D_LESS + 1;
    byte D_LESS_EQUAL = L_LESS_EQUAL + 1;
    byte L_GREATER = D_LESS_EQUAL + 1;
    byte D_GREATER = L_GREATER + 1;
    byte L_GREATER_EQUAL = D_GREATER + 1;
    byte D_GREATER_EQUAL = L_GREATER_EQUAL + 1;
    /**
     * Logical ORs two booleans.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte B_OR = D_GREATER_EQUAL + 1;
    /**
     * Logical ANDs two booleans.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value1, value2 -> result
     * </code></pre>
     */
    byte B_AND = B_OR + 1;
    /**
     * Logically inverts a boolean.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * value -> result
     * </code></pre>
     */
    byte B_INVERT = B_AND + 1;
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
    byte INVOKE = B_INVERT + 1;
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
    byte INVOKE_INTRINSIC = INVOKE + 1;
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
    byte RETURN = INVOKE_INTRINSIC + 1;
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
    byte JUMP = RETURN + 1;
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
    byte JUMP_IF_FALSE = JUMP + 1;
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
    byte L_CONST = JUMP_IF_FALSE + 1;
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
    byte D_CONST = L_CONST + 1;
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
    byte B_CONST = D_CONST + 1;
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
    byte S_CONST = B_CONST + 1;
    byte NULL_CONST = S_CONST + 1;
    /**
     * Pushes an empty list onto the stack.
     * <br>
     * Stack (before, after):
     * <pre><code>
     * -> value
     * </code></pre>
     */
    byte CREATE_LIST = NULL_CONST + 1;
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
    byte CREATE_OBJECT = CREATE_LIST + 1;
    byte LOAD_LOCAL = CREATE_OBJECT + 1;
    byte STORE_LOCAL = LOAD_LOCAL + 1;
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
    byte LOAD_MEMBER = STORE_LOCAL + 1;
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
    byte STORE_MEMBER = LOAD_MEMBER + 1;
    byte EXIT = STORE_MEMBER + 1;
    byte DUP = EXIT + 1;
    byte POP = DUP + 1;
}