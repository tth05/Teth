package com.github.tth05.teth.lang.source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSource implements ISource {

    private final String moduleName;
    private final char[] contents;

    /**
     * @param root The root to which the module name will be relative to
     * @param path The path to the file
     */
    public FileSource(Path root, Path path) throws IOException {
        if (!root.isAbsolute())
            throw new IllegalArgumentException("Root must be absolute");
        if (!path.isAbsolute())
            throw new IllegalArgumentException("Path must be absolute");

        var relativePath = path.toString().substring(root.toString().length() + 1);
        var dotIndex = relativePath.lastIndexOf('.');
        this.moduleName = relativePath.substring(0, dotIndex == -1 ? relativePath.length() : dotIndex).replace('\\', '/');
        System.out.println(this.moduleName);

        this.contents = Files.readString(path).toCharArray();
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public char[] getContents() {
        return this.contents;
    }
}
