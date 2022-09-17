package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.source.FileSource;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "ast",
        description = "Dumps the Abstract Syntax Tree for a given file",
        synopsisHeading = "@|bold,underline Usage|@:%n  ",
        descriptionHeading = "@|bold,underline Description|@:%n  ",
        parameterListHeading = "@|bold,underline Parameters|@:%n",
        optionListHeading = "@|bold,underline Options|@:%n"
)
public class ASTCommand implements Runnable {

    @CommandLine.Parameters(
            paramLabel = "<path>",
            description = "The file path to dump",
            converter = {String2ExistingFileConverter.class}
    )
    private Path filePath;

    @Override
    public void run() {
        try {
            var parserResult = Parser.parse(new FileSource(this.filePath.getParent(), this.filePath));
            parserResult.logProblems(System.out, true);
            System.out.println(parserResult.getUnit().dumpToString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
