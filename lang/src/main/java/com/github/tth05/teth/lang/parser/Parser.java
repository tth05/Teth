package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.List;
import java.util.function.Supplier;

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
            this.stream.consume();
            assignment = parseExpression();
        }

        var next = this.stream.peek();
        if (!next.isLineTerminating())
            throw new UnexpectedTokenException(next, TokenType.EOF, TokenType.LINE_BREAK);
        this.stream.consume();
        return new VariableDeclaration(type.value(), name.value(), assignment);
    }

    private Expression parseExpression() {
        return parseBinaryExpression( //Additive
                () -> parseBinaryExpression( //Multiplicative
                        () -> parseBinaryExpression( //Pow
                                this::parsePrimaryExpression, //Primary
                                List.of(TokenType.OP_ROOF)
                        ),
                        List.of(TokenType.OP_STAR, TokenType.OP_SLASH)
                ),
                List.of(TokenType.OP_PLUS, TokenType.OP_MINUS)
        );
    }

    private Expression parseBinaryExpression(Supplier<Expression> leftRightSupplier, List<TokenType> validOperatorTokens) {
        var left = leftRightSupplier.get();

        while (true) {
            var token = this.stream.peek();
            if (!validOperatorTokens.contains(token.type()))
                break;
            this.stream.consume();

            var operator = BinaryExpression.Operator.fromTokenType(token.type());
            left = new BinaryExpression(left, leftRightSupplier.get(), operator);
        }

        return left;
    }

    /**
     * Literals, variable access, method calls
     */
    private Expression parsePrimaryExpression() {
        var expr = switch (this.stream.peek().type()) {
            case L_PAREN -> {
                this.stream.consume();
                var parenExpr = parseExpression();
                this.stream.consumeType(TokenType.R_PAREN);
                yield parenExpr;
            }
            default -> parseLiteralExpression();
        };

        if (expr == null)
            throw new UnsupportedOperationException();

        return expr;
    }

    private Expression parseLiteralExpression() {
        var token = this.stream.peek();

        return switch (token.type()) {
            case NUMBER -> new LongLiteralExpression(Long.parseLong(this.stream.consume().value()));
            case STRING -> new StringLiteralExpression(this.stream.consume().value());
            default -> null;
        };
    }

    public static SourceFileUnit from(TokenStream stream) {
        return new Parser(stream).parse();
    }
}
