package com.github.tth05.teth.cli.commands.converters;

import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class String2ExistingFileConverter implements CommandLine.ITypeConverter<Path> {

    @Override
    public Path convert(String value) throws Exception {
        try {
            var path = Paths.get(value);
            if (!Files.exists(path))
                throw new Exception("File does not exist");

            return path;
        } catch (Throwable throwable) {
            throw new Exception("Cannot convert " + value + " to a path. " + throwable.getMessage());
        }
    }
}
