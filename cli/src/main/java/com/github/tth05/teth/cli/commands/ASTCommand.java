package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.cli.commands.converters.String2ExistingFileConverter;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.source.FileSource;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

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

    @CommandLine.Option(
            names = {"-w", "--watch"},
            description = "Watches the given file for changes and dumps the AST on every change"
    )
    private boolean watch;

    private int linesToClear;

    @Override
    public void run() {
        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            printAST();

            if (!this.watch)
                return;

            this.filePath.getParent().register(service, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                var key = service.take();
                for (var event : key.pollEvents()) {
                    if (event.context() == null || event.kind() == StandardWatchEventKinds.OVERFLOW)
                        continue;

                    var fileName = ((Path) event.context()).getFileName().toString();
                    if (fileName.endsWith("~"))
                        fileName = fileName.substring(0, fileName.length() - 1);
                    if (!fileName.equals(this.filePath.getFileName().toString()))
                        continue;
                    if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
                        break;

                    printAST();
                }

                key.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ignored) {
        }
    }

    private void printAST() throws IOException {
        if (this.watch) {
            var builder = Ansi.ansi();
            for (int i = 0; i < this.linesToClear; i++) {
                builder.cursorUpLine();
                builder.eraseLine();
            }
            System.out.print(builder);
        }

        var parserResult = Parser.parse(new FileSource(this.filePath));
        var writer = new StringWriter();
        parserResult.logProblems(new PrintWriter(writer), true);

        var errorStr = writer.toString();
        this.linesToClear = errorStr.isEmpty() ? 0 : errorStr.split("\n").length;
        System.out.print(errorStr);

        var str = parserResult.getUnit().dumpToString();
        this.linesToClear += str.split("\n").length;
        System.out.println(str);
    }
}
