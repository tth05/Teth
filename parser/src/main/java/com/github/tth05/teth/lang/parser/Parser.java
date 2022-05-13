package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Parser {

    private final TokenStream stream;

    public Parser(TokenStream stream) {
        this.stream = stream;
    }

    public SourceFileUnit parse() {
        var unit = new SourceFileUnit(parseStatementList(t -> t.is(TokenType.EOF)));
        this.stream.consumeType(TokenType.EOF);
        return unit;
    }

    private StatementList parseStatementList(Predicate<Token> terminatorPredicate) {
        var statements = new StatementList();
        while (true) {
            consumeLineBreaks();

            var token = this.stream.peek();
            if (terminatorPredicate.test(token))
                break;

            statements.add(parseStatement());
        }

        return statements;
    }

    private void consumeLineBreaks() {
        while (this.stream.peek().is(TokenType.LINE_BREAK))
            this.stream.consume();
    }

    private Statement parseStatement() {
        // Variable declaration
        var token = this.stream.peek();
        if (token.is(TokenType.IDENTIFIER)) {
            var next = this.stream.peek(1);
            if (next.is(TokenType.IDENTIFIER))
                return parseVariableDeclaration();
            else if (next.is(TokenType.OP_ASSIGN))
                return parseAssignmentStatement();

            throw new UnexpectedTokenException(next);
        } else if (token.is(TokenType.KEYWORD)) {
            if (token.value().equals("if")) {
                return parseIfStatement();
            } else if (token.value().equals("else")) {
                throw new UnexpectedTokenException(token);
            }

            throw new IllegalStateException("Unexpected keyword: " + token.value());
        } else if (token.is(TokenType.L_CURLY_PAREN)) {
            return parseBlock();
        } else { // Expression statement which does nothing
            return parseExpression();
        }
    }

    private IfStatement parseIfStatement() {
        this.stream.consumeType(TokenType.KEYWORD);
        var condition = parseParenthesisedExpression();
        var body = parseBlock();
        var next = this.stream.peek();
        if (next.is(TokenType.KEYWORD) && next.value().equals("else")) {
            this.stream.consumeType(TokenType.KEYWORD);
            var elseBody = parseBlock();
            return new IfStatement(condition, body, elseBody);
        }

        return new IfStatement(condition, body, null);
    }

    private BlockStatement parseBlock() {
        consumeLineBreaks();
        if (this.stream.peek().is(TokenType.L_CURLY_PAREN)) {
            this.stream.consumeType(TokenType.L_CURLY_PAREN);
            consumeLineBreaks();
            var block = new BlockStatement(parseStatementList(t -> t.is(TokenType.R_CURLY_PAREN)));
            consumeLineBreaks();
            this.stream.consumeType(TokenType.R_CURLY_PAREN);
            return block;
        } else {
            var list = new StatementList();
            list.add(parseStatement());
            consumeLineBreaks();
            return new BlockStatement(list);
        }
    }

    private VariableDeclaration parseVariableDeclaration() {
        var type = this.stream.consumeType(TokenType.IDENTIFIER);
        var name = this.stream.consumeType(TokenType.IDENTIFIER);

        Expression assignment = null;
        if (this.stream.peek().is(TokenType.OP_ASSIGN)) {
            this.stream.consumeType(TokenType.OP_ASSIGN);
            assignment = parseExpression();
        }

        return new VariableDeclaration(type.value(), name.value(), assignment);
    }

    private AssignmentStatement parseAssignmentStatement() {
        var name = this.stream.consumeType(TokenType.IDENTIFIER);
        this.stream.consumeType(TokenType.OP_ASSIGN);
        var value = parseExpression();
        return new AssignmentStatement(new IdentifierExpression(name.value()), value);
    }

    private Expression parseExpression() {
        //TODO: Make this not recursive
        return parseBinaryExpression( //Additive
                () -> parseBinaryExpression( //Multiplicative
                        () -> parseBinaryExpression( //Pow
                                () -> parseBinaryExpression( //Equal
                                        this::parsePrimaryExpression, //Primary
                                        List.of(TokenType.OP_EQUAL)
                                ),
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
        Expression expr;

        var currentType = this.stream.peek().type();
        if (currentType == TokenType.L_PAREN) {
            expr = parseParenthesisedExpression();
        } else {
            expr = parseLiteralExpression();
        }

        if (expr == null)
            throw new UnsupportedOperationException();

        return expr;
    }

    private Expression parseParenthesisedExpression() {
        this.stream.consumeType(TokenType.L_PAREN);
        var parenExpr = parseExpression();
        this.stream.consumeType(TokenType.R_PAREN);
        return parenExpr;
    }

    private Expression parseLiteralExpression() {
        var token = this.stream.peek();

        return switch (token.type()) {
            case NUMBER -> new LongLiteralExpression(Long.parseLong(this.stream.consumeType(TokenType.NUMBER).value()));
            case STRING -> new StringLiteralExpression(this.stream.consumeType(TokenType.STRING).value());
            case IDENTIFIER -> new IdentifierExpression(this.stream.consumeType(TokenType.IDENTIFIER).value());
            default -> null;
        };
    }

    public static SourceFileUnit from(TokenStream stream) {
        return new Parser(stream).parse();
    }
}
