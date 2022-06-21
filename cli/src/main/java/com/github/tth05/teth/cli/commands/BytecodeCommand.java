package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecode.decoder.IInstrunction;
import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.parser.Parser;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(
        name = "bc",
        description = "Dumps the generated bytecode for a given file",
        synopsisHeading = "@|bold,underline Usage|@:%n  ",
        descriptionHeading = "@|bold,underline Description|@:%n  ",
        parameterListHeading = "@|bold,underline Parameters|@:%n",
        optionListHeading = "@|bold,underline Options|@:%n"
)
public class BytecodeCommand implements Runnable {

    @CommandLine.Parameters(
            paramLabel = "<path>",
            description = "The file path to dump",
            converter = {String2ExistingFileConverter.class}
    )
    private Path filePath;

    @Override
    public void run() {
        try {
            var parserResult = Parser.fromString(Files.readString(this.filePath));
            if (parserResult.logProblems(System.out, true))
                return;

            var compiler = new Compiler();
            compiler.setMainUnit(parserResult.getUnit());
            var compilationResult = compiler.compile();
            if (compilationResult.logProblems(System.out, true))
                return;

            for (IInstrunction instruction : compilationResult.getInstructions()) {
                // TODO: Better debug printing
                System.out.println(instruction.getOpCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}