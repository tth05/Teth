package com.github.tth05.teth.repl;

import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecodeInterpreter.Interpreter;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.ParserResult;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.ITopLevelDeclaration;
import com.github.tth05.teth.lang.parser.ast.VariableDeclaration;
import com.github.tth05.teth.lang.source.InMemorySource;
import org.fusesource.jansi.Ansi;

import java.io.*;
import java.nio.charset.StandardCharsets;

// TODO:
//  - Save variable decls along with type or resolved type as string
//  - Add custom insn after N saved STORE_LOCAL insns
//  - Add insn handler for custom insn which loads variable decl values (like breakpoint)
//  - After run, backup local var values
public class REPL implements Runnable {

    private final BufferedReader in;
    private final OutputStreamWriter outWriter;
    private final boolean useAnsiColors;
    private final OutputStream outStream;

    private final StatementList persistentStatements = new StatementList();

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

                var parserResult = Parser.parse(new InMemorySource("repl", line));
                if (parserResult.hasProblems()) {
                    var problem = parserResult.getProblems().get(0);
                    flushColoredLine("    " + " ".repeat(problem.span().offset()) + "^ " + problem.message() + "\n", 255, 0, 0);
                    continue;
                }

                var compiler = new Compiler();
                compiler.setEntryPoint(new SourceFileUnit("repl", createStatementList(parserResult)));
                var compilerResult = compiler.compile();
                if (compilerResult.hasProblems()) {
                    var problem = compilerResult.getAnalyzerResults().get(0).getProblems().get(0);
                    flushColoredLine("    " + " ".repeat(problem.span().offset()) + "^ " + problem.message() + "\n", 255, 0, 0);
                    continue;
                }

                parserResult.getUnit().getStatements().stream()
                        .filter(s -> s instanceof ITopLevelDeclaration)
                        .forEach(s -> {
                            this.persistentStatements.add(s);
                        });

                var interpreter = new Interpreter(compilerResult.getProgram());
                interpreter.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StatementList createStatementList(ParserResult parserResult) {
        var statements = new StatementList(this.persistentStatements);
        statements.addAll(parserResult.getUnit().getStatements());
        return statements;
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
}
