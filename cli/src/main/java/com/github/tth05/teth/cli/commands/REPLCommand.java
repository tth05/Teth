package com.github.tth05.teth.cli.commands;

import com.github.tth05.teth.repl.REPL;
import picocli.CommandLine;

@CommandLine.Command(
        name = "repl",
        description = "Starts a repl",
        synopsisHeading = "@|bold,underline Usage|@:%n  ",
        descriptionHeading = "@|bold,underline Description|@:%n  ",
        parameterListHeading = "@|bold,underline Parameters|@:%n",
        optionListHeading = "@|bold,underline Options|@:%n"
)
public class REPLCommand implements Runnable {

    @Override
    public void run() {
        new REPL(System.in, System.out).run();
    }
}
