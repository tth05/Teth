package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.analyzer.module.IModuleLoader;
import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.source.FileSource;
import com.github.tth05.teth.lang.source.ISource;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractCompilerCommand implements Runnable {

    @CommandLine.Parameters(
            paramLabel = "<path>",
            description = "The file to run",
            converter = {String2ExistingFileConverter.class}
    )
    protected Path filePath;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "Print timings"
    )
    protected boolean verbose;

    @CommandLine.Option(
            names = {"-m", "--disable-modules"},
            description = "Disables module loading"
    )
    protected boolean disableModuleLoading;

    @Override
    public void run() {
        var startTime = System.nanoTime();
        try {
            var entryPointSource = new FileSource(this.filePath);
            var entryPointUnit = parseSource(entryPointSource);

            var compiler = new Compiler();
            compiler.setEntryPoint(entryPointUnit);
            if (!this.disableModuleLoading) {
                compiler.setModuleLoader(new IModuleLoader() {
                    @Override
                    public String toUniquePath(String relativeToUniquePath, String path) {
                        try {
                            var nioPath = Paths.get(relativeToUniquePath);
                            return nioPath.getParent().resolve(path + ".teth").normalize().toAbsolutePath().toString().replace('\\', '/');
                        } catch (Throwable e) {
                            e.printStackTrace();
                            return "";
                        }
                    }

                    @Override
                    public SourceFileUnit loadModule(String uniquePath) {
                        try {
                            return Parser.parse(new FileSource(Paths.get(uniquePath))).getUnit();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
            }

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

    private static SourceFileUnit parseSource(ISource entryPointSource) {
        var result = Parser.parse(entryPointSource);
        // Always log problems, compiler will be called even with invalid input to get analyzer results
        result.logProblems(System.out, true);
        return result.getUnit();
    }

    protected abstract void run(TethProgram program);
}
