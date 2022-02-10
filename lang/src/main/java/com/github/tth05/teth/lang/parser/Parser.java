package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.parser.ast.Expression;
import com.github.tth05.teth.lang.parser.ast.VariableDeclaration;

public class Parser {

    private final SourceFileUnit unit = new SourceFileUnit();
    private final TokenStream stream;

    public Parser(TokenStream stream) {
        this.stream = stream;
    }

    private SourceFileUnit parse() {
        while (true) {
            var token = this.stream.peek();
            if (token.is(TokenType.EOF))
                break;

            if (token.is(TokenType.IDENTIFIER)) {
                this.unit.addStatement(parseVariableDeclaration());
            } else {
                throw new UnexpectedTokenException(token);
            }
        }

        return this.unit;
    }

    private VariableDeclaration parseVariableDeclaration() {
        var type = this.stream.consume();
        var name = this.stream.consumeType(TokenType.IDENTIFIER);

        Expression assignment = null;
        if (this.stream.peek().is(TokenType.OP_ASSIGN)) {
            assignment = parseExpression();
        }

        var next = this.stream.peek();
        if (!next.is(TokenType.EOF) && !next.is(TokenType.LINE_BREAK))
            throw new UnexpectedTokenException(next, TokenType.EOF, TokenType.LINE_BREAK);
        return new VariableDeclaration(type.value(), name.value(), assignment);
    }

    private Expression parseExpression() {
        //TODO:
        while (!this.stream.isEmpty())
            this.stream.consume();
        return new Expression();
    }

    public static SourceFileUnit from(TokenStream stream) {
        return new Parser(stream).parse();
    }
}
