package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.interpreter.Interpreter;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.stream.CharStream;
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

    @Override
    public void run() {
        try {
            var ast = Parser.from(Tokenizer.streamOf(CharStream.fromString(Files.readString(this.filePath))));
            new Interpreter().execute(ast);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
