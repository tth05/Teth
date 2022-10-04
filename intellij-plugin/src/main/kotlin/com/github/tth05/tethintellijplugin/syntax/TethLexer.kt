package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.lang.lexer.Token
import com.github.tth05.teth.lang.lexer.TokenType
import com.github.tth05.teth.lang.lexer.Tokenizer
import com.github.tth05.teth.lang.stream.CharStream
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes.of
import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class TethLexer : LexerBase() {

    private var buffer: CharSequence? = null
    private var bufferEnd = 0
    private var tokens: MutableList<Token>? = null
    private var index = 0
    private var currentToken: Token? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferEnd = endOffset
        this.index = -1

        if (endOffset - startOffset <= 0 || buffer.isEmpty()) return

        // This just tokenizes the whole file and discards any unwanted tokens. Not efficient, but fast enough
        val result = Tokenizer.tokenize(CharStream.fromChars(buffer.toString().toCharArray()))
        this.tokens = result.tokenStream.tokens
            .filter { t: Token -> t.span().offset() >= startOffset && t.span().offsetEnd() <= endOffset }
            .toMutableList()

        if (this.tokens!!.lastOrNull()?.type() == TokenType.EOF) tokens!!.removeLast()

        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = if (currentToken == null) null else of(currentToken!!.type())

    override fun getTokenStart(): Int = currentToken!!.span().offset()

    override fun getTokenEnd(): Int = currentToken!!.span().offsetEnd()

    override fun advance() {
        index++
        currentToken = if (index >= tokens!!.size) null else tokens!![index]
    }

    override fun getBufferEnd(): Int = bufferEnd

    override fun getBufferSequence(): CharSequence = buffer!!
}