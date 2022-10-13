package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.diagnostics.Problem;
import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.lexer.*;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.parser.recovery.AnchorSets;
import com.github.tth05.teth.lang.parser.recovery.AnchorUnion;
import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.span.ArrayListWithSpan;
import com.github.tth05.teth.lang.span.ListWithSpan;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Parser {

    private final ProblemList problems;
    private final TokenStream stream;

    private boolean suppressErrors;

    private Parser(TokenizerResult tokenizerResult) {
        this(tokenizerResult.getTokenStream(), tokenizerResult.getProblems());
    }

    private Parser(TokenStream stream) {
        this(stream, ProblemList.of());
    }

    private Parser(TokenStream stream, ProblemList problems) {
        this.stream = stream.sanitized();
        this.problems = problems;
    }

    public ParserResult parse() {
        var unit = new SourceFileUnit(this.stream.getSource().getModuleName(), parseStatementList(AnchorSets.FIRST_SET_STATEMENT, TokenType.EOF));
        expectToken(TokenType.EOF, AnchorSets.EMPTY, () -> "Expected end of file");

        if (this.problems.isEmpty())
            return new ParserResult(this.stream.getSource(), unit);
        else
            return new ParserResult(this.stream.getSource(), unit, this.problems);
    }

    private UseStatement parseUseStatement(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        var lastSpan = firstSpan;
        consumeLineBreaks();

        var path = new ArrayList<IdentifierExpression>(2);
        while (true) {
            var part = expectIdentifier(anchorSet.union(AnchorSets.FIRST_SET_USE_STATEMENT), () -> "Expected use statement path part");
            if (!part.isInvalid())
                path.add(new IdentifierExpression((lastSpan = part.span()), part.value()));

            if (!this.stream.peek().is(TokenType.SLASH))
                break;
            lastSpan = consume(false).span();
        }

        consumeLineBreaks();
        var temp = expectToken(TokenType.L_CURLY_PAREN, anchorSet.union(AnchorSets.END_SET_USE_STATEMENT), () -> "Expected '{' after use statement");
        if (!temp.isInvalid())
            lastSpan = temp.span();

        var imports = new ArrayList<IdentifierExpression>(8);
        while (true) {
            consumeLineBreaks();
            var part = expectIdentifier(AnchorSets.END_SET_USE_STATEMENT.union(AnchorSets.FIRST_SET_STATEMENT_EXPRESSIONLESS), () -> "Expected identifier");
            consumeLineBreaks();
            if (!part.isInvalid())
                imports.add(new IdentifierExpression((lastSpan = part.span()), part.value()));

            if (!this.stream.peek().is(TokenType.COMMA))
                break;
            lastSpan = consume(false).span();
        }

        var endToken = expectToken(TokenType.R_CURLY_PAREN, anchorSet, () -> "Missing closing '}'");
        if (!endToken.isInvalid())
            lastSpan = endToken.span();

        consumeLineBreaks();
        return new UseStatement(Span.of(firstSpan, lastSpan), path, imports);
    }

    private StatementList parseStatementList(AnchorUnion anchorSet, TokenType endToken) {
        var statements = new StatementList();
        while (true) {
            consumeLineBreaks();

            var token = this.stream.peek();
            if (token.is(endToken) || token.is(TokenType.EOF))
                break;

            statements.add(parseStatement(anchorSet));
        }

        return statements;
    }

    private void consumeLineBreaks() {
        while (this.stream.peek().is(TokenType.LINE_BREAK))
            consume(false);
    }

    private Statement parseStatement(AnchorUnion anchorSet) {
        consumeLineBreaks();
        var current = this.stream.peek();
        return switch (current.type()) {
            case KEYWORD_IF -> parseIfStatement(anchorSet);
            case KEYWORD_LOOP -> parseLoopStatement(anchorSet);
            case KEYWORD_STRUCT -> parseStructDeclaration(anchorSet);
            case KEYWORD_FN -> parseFunctionDeclaration(anchorSet, false);
            case KEYWORD_RETURN -> parseReturnStatement(anchorSet);
            case KEYWORD_LET -> parseVariableDeclaration(anchorSet);
            case KEYWORD_USE -> parseUseStatement(anchorSet);
            case KEYWORD_ELSE -> {
                this.problems.add(new Problem(current.span(), "'else' not allowed here"));
                consume(false);
                yield new GarbageExpression(current.span());
            }
            case L_CURLY_PAREN -> parseBlock(anchorSet);
            default -> parseExpression(anchorSet); // Expression statement
        };
    }

    private Statement parseReturnStatement(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        var next = this.stream.peek();
        var expression = next.is(TokenType.LINE_BREAK) || next.is(TokenType.L_CURLY_PAREN) || next.is(TokenType.R_CURLY_PAREN) ? null : parseExpression(anchorSet);
        return new ReturnStatement(Span.of(firstSpan, expression == null ? firstSpan : expression.getSpan()), expression);
    }

    private IfStatement parseIfStatement(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        var condition = parseParenthesisedExpression(AnchorSets.FIRST_SET_ELSE_STATEMENT.union(AnchorSets.FIRST_SET_STATEMENT)).getExpression();
        var body = parseBlock(AnchorSets.FIRST_SET_ELSE_STATEMENT.union(anchorSet));
        var next = this.stream.peek();
        if (next.is(TokenType.KEYWORD_ELSE)) {
            consume(false);
            var elseBody = parseBlock(anchorSet);
            return new IfStatement(Span.of(firstSpan, elseBody.getSpan()), condition, body, elseBody);
        }

        return new IfStatement(Span.of(firstSpan, body.getSpan()), condition, body, null);
    }

    private LoopStatement parseLoopStatement(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        consumeLineBreaks();

        // Infinite loop with no header
        if (!this.stream.peek().is(TokenType.L_PAREN)) {
            var body = parseBlock(anchorSet);
            return new LoopStatement(Span.of(firstSpan, body.getSpan()), Collections.emptyList(), null, body, null);
        }

        // L_PAREN
        consume(false);
        consumeLineBreaks();

        var variableDeclarations = new ArrayList<VariableDeclaration>();
        Expression condition = null;
        Statement advance = null;

        var hasCondition = true;
        while (true) {
            if (this.stream.peek().is(TokenType.COMMA)) {
                consume(false);
                hasCondition = true;
            }
            consumeLineBreaks();

            if (this.stream.peek().is(TokenType.KEYWORD_LET))
                variableDeclarations.add(parseVariableDeclaration(anchorSet.union(AnchorSets.END_SET_PARENTHESISED_LIST)));
            else
                break;

            hasCondition = false;
            consumeLineBreaks();
        }

        if (hasCondition)
            condition = parseExpression(anchorSet.union(AnchorSets.END_SET_PARENTHESISED_LIST));
        if (hasCondition && this.stream.peek().is(TokenType.COMMA)) {
            consume(false);
            // Well, this will make for some interesting code
            advance = parseStatement(anchorSet.union(AnchorSets.END_SET_PARENTHESISED_EXPRESSION));
        }

        expectToken(TokenType.R_PAREN, anchorSet, () -> "Expected closing ')'");
        consumeLineBreaks();

        var body = parseBlock(anchorSet);
        return new LoopStatement(Span.of(firstSpan, body.getSpan()), variableDeclarations, condition, body, advance);
    }

    private BlockStatement parseBlock(AnchorUnion anchorSet) {
        if (this.stream.peek().is(TokenType.L_CURLY_PAREN)) {
            var firstSpan = consume(false).span();
            var lastSpan = firstSpan;
            consumeLineBreaks();
            var statements = parseStatementList(AnchorSets.END_SET_BLOCK.union(AnchorSets.FIRST_SET_STATEMENT), TokenType.R_CURLY_PAREN);
            lastSpan = statements.getSpanOrElse(lastSpan);

            consumeLineBreaks();
            var rCurlyToken = expectToken(TokenType.R_CURLY_PAREN, anchorSet, () -> "Expected closing '}'");
            if (!rCurlyToken.isInvalid())
                lastSpan = rCurlyToken.span();

            consumeLineBreaks();
            return new BlockStatement(Span.of(firstSpan, lastSpan), statements);
        } else {
            var list = new StatementList();
            consumeLineBreaks();
            list.add(parseStatement(anchorSet));
            consumeLineBreaks();
            return new BlockStatement(list.get(0).getSpan(), list);
        }
    }

    private VariableDeclaration parseVariableDeclaration(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        consumeLineBreaks();
        var name = expectIdentifier(anchorSet.union(AnchorSets.FIRST_SET_LET_STATEMENT), () -> "Expected variable name");
        consumeLineBreaks();

        TypeExpression type = null;
        if (this.stream.peek().is(TokenType.COLON)) {
            consume(false);
            consumeLineBreaks();
            type = parseType(anchorSet.union(AnchorSets.MIDDLE_SET_LET_STATEMENT));
            consumeLineBreaks();
        }

        var equalToken = expectToken(TokenType.EQUAL, anchorSet.union(AnchorSets.FIRST_SET_EXPRESSION), () -> "Expected '=' after variable name");

        Expression initializer;
        if (!equalToken.isInvalid())
            initializer = parseExpression(anchorSet);
        else
            initializer = new GarbageExpression(name.span() == null ? firstSpan : name.span());

        return new VariableDeclaration(
                Span.of(firstSpan, initializer.getSpan()),
                type, new IdentifierExpression(name.span(), name.value()), initializer
        );
    }

    private FunctionDeclaration parseFunctionDeclaration(AnchorUnion anchorSet, boolean instanceMethod) {
        var firstSpan = consume(false).span();
        consumeLineBreaks();
        var functionName = expectIdentifier(anchorSet.union(AnchorSets.FIRST_SET_FUNCTION), () -> "Expected function name");
        consumeLineBreaks();

        // TODO: Maybe this could skip invalid tokens? Something like fn foo>() breaks completely without it
        var genericParameters = parseGenericParameterDeclarations(anchorSet.union(AnchorSets.FIRST_SET_EXPRESSION));

        consumeLineBreaks();
        expectToken(TokenType.L_PAREN, anchorSet.union(AnchorSets.FIRST_SET_EXPRESSION), () -> "Expected opening '(' after function name");

        var parameters = parseList(anchorSet.union(AnchorUnion.leaf(List.of(TokenType.COLON))), (a) -> {
            var nameToken = expectIdentifier(a, () -> "Expected parameter name");
            if (instanceMethod && "self".equals(nameToken.value()))
                this.problems.add(new Problem(nameToken.span(), "Parameter name 'self' is not allowed for instance methods"));

            consumeLineBreaks();
            var colonToken = expectToken(TokenType.COLON, a, () -> "Expected ':' after parameter name");
            consumeLineBreaks();
            var type = parseType(a);

            var paramFirstSpan = nameToken.isInvalid() ? colonToken.isInvalid() ? type.getSpan() == null ? null : type.getSpan() : colonToken.span() : nameToken.span();
            var paramLastSpan = type.getSpan() == null ? colonToken.isInvalid() ? nameToken.isInvalid() ? null : nameToken.span() : colonToken.span() : type.getSpan();
            return new FunctionDeclaration.ParameterDeclaration(paramFirstSpan == null ? null : Span.of(paramFirstSpan, paramLastSpan), type, new IdentifierExpression(nameToken.span(), nameToken.value()));
        }, ArrayListWithSpan::new, TokenType.R_PAREN);

        expectToken(TokenType.R_PAREN, anchorSet.union(AnchorSets.FIRST_SET_STATEMENT), () -> "Expected closing ')'");
        consumeLineBreaks();

        var returnType = this.stream.peek().is(TokenType.IDENTIFIER) ? parseType(anchorSet.union(AnchorSets.FIRST_SET_STATEMENT)) : null;
        var body = parseBlock(anchorSet);
        return new FunctionDeclaration(
                Span.of(firstSpan, body.getSpan()),
                new IdentifierExpression(functionName.span(), functionName.value()),
                genericParameters, parameters, returnType, body, instanceMethod
        );
    }

    private ListWithSpan<GenericParameterDeclaration> parseGenericParameterDeclarations(AnchorUnion anchorSet) {
        if (this.stream.peek().is(TokenType.LESS)) {
            var listStartSpan = consume(false).span();
            var genericParameters = parseList(anchorSet, (a) -> {
                var parameterName = expectIdentifier(a, () -> "Expected generic parameter name");
                return new GenericParameterDeclaration(parameterName.span(), parameterName.value());
            }, () -> new ArrayListWithSpan<>(2), TokenType.GREATER);
            var greaterToken = expectToken(TokenType.GREATER, anchorSet, () -> "Expected '>'");

            genericParameters.setSpan(Span.of(listStartSpan, greaterToken.isInvalid() ? genericParameters.getSpanOrElse(listStartSpan) : greaterToken.span()));
            consumeLineBreaks();
            return genericParameters;
        } else {
            return ArrayListWithSpan.emptyList();
        }
    }

    private <T, R extends List<T>> R parseList(
            AnchorUnion anchorSet,
            Function<AnchorUnion, T> expressionSupplier,
            Supplier<R> collector,
            TokenType endToken
    ) {
        anchorSet = anchorSet.union(AnchorUnion.leaf(List.of(endToken)));

        var list = collector.get();
        while (true) {
            var peek = this.stream.peek();
            if (peek.is(endToken) || peek.is(TokenType.EOF))
                break;

            if (!list.isEmpty())
                expectToken(TokenType.COMMA, anchorSet, () -> "Expected ',' or '" + endToken.getText() + "'");

            consumeLineBreaks();
            var tokensLeft = this.stream.tokensLeft();
            list.add(expressionSupplier.apply(anchorSet.union(AnchorSets.FIRST_SET_LIST)));
            // Prevent infinite loops
            if (tokensLeft == this.stream.tokensLeft() && !this.stream.peek().is(TokenType.COMMA))
                break;

            consumeLineBreaks();
        }
        return list;
    }

    private StructDeclaration parseStructDeclaration(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        consumeLineBreaks();
        var structName = expectIdentifier(anchorSet.union(AnchorSets.FIRST_SET_STRUCT), () -> "Expected struct name");
        consumeLineBreaks();

        var genericParameters = parseGenericParameterDeclarations(anchorSet.union(AnchorSets.FIRST_SET_STRUCT));

        consumeLineBreaks();
        // Deal-breaker. Code after this will be parsed normally
        if (!this.stream.peek().is(TokenType.L_CURLY_PAREN)) {
            return new StructDeclaration(
                    Span.of(firstSpan, genericParameters.getSpanOrElse(structName.isInvalid() ? firstSpan : structName.span())),
                    new IdentifierExpression(structName.span(), structName.value()),
                    genericParameters, Collections.emptyList(), Collections.emptyList()
            );
        }

        // L_CURLY_PAREN
        consume(false);
        consumeLineBreaks();

        var fields = new ArrayList<StructDeclaration.FieldDeclaration>();
        var functions = new ArrayList<FunctionDeclaration>();

        anchorSet = AnchorSets.MIDDLE_SET_STRUCT;

        Token currentToken;
        while (!(currentToken = this.stream.peek()).is(TokenType.EOF) && !currentToken.is(TokenType.R_CURLY_PAREN)) {
            if (currentToken.is(TokenType.IDENTIFIER)) {
                var name = consume(false);
                var lastSpan = name.span();
                var colonToken = expectToken(TokenType.COLON, anchorSet, () -> "Expected ':' after field name");
                if (!colonToken.isInvalid())
                    lastSpan = colonToken.span();

                var type = parseType(anchorSet);
                if (type.getSpan() != null)
                    lastSpan = type.getSpan();

                fields.add(new StructDeclaration.FieldDeclaration(Span.of(name.span(), lastSpan), type, new IdentifierExpression(name.span(), name.value()), fields.size()));
            } else if (currentToken.is(TokenType.KEYWORD_FN)) {
                var function = parseFunctionDeclaration(anchorSet, true);

                functions.add(function);
            } else {
                reportAndRecover(anchorSet, currentToken.span(), "Expected field or function declaration");
            }

            consumeLineBreaks();
        }

        var endSpan = expectToken(TokenType.R_CURLY_PAREN, anchorSet, () -> "Expected closing '}'").span();
        // Can only happen at EOF
        if (endSpan == null)
            endSpan = this.stream.peek().span();

        return new StructDeclaration(
                Span.of(firstSpan, endSpan),
                new IdentifierExpression(structName.span(), structName.value()),
                genericParameters,
                fields,
                functions
        );
    }

    private Expression parseExpression(AnchorUnion anchorSet) {
        anchorSet = anchorSet.union(AnchorSets.BINARY_EXPRESSION_OPERATORS);
        var head = parseUnaryExpression(anchorSet);
        var current = head;

        while (true) {
            var next = this.stream.peek();
            var operator = BinaryExpression.Operator.fromTokenType(next.type());
            if (operator == null)
                break;

            consume(false);

            if (current instanceof BinaryExpression old) {
                if (operator.getPrecedence() < old.getOperator().getPrecedence()) {
                    var newRight = new BinaryExpression(old.getRight(), parseUnaryExpression(anchorSet), operator);
                    old.setRight(newRight);
                    current = newRight;
                } else {
                    var newCurrent = new BinaryExpression(old.getLeft(), old.getRight(), old.getOperator());
                    old.setLeft(newCurrent);
                    old.setRight(parseUnaryExpression(anchorSet));
                    old.setOperator(operator);
                }
            } else { // Convert head into binary expression
                head = new BinaryExpression(head, parseUnaryExpression(anchorSet), operator);
                current = head;
            }

            if (operator == BinaryExpression.Operator.OP_ASSIGN && !(((BinaryExpression) current).getLeft() instanceof IAssignmentTarget))
                this.problems.add(new Problem(((BinaryExpression) current).getLeft().getSpan(), "Cannot assign to this expression"));
        }

        return head;
    }

    private Expression parseUnaryExpression(AnchorUnion set) {
        var token = this.stream.peek();
        var op = UnaryExpression.Operator.fromTokenType(token.type());
        if (op == null)
            return parsePostfixExpression(set);

        consume(false);
        var expr = parseUnaryExpression(set);
        return new UnaryExpression(Span.of(token.span(), expr.getSpan()), expr, op);
    }

    private Expression parsePostfixExpression(AnchorUnion set) {
        set.union(AnchorSets.FIRST_SET_POSTFIX_OP);

        var expr = parsePrimaryExpression(set);

        while (true) {
            var token = this.stream.peek();
            if (token.is(TokenType.L_PAREN) || token.is(TokenType.LESS_PIPE)) {
                expr = parseFunctionInvocation(set, expr);
            } else if (token.is(TokenType.DOT)) {
                expr = parseMemberAccessExpression(set, expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private ObjectCreationExpression parseObjectCreationExpression(AnchorUnion anchorSet) {
        var firstSpan = consume(false).span();
        var lastSpan = firstSpan;
        consumeLineBreaks();
        var nameToken = expectIdentifier(anchorSet, () -> "Expected type name");
        if (!nameToken.isInvalid())
            lastSpan = nameToken.span();

        consumeLineBreaks();

        var genericParameters = tryParseGenericParametersOnInvocation(anchorSet.union(AnchorSets.FIRST_SET_PARENTHESISED_EXPRESSION), TokenType.LESS);
        lastSpan = genericParameters.getSpanOrElse(lastSpan);

        var parameters = parseParameterList(anchorSet);
        lastSpan = parameters.getSpanOrElse(lastSpan);

        return new ObjectCreationExpression(
                Span.of(firstSpan, lastSpan),
                new IdentifierExpression(nameToken.span(), nameToken.value()),
                genericParameters, parameters
        );
    }

    /**
     * Literals, variable access, method calls
     */
    private Expression parsePrimaryExpression(AnchorUnion anchorSet) {
        consumeLineBreaks();
        Expression expr;

        var token = this.stream.peek();
        if (token.is(TokenType.L_PAREN)) {
            expr = parseParenthesisedExpression(anchorSet);
        } else if (token.is(TokenType.KEYWORD_NEW)) {
            expr = parseObjectCreationExpression(anchorSet);
        } else {
            expr = parseLiteralExpression(anchorSet);
        }

        consumeLineBreaks();
        return expr;
    }

    private Expression parseMemberAccessExpression(AnchorUnion anchorSet, Expression target) {
        consume(false);

        var lastSpan = this.stream.peek().span();
        var name = expectIdentifier(anchorSet, () -> "Expected member name after '.'");
        if (!name.isInvalid())
            lastSpan = name.span();

        return new MemberAccessExpression(Span.of(target.getSpan(), lastSpan), new IdentifierExpression(name.span(), name.value()), target);
    }

    private Expression parseFunctionInvocation(AnchorUnion anchorSet, Expression target) {
        var genericParameters = tryParseGenericParametersOnInvocation(anchorSet.union(AnchorSets.FIRST_SET_PARENTHESISED_EXPRESSION), TokenType.LESS_PIPE);
        var parameters = parseParameterList(anchorSet);

        return new FunctionInvocationExpression(Span.of(target.getSpan(), parameters.getSpanOrElse(target.getSpan())), target, genericParameters, parameters);
    }

    private ListWithSpan<TypeExpression> tryParseGenericParametersOnInvocation(AnchorUnion anchorSet, TokenType prefix) {
        if (this.stream.peek().is(prefix)) {
            var listStartSpan = consume(false).span();
            var genericParameters = parseList(anchorSet, this::parseType, ArrayListWithSpan::new, TokenType.GREATER);
            var greaterToken = expectToken(TokenType.GREATER, anchorSet, () -> "Expected '>'");

            genericParameters.setSpan(Span.of(listStartSpan, greaterToken.isInvalid() ? genericParameters.getSpanOrElse(listStartSpan) : greaterToken.span()));
            consumeLineBreaks();
            return genericParameters;
        } else {
            return ArrayListWithSpan.emptyList();
        }
    }

    private ExpressionList parseParameterList(AnchorUnion anchorSet) {
        // Parse the parameter list
        var lParenToken = expectToken(TokenType.L_PAREN, anchorSet.union(AnchorSets.FIRST_SET_EXPRESSION), () -> "Expected opening '('");
        var parameters = parseList(anchorSet, this::parseExpression, ExpressionList::new, TokenType.R_PAREN);
        var rParenToken = expectToken(TokenType.R_PAREN, anchorSet, () -> "Expected closing ')'");

        if (lParenToken.isInvalid() && rParenToken.isInvalid())
            return parameters;

        // Extend the span of the list
        if (lParenToken.isInvalid())
            parameters.setSpan(Span.of(parameters.getSpanOrElse(rParenToken.span()), rParenToken.span()));
        else if (rParenToken.isInvalid())
            parameters.setSpan(Span.of(lParenToken.span(), parameters.getSpanOrElse(lParenToken.span())));
        else
            parameters.setSpan(Span.of(lParenToken.span(), rParenToken.span()));

        return parameters;
    }

    private ParenthesisedExpression parseParenthesisedExpression(AnchorUnion anchorSet) {
        consumeLineBreaks();
        var firstToken = expectToken(TokenType.L_PAREN, anchorSet.union(AnchorSets.FIRST_SET_EXPRESSION), () -> "Expected opening '('");
        var parenExpr = parseExpression(anchorSet.union(AnchorSets.END_SET_PARENTHESISED_EXPRESSION));
        var secondToken = expectToken(TokenType.R_PAREN, anchorSet, () -> "Expected closing ')'");
        consumeLineBreaks();

        var firstSpan = firstToken.isInvalid() ? parenExpr.getSpan() : firstToken.span();
        var lastSpan = secondToken.isInvalid() ? parenExpr.getSpan() : secondToken.span();
        return new ParenthesisedExpression(Span.of(firstSpan, lastSpan), parenExpr);
    }

    private Expression parseLiteralExpression(AnchorUnion anchorSet) {
        var token = this.stream.peek();
        var span = token.span();

        return switch (token.type()) {
            case LONG_LITERAL -> new LongLiteralExpression(span, Long.parseLong(consume(false).value()));
            case DOUBLE_LITERAL -> new DoubleLiteralExpression(span, Double.parseDouble(consume(false).value()));
            case STRING_LITERAL -> parseStringLiteralExpression();
            case BOOLEAN_LITERAL -> new BooleanLiteralExpression(span, Boolean.parseBoolean(consume(false).value()));
            case IDENTIFIER -> new IdentifierExpression(span, consume(false).value());
            case L_SQUARE_BRACKET -> {
                consume(false);
                var lastSpan = span;

                var elements = parseList(anchorSet, this::parseExpression, ExpressionList::new, TokenType.R_SQUARE_BRACKET);
                lastSpan = elements.getSpanOrElse(lastSpan);

                var rSquareToken = expectToken(TokenType.R_SQUARE_BRACKET, anchorSet, () -> "Expected closing ']'");
                if (!rSquareToken.isInvalid())
                    lastSpan = rSquareToken.span();

                yield new ListLiteralExpression(Span.of(span, lastSpan), elements);
            }
            default -> {
                reportAndRecover(anchorSet, token.span(), "Expected an expression");
                //TODO: GarbageExpression should contain skipped tokens -> auto-completion?
                yield new GarbageExpression(new Span(token.span().source(), token.span().offset(), token.span().offset()));
            }
        };
    }

    private StringLiteralExpression parseStringLiteralExpression() {
        var parts = new ArrayList<StringLiteralExpression.Part>(1);
        while (this.stream.peek().is(TokenType.STRING_LITERAL)) {
            var token = consume(false);
            parts.add(StringLiteralExpression.stringPart(token.span(), token.value()));

            if (this.stream.peek().is(TokenType.STRING_LITERAL_CODE_START)) {
                consume(false);
                parts.add(StringLiteralExpression.expressionPart(parseExpression(AnchorSets.END_SET_STRING_CODE_LITERAL)));
                if (!this.stream.peek().is(TokenType.STRING_LITERAL_CODE_END))
                    break;

                consume(false);
            } else {
                break;
            }
        }

        return new StringLiteralExpression(parts);
    }

    private TypeExpression parseType(AnchorUnion anchorSet) {
        var firstSpan = this.stream.peek().span();
        var current = expectIdentifier(anchorSet, () -> "Expected a type name");
        if (current.isInvalid())
            return new TypeExpression(zeroWidthSpan(firstSpan), new IdentifierExpression(null, null));

        firstSpan = current.span();
        var secondSpan = firstSpan;

        List<TypeExpression> genericParameters;
        if (this.stream.peek().is(TokenType.LESS)) {
            var genericStartSpan = consume(false).span();
            genericParameters = parseList(anchorSet, this::parseType, ArrayList::new, TokenType.GREATER);
            if (genericParameters.isEmpty())
                this.problems.add(new Problem(genericStartSpan, "Empty generic parameter list"));

            var greaterToken = expectToken(TokenType.GREATER, anchorSet, () -> "Expected '>'");
            if (!greaterToken.isInvalid())
                secondSpan = greaterToken.span();

            consumeLineBreaks();
        } else {
            genericParameters = Collections.emptyList();
        }

        return new TypeExpression(Span.of(firstSpan, secondSpan), new IdentifierExpression(current.span(), current.value()), genericParameters);
    }

    private Token expectIdentifier(AnchorUnion anchorSet, Supplier<String> messageSupplier) {
        return expectToken(TokenType.IDENTIFIER, anchorSet, messageSupplier);
    }

    private Token expectToken(TokenType expectedType, AnchorUnion anchorSet, Supplier<String> messageSupplier) {
        var token = this.stream.peek();
        if (token.is(expectedType))
            return consume(false);

        reportAndRecover(anchorSet, token.span(), messageSupplier.get());
        return Token.INVALID;
    }

    private void reportAndRecover(AnchorUnion anchorSet, Span span, String message) {
        if (!this.suppressErrors)
            this.problems.add(new Problem(span, message));

        // Prevents consecutive errors caused by multiple recoveries without consuming any tokens
        this.suppressErrors = true;

        // Skip until next anchor
        Token current;
        while (!(current = this.stream.peek()).is(TokenType.EOF) && !anchorSet.contains(current.type()))
            consume(true);
    }

    private Token consume(boolean recovering) {
        // We consumed something valid, let's allow errors again
        if (!recovering)
            this.suppressErrors = false;

        return this.stream.consume();
    }

    private static Span zeroWidthSpan(Span span) {
        return new Span(span.source(), span.offset(), span.offset());
    }

    public static ParserResult parse(ISource source) {
        var tokenizerResult = Tokenizer.tokenize(CharStream.fromSource(source));
        return new Parser(tokenizerResult).parse();
    }

    public static ParserResult parse(TokenStream tokenStream) {
        return new Parser(tokenStream).parse();
    }

    public static ParserResult parse(TokenizerResult result) {
        return new Parser(result).parse();
    }

    public static List<ParserResult> parse(List<? extends ISource> sources) {
        return sources.stream().map(Parser::parse).toList();
    }

    /**
     * Only useful when files are very big, as the parser is currently very fast and therefore outperforms the cost of
     * multi-threading.
     *
     * @param sources The sources to parse
     * @return The parser results
     */
    public static List<ParserResult> parseParallel(List<? extends ISource> sources) {
        return sources.stream().map(Parser::parse).toList();
    }
}
