package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.ArrayList;
import java.util.function.Predicate;

//TODO: Allow new-lines everywhere
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
        var current = this.stream.peek();
        var next = this.stream.peek(1);
        if (current.is(TokenType.IDENTIFIER) && next.is(TokenType.IDENTIFIER)) {
            return parseVariableDeclaration();
        } else if (current.is(TokenType.KEYWORD)) { // Keyword statement
            if (current.value().equals("if")) {
                return parseIfStatement();
            } else if (current.value().equals("fn")) {
                return parseFunctionDeclaration();
            }

            throw new UnexpectedTokenException(current);
        } else if (current.is(TokenType.L_CURLY_PAREN)) { // Block statement
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
            consumeLineBreaks();
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
        if (this.stream.peek().is(TokenType.EQUAL)) {
            this.stream.consumeType(TokenType.EQUAL);
            assignment = parseExpression();
        }

        return new VariableDeclaration(type.value(), name.value(), assignment);
    }

    private FunctionDeclaration parseFunctionDeclaration() {
        this.stream.consumeType(TokenType.KEYWORD);
        var name = this.stream.consumeType(TokenType.IDENTIFIER);
        this.stream.consumeType(TokenType.L_PAREN);
        var parameters = new ArrayList<FunctionDeclaration.Parameter>();
        while (true) {
            var token = this.stream.peek();
            if (token.is(TokenType.R_PAREN))
                break;
            if (!parameters.isEmpty())
                this.stream.consumeType(TokenType.COMMA);

            parameters.add(new FunctionDeclaration.Parameter(
                    this.stream.consumeType(TokenType.IDENTIFIER).value(),
                    this.stream.consumeType(TokenType.IDENTIFIER).value())
            );
        }

        this.stream.consumeType(TokenType.R_PAREN);
        var body = parseBlock();
        return new FunctionDeclaration(name.value(), parameters, body);
    }

    private Expression parseExpression() {
        var head = parsePrimaryExpression();
        var current = head;

        while (true) {
            var next = this.stream.peek();
            var operator = BinaryExpression.Operator.fromTokenType(next.type());
            if (operator == null)
                break;

            this.stream.consume();

            if (current instanceof BinaryExpression old && operator.getPrecedence() <= old.getOperator().getPrecedence()) {
                var newRight = new BinaryExpression(old.getRight(), parsePrimaryExpression(), operator);
                old.setRight(newRight);
                current = newRight;
            } else { // Convert head into binary expression
                head = new BinaryExpression(head, parsePrimaryExpression(), operator);
                current = head;
            }
        }

        return head;
    }

    /**
     * Literals, variable access, method calls
     */
    private Expression parsePrimaryExpression() {
        Expression expr;

        var currentType = this.stream.peek().type();
        var operator = UnaryExpression.Operator.fromTokenType(currentType);
        if (currentType == TokenType.L_PAREN) {
            expr = parseParenthesisedExpression();
        } else if (operator != null) {
            this.stream.consume();
            expr = new UnaryExpression(parsePrimaryExpression(), UnaryExpression.Operator.fromTokenType(currentType));
        } else {
            expr = parseLiteralExpression();
        }

        currentType = this.stream.peek().type();
        if (currentType == TokenType.L_PAREN) {
            expr = parseFunctionInvocation(expr);
        }

        return expr;
    }

    private Expression parseFunctionInvocation(Expression target) {
        this.stream.consumeType(TokenType.L_PAREN);
        var parameters = new ExpressionList();
        while (true) {
            var token = this.stream.peek();
            if (token.is(TokenType.R_PAREN))
                break;
            if (!parameters.isEmpty())
                this.stream.consumeType(TokenType.COMMA);

            parameters.add(parseExpression());
        }

        this.stream.consumeType(TokenType.R_PAREN);
        return new FunctionInvocationExpression(target, parameters);
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
            case NUMBER_LITERAL -> new LongLiteralExpression(Long.parseLong(this.stream.consumeType(TokenType.NUMBER_LITERAL).value()));
            case STRING_LITERAL -> new StringLiteralExpression(this.stream.consumeType(TokenType.STRING_LITERAL).value());
            case BOOLEAN_LITERAL -> new BooleanLiteralExpression(Boolean.parseBoolean(this.stream.consumeType(TokenType.BOOLEAN_LITERAL).value()));
            case IDENTIFIER -> new IdentifierExpression(this.stream.consumeType(TokenType.IDENTIFIER).value());
            default -> throw new UnexpectedTokenException(token);
        };
    }

    public static SourceFileUnit from(TokenStream stream) {
        return new Parser(stream).parse();
    }
}
