package com.github.tth05.teth.lang.source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileSource implements ISource {

    private final String moduleName;
    private final char[] contents;

    /**
     * @param path The path to the file
     */
    public FileSource(Path path) throws IOException {
        path = path.normalize().toAbsolutePath();
        this.moduleName = path.toString().replace('\\', '/');
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

    @Override
    public String toString() {
        return "FileSource{" +
               "moduleName='" + this.moduleName + '\'' +
               ", contentsLength=" + this.contents.length +
               ", contentsHash=" + Arrays.hashCode(this.contents) +
               '}';
    }
}
