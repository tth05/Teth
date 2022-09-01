package com.github.tth05.teth.lang.source;

public interface ISource {

    /**
     * A module name represents the path by which this module can be imported. For example {@code myapp/parser/utils}
     *
     * @return The module name
     */
    String getModuleName();

    char[] getContents();
}
