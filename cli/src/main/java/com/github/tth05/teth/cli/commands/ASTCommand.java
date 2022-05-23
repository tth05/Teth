package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.stream.CharStream;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
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
            var tokenizerResult = Tokenizer.streamOf(CharStream.fromString(Files.readString(this.filePath)));
            if (tokenizerResult.logProblems())
                return;

            System.out.println(Parser.from(tokenizerResult.getTokenStream()).dumpToString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
