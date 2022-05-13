package com.github.tth05.teth.lang.stream;

public class CharStream {

    private final char[] chars;

    private int index;

    private CharStream(String source) {
        this.chars = source.toCharArray();
    }

    public char consume() {
        if (!isValidIndex(0))
            throw new EndOfStreamException();

        return this.chars[this.index++];
    }

    public char peek() {
        return peek(0);
    }

    public char peek(int offset) {
        if (!isValidIndex(offset))
            return 0;

        return this.chars[this.index + offset];
    }

    public boolean isEmpty() {
        return peek() == 0;
    }

    private boolean isValidIndex(int offset) {
        return this.index + offset < this.chars.length;
    }

    public static CharStream fromString(String source) {
        return new CharStream(source + '\u0000');
    }
}
