package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.bytecodeInterpreter.Interpreter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "run",
        description = "Runs the given teth file",
        synopsisHeading = "@|bold,underline Usage|@:%n  ",
        descriptionHeading = "@|bold,underline Description|@:%n  ",
        parameterListHeading = "@|bold,underline Parameters|@:%n",
        optionListHeading = "@|bold,underline Options|@:%n"
)
public class RunCommand extends AbstractCompilerCommand {

    @Override
    public void run(TethProgram program) {
        var startTime = System.nanoTime();
        var interpreter = new Interpreter(program);
        interpreter.execute();

        if (this.verbose) {
            System.out.println("Ran in " + (System.nanoTime() - startTime) / 1000000.0 + "ms");
        }
    }
}
