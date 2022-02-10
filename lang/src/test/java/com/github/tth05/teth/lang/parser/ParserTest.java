package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.stream.CharStream;
import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    public void parseVariableDeclaration() {
        System.out.println(Parser.from(Tokenizer.streamOf(CharStream.fromString("double d /"))));
    }
}
