package com.github.tth05.teth.lang;

import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;

public class AbstractParserTest extends AbstractTokenizerTest {

    protected SourceFileUnit unit;

    protected void createAST(String str) {
        super.createStreams(str);
        this.unit = Parser.from(this.tokenStream);
    }
}
