package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.source.FileSource;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractCompilerCommand implements Runnable {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(
            paramLabel = "<path>",
            description = "The file to run",
            converter = {String2ExistingFileConverter.class}
    )
    protected Path filePath;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "Verbose execution mode"
    )
    protected boolean verbose;

    @CommandLine.Option(
            names = {"-b", "--basedir"},
            description = "The base directory for module resolution",
            converter = {String2ExistingFileConverter.class}
    )
    protected Path baseDir;

    @Override
    public void run() {
        if (this.baseDir == null)
            this.baseDir = this.filePath.getParent();
        this.baseDir = this.baseDir.toAbsolutePath().normalize();

        if (!Files.isDirectory(this.baseDir))
            throw new CommandLine.ParameterException(this.spec.commandLine(), "Base directory is not a directory");
        if (!this.filePath.startsWith(this.baseDir))
            throw new CommandLine.ParameterException(this.spec.commandLine(), "File is not child of base directory");

        try {
            long startTime = System.nanoTime();
            var parserResult = Parser.parse(new FileSource(this.baseDir, this.filePath));
            if (parserResult.logProblems(System.out, true))
                return;

            var compiler = new Compiler();
            compiler.setMainUnit(parserResult.getUnit());
            var compilationResult = compiler.compile();

            if (this.verbose)
                System.out.println("Compiled in " + (System.nanoTime() - startTime) / 1000000.0 + "ms");
            if (compilationResult.logProblems(System.out, true))
                return;

            run(compilationResult.getProgram());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void run(TethProgram program);
}
