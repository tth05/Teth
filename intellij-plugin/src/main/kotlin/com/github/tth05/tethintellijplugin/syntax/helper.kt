package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.analyzer.Analyzer
import com.github.tth05.teth.analyzer.AnalyzerResult
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.parser.ParserResult
import com.github.tth05.teth.lang.source.InMemorySource
import com.intellij.psi.PsiFile

data class CachedAnalyzerState(
    val analyzer: Analyzer,
    val analyzerResults: List<AnalyzerResult>,
    val parserResult: ParserResult
)

fun analyzeAndParseFile(file: PsiFile): CachedAnalyzerState {
    val parserResult = Parser.parse(InMemorySource("", file.text))

    val analyzer = Analyzer(parserResult.unit)
    analyzer.setAnalyzeEntryPointOnly(true)
    return CachedAnalyzerState(analyzer, analyzer.analyze(), parserResult)
}