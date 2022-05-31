package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.function.Predicate;

//TODO: Allow new-lines everywhere
public class Parser {

    private final TokenStream stream;

    public Parser(TokenStream stream) {
        this.stream = stream;
    }

    public ParserResult parse() {
        try {
            var unit = new SourceFileUnit(parseStatementList(t -> t.is(TokenType.EOF)));
            this.stream.consumeType(TokenType.EOF);
            return new ParserResult(unit);
        } catch (UnexpectedTokenException e) {
            return new ParserResult(null, ProblemList.of(e.asProblem()));
        }
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
        consumeLineBreaks();
        var current = this.stream.peek();
        var next = this.stream.peek(1);
        if (current.is(TokenType.IDENTIFIER) && next.is(TokenType.IDENTIFIER)) {
            return parseVariableDeclaration();
        } else if (current.is(TokenType.KEYWORD)) { // Keyword statement
            var keyword = current.value();
            return switch (keyword) {
                case "if" -> parseIfStatement();
                case "fn" -> parseFunctionDeclaration();
                case "return" -> parseReturnStatement();
                default -> throw new UnexpectedTokenException(current.span(), "Keyword '%s' not allowed here", keyword);
            };
        } else if (current.is(TokenType.L_CURLY_PAREN)) { // Block statement
            return parseBlock();
        } else { // Expression statement which does nothing
            return parseExpression();
        }
    }

    private Statement parseReturnStatement() {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        var expression = parseExpression();
        return new ReturnStatement(Span.of(firstSpan, expression.getSpan()), expression);
    }

    private IfStatement parseIfStatement() {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        var condition = parseParenthesisedExpression();
        var body = parseBlock();
        var next = this.stream.peek();
        if (next.is(TokenType.KEYWORD) && next.value().equals("else")) {
            this.stream.consumeType(TokenType.KEYWORD);
            var elseBody = parseBlock();
            return new IfStatement(Span.of(firstSpan, elseBody.getSpan()), condition, body, elseBody);
        }

        return new IfStatement(Span.of(firstSpan, body.getSpan()), condition, body, null);
    }

    private BlockStatement parseBlock() {
        if (this.stream.peek().is(TokenType.L_CURLY_PAREN)) {
            var firstSpan = this.stream.consumeType(TokenType.L_CURLY_PAREN).span();
            consumeLineBreaks();
            var statements = parseStatementList(t -> t.is(TokenType.R_CURLY_PAREN));
            consumeLineBreaks();
            var secondSpan = this.stream.consumeType(TokenType.R_CURLY_PAREN).span();
            consumeLineBreaks();
            return new BlockStatement(Span.of(firstSpan, secondSpan), statements);
        } else {
            var list = new StatementList();
            consumeLineBreaks();
            list.add(parseStatement());
            consumeLineBreaks();
            return new BlockStatement(list.get(0).getSpan(), list);
        }
    }

    private VariableDeclaration parseVariableDeclaration() {
        var type = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();
        var name = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();

        Expression assignment = null;
        if (this.stream.peek().is(TokenType.EQUAL)) {
            this.stream.consumeType(TokenType.EQUAL);
            assignment = parseExpression();
        }

        return new VariableDeclaration(
                Span.of(type.span(), assignment != null ? assignment.getSpan() : name.span()),
                Type.fromString(type.value()), new IdentifierExpression(name.span(), name.value()), assignment
        );
    }

    private FunctionDeclaration parseFunctionDeclaration() {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        consumeLineBreaks();
        var functionName = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();
        this.stream.consumeType(TokenType.L_PAREN);
        var parameters = new ArrayList<FunctionDeclaration.Parameter>();
        while (true) {
            var token = this.stream.peek();
            if (token.is(TokenType.R_PAREN))
                break;
            if (!parameters.isEmpty())
                this.stream.consumeType(TokenType.COMMA);

            consumeLineBreaks();
            var type = Type.fromString(this.stream.consumeType(TokenType.IDENTIFIER).value());
            consumeLineBreaks();
            var parameterName = this.stream.consumeType(TokenType.IDENTIFIER).value();
            consumeLineBreaks();
            parameters.add(new FunctionDeclaration.Parameter(type, parameterName));
        }

        this.stream.consumeType(TokenType.R_PAREN);
        consumeLineBreaks();
        var returnTypeToken = this.stream.consumeType(TokenType.IDENTIFIER);
        var body = parseBlock();
        return new FunctionDeclaration(
                Span.of(firstSpan, body.getSpan()),
                new IdentifierExpression(functionName.span(), functionName.value()),
                new TypeExpression(returnTypeToken.span(), Type.fromString(returnTypeToken.value())),
                parameters, body
        );
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

            if (current instanceof BinaryExpression old) {
                if (operator.getPrecedence() < old.getOperator().getPrecedence()) {
                    var newRight = new BinaryExpression(old.getRight(), parsePrimaryExpression(), operator);
                    old.setRight(newRight);
                    current = newRight;
                } else {
                    var newCurrent = new BinaryExpression(old.getLeft(), old.getRight(), old.getOperator());
                    old.setLeft(newCurrent);
                    old.setRight(parsePrimaryExpression());
                    old.setOperator(operator);
                }
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
        consumeLineBreaks();
        Expression expr;

        var currentType = this.stream.peek().type();
        var operator = UnaryExpression.Operator.fromTokenType(currentType);
        if (currentType == TokenType.L_PAREN) {
            expr = parseParenthesisedExpression();
        } else if (operator != null) {
            var firstSpan = this.stream.consume().span();
            expr = parsePrimaryExpression();
            expr = new UnaryExpression(Span.of(firstSpan, expr.getSpan()), expr, UnaryExpression.Operator.fromTokenType(currentType));
        } else {
            expr = parseLiteralExpression();
        }

        while (true) {
            currentType = this.stream.peek().type();
            if (currentType == TokenType.L_PAREN) {
                expr = parseFunctionInvocation(expr);
            } else if (currentType == TokenType.EQUAL) {
                if (!(expr instanceof IdentifierExpression ident))
                    throw new UnexpectedTokenException(expr.getSpan(), "Left side of assignment must be an identifier");
                // Consume the equal sign
                this.stream.consume();
                var initializerExpression = parseExpression();
                expr = new VariableAssignmentExpression(
                        Span.of(expr.getSpan(), initializerExpression.getSpan()),
                        ident, initializerExpression
                );
            } else if (currentType == TokenType.DOT) {
                this.stream.consume();
                var target = parseLiteralExpression();
                if (!(target instanceof IdentifierExpression ident))
                    throw new UnexpectedTokenException(target.getSpan(), "Method access name must be an identifier");

                expr = new MemberAccessExpression(Span.of(expr.getSpan(), ident.getSpan()), ident, expr);
            } else {
                break;
            }
        }

        consumeLineBreaks();
        return expr;
    }

    private Expression parseFunctionInvocation(Expression target) {
        var firstSpan = this.stream.consumeType(TokenType.L_PAREN).span();
        consumeLineBreaks();
        var parameters = new ExpressionList();
        while (true) {
            var token = this.stream.peek();
            if (token.is(TokenType.R_PAREN))
                break;
            if (!parameters.isEmpty())
                this.stream.consumeType(TokenType.COMMA);

            parameters.add(parseExpression());
        }

        var secondSpan = this.stream.consumeType(TokenType.R_PAREN).span();
        return new FunctionInvocationExpression(Span.of(firstSpan, secondSpan), target, parameters);
    }

    private Expression parseParenthesisedExpression() {
        consumeLineBreaks();
        this.stream.consumeType(TokenType.L_PAREN);
        var parenExpr = parseExpression();
        this.stream.consumeType(TokenType.R_PAREN);
        consumeLineBreaks();
        return parenExpr;
    }

    private Expression parseLiteralExpression() {
        var token = this.stream.peek();
        var span = token.span();

        return switch (token.type()) {
            case LONG_LITERAL -> new LongLiteralExpression(span, Long.parseLong(this.stream.consume().value()));
            case DOUBLE_LITERAL -> new DoubleLiteralExpression(span, Double.parseDouble(this.stream.consume().value()));
            case STRING_LITERAL ->
                    new StringLiteralExpression(span, this.stream.consumeType(TokenType.STRING_LITERAL).value());
            case BOOLEAN_LITERAL ->
                    new BooleanLiteralExpression(span, Boolean.parseBoolean(this.stream.consumeType(TokenType.BOOLEAN_LITERAL).value()));
            case IDENTIFIER -> new IdentifierExpression(span, this.stream.consumeType(TokenType.IDENTIFIER).value());
            default -> throw new UnexpectedTokenException(token.span(), "Expected a literal");
        };
    }

    public static ParserResult fromString(String source) {
        var tokenizerResult = Tokenizer.streamOf(CharStream.fromString(source));
        if (tokenizerResult.hasProblems())
            return new ParserResult(null, tokenizerResult.getProblems());
        return new Parser(tokenizerResult.getTokenStream()).parse();
    }

    public static ParserResult from(TokenStream stream) {
        return new Parser(stream).parse();
    }
}
