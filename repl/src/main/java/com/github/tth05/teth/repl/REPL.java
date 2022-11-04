package com.github.tth05.teth.repl;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.type.SemanticType;
import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecode.op.IInstrunction;
import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.bytecodeInterpreter.Interpreter;
import com.github.tth05.teth.lang.parser.*;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.source.InMemorySource;
import org.fusesource.jansi.Ansi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// TODO:
//  - Add null support to compiler and interpreter
public class REPL implements Runnable {

    private final BufferedReader in;
    private final OutputStreamWriter outWriter;
    private final boolean useAnsiColors;
    private final OutputStream outStream;

    private final StatementList persistentStatements = new StatementList();
    private final List<CachedLocalVariable> cachedLocalVariables = new ArrayList<>();

    public REPL(InputStream in, OutputStream out) {
        this(in, out, true);
    }

    public REPL(InputStream in, OutputStream out, boolean useAnsiColors) {
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.outStream = out;
        this.outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        this.useAnsiColors = useAnsiColors;
    }

    @Override
    public void run() {
        try {
            while (true) {
                flushLine(">>> ");

                var line = this.in.readLine();
                if (line == null)
                    return;
                if (line.indexOf(0) != -1) {
                    flushColoredLine("Ignoring because line contains EOF\n", 255, 0, 0);
                    continue;
                }

                // Parse
                var parserResult = Parser.parse(new InMemorySource("repl", line));
                if (parserResult.hasProblems()) {
                    var problem = parserResult.getProblems().get(0);
                    flushColoredLine("    " + " ".repeat(problem.span().offset()) + "^ " + problem.message() + "\n", 255, 0, 0);
                    continue;
                }

                // Compile
                var compiler = new Compiler();
                compiler.setEntryPoint(new SourceFileUnit("repl", createStatementList(parserResult)));
                var compilerResult = compiler.compile();
                if (compilerResult.hasProblems()) {
                    var problem = compilerResult.getAnalyzerResults().get(0).getProblems().get(0);
                    flushColoredLine("    " + " ".repeat(problem.span().offset()) + "^ " + problem.message() + "\n", 255, 0, 0);
                    continue;
                }

                var interpreter = new Interpreter(injectRestoreLocalsInsn(compilerResult.getProgram()));
                interpreter.addCustomInsnHandler(RestoreLocalsInsn.OPCODE, (instruction) -> {
                    for (int i = 0; i < this.cachedLocalVariables.size(); i++)
                        interpreter.storeLocal(i, this.cachedLocalVariables.get(i).value);
                });
                interpreter.execute();

                // Backup important statements
                parserResult.getUnit().getStatements().stream()
                        .filter(s -> s instanceof ITopLevelDeclaration || s instanceof VariableDeclaration)
                        .forEach(s -> {
                            if (s instanceof VariableDeclaration var) {
                                TypeExpression type;
                                if (var.getTypeExpr() != null)
                                    type = var.getTypeExpr();
                                else
                                    type = semanticTypeToExpression(compilerResult.getAnalyzer(), compilerResult.getAnalyzer().resolvedExpressionType(var.getInitializerExpr()));

                                this.cachedLocalVariables.add(new CachedLocalVariable(var.getNameExpr().getValue(), type));
                            } else {
                                this.persistentStatements.add(s);
                            }
                        });

                // Cache local variable values
                for (int i = 0; i < this.cachedLocalVariables.size(); i++) {
                    var cachedLocalVariable = this.cachedLocalVariables.get(i);
                    var value = interpreter.loadLocal(i);
                    if (value != null)
                        cachedLocalVariable.value = value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StatementList createStatementList(ParserResult parserResult) {
        var statements = new StatementList(this.persistentStatements);
        this.cachedLocalVariables.forEach((v) -> statements.add(new VariableDeclaration(null, v.type, new IdentifierExpression(null, v.name), v.createInitializerExpression())));
        var parsedStatements = parserResult.getUnit().getStatements();

        // Convert last expression to print statement
        var last = parsedStatements.get(parsedStatements.size() - 1);
        if (last instanceof Expression expr) {
            var shouldAddPrint = true;
            if (expr instanceof FunctionInvocationExpression invo) {
                var analyzer = new Analyzer(parserResult.getUnit());
                if (analyzer.analyze().get(0).hasProblems()) {
                    shouldAddPrint = false;
                } else {
                    // Functions that don't return anything should not be printed
                    if (((FunctionDeclaration) analyzer.resolvedReference((IDeclarationReference) invo.getTarget())).getReturnTypeExpr() == null)
                        shouldAddPrint = false;
                }
            }

            if (shouldAddPrint)
                parsedStatements.set(parsedStatements.size() - 1, new FunctionInvocationExpression(null, new IdentifierExpression(null, "print"), List.of(), ExpressionList.of(expr)));
        }

        statements.addAll(parsedStatements);
        return statements;
    }

    private TethProgram injectRestoreLocalsInsn(TethProgram program) {
        var insns = new ArrayList<>(List.of(program.getInstructions()));
        insns.add(/* INVOKE */ 1 + /* PUSH + STORE_LOCAL */ this.cachedLocalVariables.size() * 2, new RestoreLocalsInsn());
        return new TethProgram(insns.toArray(IInstrunction[]::new), program.getStructData());
    }

    private void flushLine(String line) throws IOException {
        this.outWriter.append(line);
        this.outWriter.flush();
    }

    private void flushColoredLine(String line, int r, int g, int b) throws IOException {
        if (this.useAnsiColors)
            flushLine(Ansi.ansi().fgRgb(r, g, b).a(line).reset().toString());
        else
            flushLine(line);
    }

    private static TypeExpression semanticTypeToExpression(Analyzer analyzer, SemanticType type) {
        if (!type.hasGenericBounds())
            return new TypeExpression(null, new IdentifierExpression(null, semanticTypeToName(analyzer, type)));
        return new TypeExpression(
                null,
                new IdentifierExpression(null, semanticTypeToName(analyzer, type)),
                type.getGenericBounds().stream().map(t -> semanticTypeToExpression(analyzer, t)).toList()
        );
    }

    private static String semanticTypeToName(Analyzer analyzer, SemanticType type) {
        return ((StructDeclaration) analyzer.getTypeCache().getDeclaration(type)).getNameExpr().getValue();
    }

    private static class CachedLocalVariable {

        private final String name;
        private final TypeExpression type;
        private Object value;

        private CachedLocalVariable(String name, TypeExpression type) {
            this.name = name;
            this.type = type;
        }

        public Expression createInitializerExpression() {
            var typeName = this.type.getNameExpr().getValue();
            return switch (typeName) {
                case "long" -> new LongLiteralExpression(null, 0);
                case "double" -> new DoubleLiteralExpression(null, 0);
                case "bool" -> new BooleanLiteralExpression(null, false);
                default -> new NullLiteralExpression(null);
            };
        }
    }

    private record RestoreLocalsInsn() implements IInstrunction {

        static final byte OPCODE = 127;

        @Override
        public byte getOpCode() {
            return OPCODE;
        }

        @Override
        public String getDebugParametersString() {
            return null;
        }
    }
}
