package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.diagnostics.Problem;
import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.parser.recovery.AnchorSet;
import com.github.tth05.teth.lang.parser.recovery.AnchorSets;
import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Parser {

    private final ProblemList problems = new ProblemList();
    private final TokenStream stream;

    private Parser(TokenStream stream) {
        this.stream = stream;
    }

    public ParserResult parse() {
        var unit = new SourceFileUnit(this.stream.getSource().getModuleName(), parseStatementList(AnchorSets.FIRST_SET_STATEMENT, TokenType.EOF));
        this.stream.consumeType(TokenType.EOF);

        if (this.problems.isEmpty())
            return new ParserResult(this.stream.getSource(), unit);
        else
            return new ParserResult(this.stream.getSource(), unit, this.problems);
    }

    private UseStatement parseUseStatement(AnchorSet anchorSet) {
        var firstSpan = this.stream.consume().span();
        var lastSpan = firstSpan;
        consumeLineBreaks();

        var path = new ArrayList<IdentifierExpression>(2);
        while (true) {
            var part = expectIdentifier(anchorSet.lazyUnion(AnchorSets.FIRST_SET_USE_STATEMENT), () -> "Expected use statement path part");
            if (!part.isInvalid())
                path.add(new IdentifierExpression((lastSpan = part.span()), part.value()));

            if (!this.stream.peek().is(TokenType.SLASH))
                break;
            lastSpan = this.stream.consume().span();
        }

        consumeLineBreaks();
        var temp = expectToken(TokenType.L_CURLY_PAREN, anchorSet.lazyUnion(AnchorSets.END_SET_USE_STATEMENT), () -> "Expected '{' after use statement");
        if (!temp.isInvalid())
            lastSpan = temp.span();

        var imports = new ArrayList<IdentifierExpression>(8);
        while (true) {
            consumeLineBreaks();
            var part = expectIdentifier(AnchorSets.END_SET_USE_STATEMENT.lazyUnion(AnchorSets.FIRST_SET_STATEMENT_EXPRESSIONLESS), () -> "Expected identifier");
            consumeLineBreaks();
            if (!part.isInvalid())
                imports.add(new IdentifierExpression((lastSpan = part.span()), part.value()));

            if (!this.stream.peek().is(TokenType.COMMA))
                break;
            lastSpan = this.stream.consume().span();
        }

        var endToken = expectToken(TokenType.R_CURLY_PAREN, anchorSet, () -> "Missing closing '}'");
        if (!endToken.isInvalid())
            lastSpan = endToken.span();

        consumeLineBreaks();
        return new UseStatement(Span.of(firstSpan, lastSpan), path, imports);
    }

    private StatementList parseStatementList(AnchorSet statementAnchorSet, TokenType endToken) {
        var statements = new StatementList();
        while (true) {
            consumeLineBreaks();

            var token = this.stream.peek();
            if (token.is(endToken) || token.is(TokenType.EOF))
                break;

            statements.add(parseStatement(statementAnchorSet));
        }

        return statements;
    }

    private void consumeLineBreaks() {
        while (this.stream.peek().is(TokenType.LINE_BREAK))
            this.stream.consume();
    }

    private Statement parseStatement(AnchorSet anchorSet) {
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
            case L_CURLY_PAREN -> parseBlock(anchorSet);
            default -> parseExpression(anchorSet); // Expression statement
        };
    }

    private Statement parseReturnStatement(AnchorSet anchorSet) {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD_RETURN).span();
        var next = this.stream.peek();
        var expression = next.is(TokenType.LINE_BREAK) || next.is(TokenType.L_CURLY_PAREN) || next.is(TokenType.R_CURLY_PAREN) ? null : parseExpression(anchorSet);
        return new ReturnStatement(Span.of(firstSpan, expression == null ? firstSpan : expression.getSpan()), expression);
    }

    private IfStatement parseIfStatement(AnchorSet anchorSet) {
        var firstSpan = this.stream.consume().span();
        var condition = parseParenthesisedExpression(AnchorSets.FIRST_SET_ELSE_STATEMENT.lazyUnion(AnchorSets.FIRST_SET_STATEMENT));
        var body = parseBlock(AnchorSets.FIRST_SET_ELSE_STATEMENT.lazyUnion(anchorSet));
        var next = this.stream.peek();
        if (next.is(TokenType.KEYWORD_ELSE)) {
            this.stream.consume();
            var elseBody = parseBlock(anchorSet);
            return new IfStatement(Span.of(firstSpan, elseBody.getSpan()), condition, body, elseBody);
        }

        return new IfStatement(Span.of(firstSpan, body.getSpan()), condition, body, null);
    }

    private LoopStatement parseLoopStatement(AnchorSet anchorSet) {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD_LOOP).span();
        consumeLineBreaks();

        // Infinite loop with no header
        if (!this.stream.peek().is(TokenType.L_PAREN)) {
            var body = parseBlock(anchorSet);
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
            variableDeclarations.add(parseVariableDeclaration(anchorSet));

            if (this.stream.peek().is(TokenType.COMMA)) {
                this.stream.consumeType(TokenType.COMMA);
                hasCondition = true;
            }
        }

        if (hasCondition)
            condition = parseExpression(anchorSet);
        if (hasCondition && this.stream.peek().is(TokenType.COMMA)) {
            this.stream.consumeType(TokenType.COMMA);
            // Well, this will make for some interesting code
            advance = parseStatement(anchorSet);
        }

        this.stream.consumeType(TokenType.R_PAREN);
        consumeLineBreaks();

        var body = parseBlock(anchorSet);
        return new LoopStatement(Span.of(firstSpan, body.getSpan()), variableDeclarations, condition, body, advance);
    }

    private BlockStatement parseBlock(AnchorSet anchorSet) {
        if (this.stream.peek().is(TokenType.L_CURLY_PAREN)) {
            var firstSpan = this.stream.consumeType(TokenType.L_CURLY_PAREN).span();
            consumeLineBreaks();
            var statements = parseStatementList(AnchorSets.END_SET_BLOCK.lazyUnion(AnchorSets.FIRST_SET_STATEMENT), TokenType.R_CURLY_PAREN);
            consumeLineBreaks();
            var secondSpan = this.stream.consumeType(TokenType.R_CURLY_PAREN).span();
            consumeLineBreaks();
            return new BlockStatement(Span.of(firstSpan, secondSpan), statements);
        } else {
            var list = new StatementList();
            consumeLineBreaks();
            list.add(parseStatement(anchorSet));
            consumeLineBreaks();
            return new BlockStatement(list.get(0).getSpan(), list);
        }
    }

    private VariableDeclaration parseVariableDeclaration(AnchorSet anchorSet) {
        // We know that a 'let' is here
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD_LET).span();
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
        var initializer = parseExpression(anchorSet);

        return new VariableDeclaration(
                Span.of(firstSpan, initializer.getSpan()),
                type, new IdentifierExpression(name.span(), name.value()), initializer
        );
    }

    private FunctionDeclaration parseFunctionDeclaration(AnchorSet anchorSet, boolean instanceMethod) {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD_FN).span();
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
        var body = parseBlock(anchorSet);
        return new FunctionDeclaration(
                Span.of(firstSpan, body.getSpan()),
                new IdentifierExpression(functionName.span(), functionName.value()),
                genericParameters, parameters, returnType, body, instanceMethod
        );
    }

    private List<GenericParameterDeclaration> parseGenericParameterDeclarations() {
        var genericParameters = Collections.<GenericParameterDeclaration>emptyList();
        if (this.stream.peek().is(TokenType.LESS)) {
            this.stream.consumeType(TokenType.LESS);
            genericParameters = parseList(() -> {
                var parameterName = this.stream.consumeType(TokenType.IDENTIFIER);
                consumeLineBreaks();
                return new GenericParameterDeclaration(parameterName.span(), parameterName.value());
            }, () -> new ArrayList<>(2), TokenType.GREATER);
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

    private StructDeclaration parseStructDeclaration(AnchorSet anchorSet) {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD_STRUCT).span();
        consumeLineBreaks();
        var structName = this.stream.consumeType(TokenType.IDENTIFIER);
        consumeLineBreaks();

        var genericParameters = parseGenericParameterDeclarations();

        consumeLineBreaks();
        this.stream.consumeType(TokenType.L_CURLY_PAREN);
        consumeLineBreaks();

        var fields = new ArrayList<StructDeclaration.FieldDeclaration>();
        var functions = new ArrayList<FunctionDeclaration>();

        //TODO: Move this to name analysis
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
            } else if (token.is(TokenType.KEYWORD_FN)) {
                var function = parseFunctionDeclaration(anchorSet, true);

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

    private Expression parseExpression(AnchorSet anchorSet) {
        anchorSet = anchorSet.lazyUnion(AnchorSets.BINARY_EXPRESSION_OPERATORS);
        var head = parseUnaryExpression(anchorSet);
        var current = head;

        while (true) {
            var next = this.stream.peek();
            var operator = BinaryExpression.Operator.fromTokenType(next.type());
            if (operator == null)
                break;

            this.stream.consume();

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

    private Expression parseUnaryExpression(AnchorSet set) {
        var token = this.stream.peek();
        var op = UnaryExpression.Operator.fromTokenType(token.type());
        if (op == null)
            return parsePostfixExpression(set);

        this.stream.consume();
        var expr = parseUnaryExpression(set);
        return new UnaryExpression(Span.of(token.span(), expr.getSpan()), expr, op);
    }

    private Expression parsePostfixExpression(AnchorSet set) {
        set.lazyUnion(AnchorSets.FIRST_SET_POSTFIX_OP);

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

    private ObjectCreationExpression parseObjectCreationExpression(AnchorSet anchorSet) {
        var firstSpan = this.stream.consumeType(TokenType.KEYWORD_NEW).span();
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
    private Expression parsePrimaryExpression(AnchorSet anchorSet) {
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

    private Expression parseMemberAccessExpression(AnchorSet anchorSet, Expression target) {
        this.stream.consume();

        var name = this.stream.consumeType(TokenType.IDENTIFIER);
        return new MemberAccessExpression(Span.of(target.getSpan(), name.span()), new IdentifierExpression(name.span(), name.value()), target);
    }

    private Expression parseFunctionInvocation(AnchorSet anchorSet, Expression target) {
        var genericParameters = tryParseGenericParametersOnInvocation(TokenType.LESS_PIPE);

        this.stream.consumeType(TokenType.L_PAREN);
        consumeLineBreaks();
        var parameters = parseParameterList();

        var secondSpan = this.stream.consumeType(TokenType.R_PAREN).span();
        return new FunctionInvocationExpression(Span.of(target.getSpan(), secondSpan), target, genericParameters, parameters);
    }

    private List<TypeExpression> tryParseGenericParametersOnInvocation(TokenType prefix) {
        if (this.stream.peek().is(prefix)) {
            this.stream.consume();
            var genericParameters = parseList(this::parseType, ArrayList::new, TokenType.GREATER);
            this.stream.consumeType(TokenType.GREATER);
            consumeLineBreaks();
            return genericParameters;
        } else {
            return Collections.emptyList();
        }
    }

    private ExpressionList parseParameterList() {
        return parseList(() -> parseExpression(AnchorSets.EMPTY), ExpressionList::new, TokenType.R_PAREN);
    }

    private Expression parseParenthesisedExpression(AnchorSet anchorSet) {
        consumeLineBreaks();
        expectToken(TokenType.L_PAREN, anchorSet.lazyUnion(AnchorSets.FIRST_SET_EXPRESSION), () -> "Expected opening '('");
        var parenExpr = parseExpression(anchorSet.lazyUnion(AnchorSets.END_SET_PARENTHESISED_EXPRESSION));
        expectToken(TokenType.R_PAREN, anchorSet, () -> "Expected closing ')'");
        consumeLineBreaks();
        return parenExpr;
    }

    private Expression parseLiteralExpression(AnchorSet anchorSet) {
        var token = this.stream.peek();
        var span = token.span();

        return switch (token.type()) {
            case LONG_LITERAL -> new LongLiteralExpression(span, Long.parseLong(this.stream.consume().value()));
            case DOUBLE_LITERAL -> new DoubleLiteralExpression(span, Double.parseDouble(this.stream.consume().value()));
            case STRING_LITERAL -> parseStringLiteralExpression(anchorSet);
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

                    elements.add(parseExpression(anchorSet));
                }

                var lastSpan = this.stream.consumeType(TokenType.R_SQUARE_BRACKET).span();
                yield new ListLiteralExpression(Span.of(span, lastSpan), elements);
            }
            default -> {
                reportAndRecover(anchorSet, token.span(), "Expected a literal");
                yield new GarbageExpression(token.span());
            }
        };
    }

    private StringLiteralExpression parseStringLiteralExpression(AnchorSet anchorSet) {
        var parts = new ArrayList<StringLiteralExpression.Part>(1);
        while (this.stream.peek().is(TokenType.STRING_LITERAL)) {
            var token = this.stream.consumeType(TokenType.STRING_LITERAL);
            parts.add(StringLiteralExpression.stringPart(token.span(), token.value()));

            if (this.stream.peek().is(TokenType.STRING_LITERAL_CODE_START)) {
                this.stream.consumeType(TokenType.STRING_LITERAL_CODE_START);
                parts.add(StringLiteralExpression.expressionPart(parseExpression(anchorSet)));
                this.stream.consumeType(TokenType.STRING_LITERAL_CODE_END);
            }
        }

        return new StringLiteralExpression(parts);
    }

    private TypeExpression parseType() {
        var current = this.stream.consume();
        if (!current.is(TokenType.IDENTIFIER))
            throw new UnexpectedTokenException(current.span(), "Expected a type");

        var firstSpan = current.span();
        var secondSpan = firstSpan;

        List<TypeExpression> genericParameters;
        if (this.stream.peek().is(TokenType.LESS)) {
            this.stream.consume();
            genericParameters = parseList(this::parseType, ArrayList::new, TokenType.GREATER);
            secondSpan = this.stream.consumeType(TokenType.GREATER).span();
            consumeLineBreaks();
        } else {
            genericParameters = Collections.emptyList();
        }

        return new TypeExpression(Span.of(firstSpan, secondSpan), current.value(), genericParameters);
    }

    private Token expectIdentifier(AnchorSet anchorSet, Supplier<String> messageSupplier) {
        return expectToken(TokenType.IDENTIFIER, anchorSet, messageSupplier);
    }

    private Token expectToken(TokenType expectedType, AnchorSet anchorSet, Supplier<String> messageSupplier) {
        var token = this.stream.peek();
        if (token.is(expectedType))
            return this.stream.consume();

        reportAndRecover(anchorSet, token.span(), messageSupplier.get());
        return Token.INVALID;
    }

    private void reportAndRecover(AnchorSet anchorSet, ISpan span, String message) {
        this.problems.add(new Problem(span, message));

        //TODO: Don't discard skipped tokens, auto-completion?
        // Skip until next anchor
        Token current;
        while (!(current = this.stream.peek()).is(TokenType.EOF) && !anchorSet.contains(current.type()))
            this.stream.consume();
    }

    public static ParserResult parse(ISource source) {
        var tokenizerResult = Tokenizer.tokenize(CharStream.fromSource(source));
        if (tokenizerResult.hasProblems())
            return new ParserResult(source, null, tokenizerResult.getProblems());
        return new Parser(tokenizerResult.getTokenStream()).parse();
    }

    public static ParserResult parse(TokenStream tokenStream) {
        return new Parser(tokenStream).parse();
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
