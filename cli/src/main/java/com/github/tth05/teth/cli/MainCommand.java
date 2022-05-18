package com.github.tth05.teth.cli;

import com.github.tth05.teth.cli.commands.ASTCommand;
import com.github.tth05.teth.cli.commands.RunCommand;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

@CommandLine.Command(
        name = "teth-cli",
        version = "teth-cli 0.0.1",
        subcommands = {ASTCommand.class, RunCommand.class},
        mixinStandardHelpOptions = true,
        synopsisHeading = "@|bold,underline Usage|@:%n  ",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        optionListHeading = "@|bold,underline Options|@:%n",
        commandListHeading = "@|bold,underline Commands|@:%n",
        synopsisSubcommandLabel = "[command] [<args>]"
)
public class MainCommand {

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        //noinspection InstantiationOfUtilityClass
        new CommandLine(new MainCommand()).execute(args);
    }
}
