package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
        if (current.is(TokenType.KEYWORD)) { // Keyword statement
            var keyword = current.value();
            return switch (keyword) {
                case "if" -> parseIfStatement();
                case "loop" -> parseLoopStatement();
                case "struct" -> parseStructDeclaration();
                case "fn" -> parseFunctionDeclaration(false);
                case "return" -> parseReturnStatement();
                case "let" -> parseVariableDeclaration();
                case "new" -> parseExpression();
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
        var next = this.stream.peek();
        var expression = next.is(TokenType.LINE_BREAK) || next.is(TokenType.L_CURLY_PAREN) || next.is(TokenType.R_CURLY_PAREN) ? null : parseExpression();
        return new ReturnStatement(Span.of(firstSpan, expression == null ? firstSpan : expression.getSpan()), expression);
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

    private LoopStatement parseLoopStatement() {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        consumeLineBreaks();

        // Infinite loop with no header
        if (!this.stream.peek().is(TokenType.L_PAREN)) {
            var body = parseBlock();
            return new LoopStatement(Span.of(firstSpan, body.getSpan()), Collections.emptyList(), null, body, null);
        }

        this.stream.consumeType(TokenType.L_PAREN);
        consumeLineBreaks();

        var variableDeclarations = new ArrayList<VariableDeclaration>();
        Expression condition = null;
        Statement advance = null;

        var hasCondition = true;
        while (this.stream.peek().value().equals("let")) {
            consumeLineBreaks();

            hasCondition = false;
            variableDeclarations.add(parseVariableDeclaration());

            if (this.stream.peek().is(TokenType.COMMA)) {
                this.stream.consumeType(TokenType.COMMA);
                hasCondition = true;
            }
        }

        if (hasCondition)
            condition = parseExpression();
        if (hasCondition && this.stream.peek().is(TokenType.COMMA)) {
            this.stream.consumeType(TokenType.COMMA);
            // Well, this will make for some interesting code
            advance = parseStatement();
        }

        this.stream.consumeType(TokenType.R_PAREN);
        consumeLineBreaks();

        var body = parseBlock();
        return new LoopStatement(Span.of(firstSpan, body.getSpan()), variableDeclarations, condition, body, advance);
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
        // We know that a 'let' is here
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        consumeLineBreaks();
        var name = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();

        TypeExpression type = null;
        if (this.stream.peek().is(TokenType.COLON)) {
            this.stream.consumeType(TokenType.COLON);
            consumeLineBreaks();
            type = parseType();
            consumeLineBreaks();
        }

        this.stream.consumeType(TokenType.EQUAL);
        var initializer = parseExpression();

        return new VariableDeclaration(
                Span.of(firstSpan, initializer.getSpan()),
                type, new IdentifierExpression(name.span(), name.value()), initializer
        );
    }

    private FunctionDeclaration parseFunctionDeclaration(boolean instanceMethod) {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        consumeLineBreaks();
        var functionName = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();

        var genericParameters = parseGenericParameterDeclarations();

        consumeLineBreaks();
        this.stream.consumeType(TokenType.L_PAREN);

        var parameters = parseList(() -> {
            var nameToken = this.stream.consumeType(TokenType.IDENTIFIER);
            if (instanceMethod && nameToken.value().equals("self"))
                throw new UnexpectedTokenException(nameToken.span(), "Parameter name 'self' is not allowed for instance methods");

            consumeLineBreaks();
            this.stream.consumeType(TokenType.COLON);
            consumeLineBreaks();
            var type = parseType();
            return new FunctionDeclaration.ParameterDeclaration(type, new IdentifierExpression(nameToken.span(), nameToken.value()));
        }, ArrayList::new, TokenType.R_PAREN);

        this.stream.consumeType(TokenType.R_PAREN);

        consumeLineBreaks();

        var returnType = this.stream.peek().is(TokenType.IDENTIFIER) ? parseType() : null;
        var body = parseBlock();
        return new FunctionDeclaration(
                Span.of(firstSpan, body.getSpan()),
                new IdentifierExpression(functionName.span(), functionName.value()),
                genericParameters, parameters, returnType, body, instanceMethod
        );
    }

    private List<GenericParameterDeclaration> parseGenericParameterDeclarations() {
        var genericParameters = new ArrayList<GenericParameterDeclaration>();
        if (this.stream.peek().is(TokenType.LESS)) {
            this.stream.consumeType(TokenType.LESS);
            parseList(() -> {
                var parameterName = this.stream.consumeType(TokenType.IDENTIFIER);
                consumeLineBreaks();
                return new GenericParameterDeclaration(parameterName.span(), parameterName.value());
            }, () -> genericParameters, TokenType.GREATER);
            this.stream.consumeType(TokenType.GREATER);
        }
        return genericParameters;
    }

    private <T, R extends List<T>> R parseList(Supplier<T> expressionSupplier, Supplier<R> collector, TokenType endToken) {
        var list = collector.get();
        while (!this.stream.peek().is(endToken)) {
            if (!list.isEmpty())
                this.stream.consumeType(TokenType.COMMA);

            consumeLineBreaks();
            list.add(expressionSupplier.get());
            consumeLineBreaks();
        }
        return list;
    }

    private StructDeclaration parseStructDeclaration() {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        consumeLineBreaks();
        var structName = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();

        var genericParameters = parseGenericParameterDeclarations();

        consumeLineBreaks();
        this.stream.consumeType(TokenType.L_CURLY_PAREN);
        consumeLineBreaks();

        var fields = new ArrayList<StructDeclaration.FieldDeclaration>();
        var functions = new ArrayList<FunctionDeclaration>();

        var checkDuplicateDeclaration = (BiConsumer<ISpan, String>) (span, value) -> {
            if (fields.stream().anyMatch(f -> f.getNameExpr().getValue().equals(value)) ||
                functions.stream().anyMatch(f -> f.getNameExpr().getValue().equals(value)))
                throw new UnexpectedTokenException(span, "Duplicate declaration");
        };

        while (!this.stream.peek().is(TokenType.R_CURLY_PAREN)) {
            var token = this.stream.peek();
            if (token.is(TokenType.IDENTIFIER)) {
                var name = this.stream.consumeType(TokenType.IDENTIFIER);
                this.stream.consumeType(TokenType.COLON);
                var type = parseType();

                checkDuplicateDeclaration.accept(name.span(), name.value());
                fields.add(new StructDeclaration.FieldDeclaration(Span.of(name.span(), type.getSpan()), type, new IdentifierExpression(name.span(), name.value()), fields.size()));
            } else if (token.is(TokenType.KEYWORD) && token.value().equals("fn")) {
                var function = parseFunctionDeclaration(true);

                checkDuplicateDeclaration.accept(function.getNameExpr().getSpan(), function.getNameExpr().getValue());
                functions.add(function);
            } else {
                throw new UnexpectedTokenException(token.span(), "Expected field or function declaration");
            }

            consumeLineBreaks();
        }

        var endSpan = this.stream.consumeType(TokenType.R_CURLY_PAREN).span();
        return new StructDeclaration(
                Span.of(firstSpan, endSpan),
                new IdentifierExpression(structName.span(), structName.value()),
                genericParameters,
                fields,
                functions
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

    private ObjectCreationExpression parseObjectCreationExpression() {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD).span();
        consumeLineBreaks();
        var name = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();

        var genericParameters = tryParseGenericParametersOnInvocation(TokenType.LESS);

        this.stream.consumeType(TokenType.L_PAREN);
        var parameters = parseParameterList();
        var secondSpan = this.stream.consumeType(TokenType.R_PAREN).span();

        return new ObjectCreationExpression(
                Span.of(firstSpan, secondSpan),
                new IdentifierExpression(name.span(), name.value()),
                genericParameters, parameters
        );
    }

    /**
     * Literals, variable access, method calls
     */
    private Expression parsePrimaryExpression() {
        consumeLineBreaks();
        Expression expr;

        var currentToken = this.stream.peek();
        var currentType = currentToken.type();
        var operator = UnaryExpression.Operator.fromTokenType(currentType);
        if (currentType == TokenType.L_PAREN) {
            expr = parseParenthesisedExpression();
        } else if (operator != null) {
            var firstSpan = this.stream.consume().span();
            expr = parsePrimaryExpression();
            expr = new UnaryExpression(Span.of(firstSpan, expr.getSpan()), expr, UnaryExpression.Operator.fromTokenType(currentType));
        } else if (currentType == TokenType.KEYWORD && currentToken.value().equals("new")) {
            expr = parseObjectCreationExpression();
        } else {
            expr = parseLiteralExpression();
        }

        while (true) {
            currentType = this.stream.peek().type();
            if (currentType == TokenType.L_PAREN || currentType == TokenType.LESS_PIPE) {
                expr = parseFunctionInvocation(expr);
            } else if (currentType == TokenType.EQUAL) {
                if (!(expr instanceof IAssignmentTarget))
                    throw new UnexpectedTokenException(expr.getSpan(), "Left side of assignment must be an identifier or member access");
                // Consume the equal sign
                this.stream.consume();
                var initializerExpression = parseExpression();
                expr = new VariableAssignmentExpression(
                        Span.of(expr.getSpan(), initializerExpression.getSpan()),
                        expr, initializerExpression
                );
            } else if (currentType == TokenType.DOT) {
                this.stream.consume();
                var target = parseLiteralExpression();
                if (!(target instanceof IdentifierExpression ident))
                    throw new UnexpectedTokenException(target.getSpan(), "Member access name must be an identifier");

                expr = new MemberAccessExpression(Span.of(expr.getSpan(), ident.getSpan()), ident, expr);
            } else {
                break;
            }
        }

        consumeLineBreaks();
        return expr;
    }

    private Expression parseFunctionInvocation(Expression target) {
        var genericParameters = tryParseGenericParametersOnInvocation(TokenType.LESS_PIPE);

        this.stream.consumeType(TokenType.L_PAREN);
        consumeLineBreaks();
        var parameters = parseParameterList();

        var secondSpan = this.stream.consumeType(TokenType.R_PAREN).span();
        return new FunctionInvocationExpression(Span.of(target.getSpan(), secondSpan), target, genericParameters, parameters);
    }

    private List<TypeExpression> tryParseGenericParametersOnInvocation(TokenType prefix) {
        var genericParameters = Collections.<TypeExpression>emptyList();
        if (this.stream.peek().is(prefix)) {
            this.stream.consume();
            genericParameters = parseList(this::parseType, ArrayList::new, TokenType.GREATER);
            this.stream.consumeType(TokenType.GREATER);
            consumeLineBreaks();
        }
        return genericParameters;
    }

    private ExpressionList parseParameterList() {
        return parseList(this::parseExpression, ExpressionList::new, TokenType.R_PAREN);
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
            case L_SQUARE_BRACKET -> {
                this.stream.consume();
                var elements = new ExpressionList();
                while (true) {
                    token = this.stream.peek();
                    if (token.is(TokenType.R_SQUARE_BRACKET))
                        break;

                    if (!elements.isEmpty())
                        this.stream.consumeType(TokenType.COMMA);

                    elements.add(parseExpression());
                }

                var lastSpan = this.stream.consumeType(TokenType.R_SQUARE_BRACKET).span();
                yield new ListLiteralExpression(Span.of(span, lastSpan), elements);
            }
            default -> throw new UnexpectedTokenException(token.span(), "Expected a literal");
        };
    }

    private TypeExpression parseType() {
        var current = this.stream.consume();
        if (!current.is(TokenType.IDENTIFIER))
            throw new UnexpectedTokenException(current.span(), "Expected a type");

        var firstSpan = current.span();
        var genericParameters = tryParseGenericParametersOnInvocation(TokenType.LESS);
        if (genericParameters == null)
            genericParameters = Collections.emptyList();

        return new TypeExpression(Span.of(firstSpan, Span.of(genericParameters, firstSpan)), current.value(), genericParameters);
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
