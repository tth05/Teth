package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecodeInterpreter.Interpreter;
import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.parser.Parser;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(
        name = "run",
        description = "Runs the given teth file",
        synopsisHeading = "@|bold,underline Usage|@:%n  ",
        descriptionHeading = "@|bold,underline Description|@:%n  ",
        parameterListHeading = "@|bold,underline Parameters|@:%n",
        optionListHeading = "@|bold,underline Options|@:%n"
)
public class RunCommand implements Runnable {

    @CommandLine.Parameters(
            paramLabel = "<path>",
            description = "The file to run",
            converter = {String2ExistingFileConverter.class}
    )
    private Path filePath;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "Verbose execution mode"
    )
    private boolean verbose;

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();
            var parserResult = Parser.fromString(Files.readString(this.filePath));
            if (parserResult.logProblems(System.out, true))
                return;

            var compiler = new Compiler();
            compiler.setMainUnit(parserResult.getUnit());
            var compilationResult = compiler.compile();
            if (compilationResult.logProblems(System.out, true))
                return;

            var interpreter = new Interpreter(compilationResult.getProgram());
            interpreter.execute();

            if (this.verbose) {
                System.out.println("Total time: " + (System.nanoTime() - startTime) / 1000000.0 + "ms");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
