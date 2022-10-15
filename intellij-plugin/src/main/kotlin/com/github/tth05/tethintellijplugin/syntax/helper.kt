package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.analyzer.Analyzer
import com.github.tth05.teth.analyzer.AnalyzerResult
import com.github.tth05.teth.analyzer.module.IModuleLoader
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.parser.ParserResult
import com.github.tth05.teth.lang.parser.SourceFileUnit
import com.github.tth05.teth.lang.source.InMemorySource
import com.intellij.execution.ExecutionException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.nio.file.Paths

data class CachedAnalyzerState(
    val analyzer: Analyzer,
    val analyzerResults: List<AnalyzerResult>,
    val parserResult: ParserResult
)

fun analyzeAndParseFile(file: PsiFile): CachedAnalyzerState {
    val parserResult = parse(file)

    val analyzer = Analyzer(parserResult.unit)
    analyzer.setAnalyzeEntryPointOnly(true)
    analyzer.setModuleLoader(IntellijModuleLoader(file.project))
    return CachedAnalyzerState(analyzer, analyzer.analyze(), parserResult)
}

fun parse(file: PsiFile): ParserResult =
    Parser.parse(InMemorySource(FileUtil.toSystemIndependentName(file.originalFile.virtualFile.path), file.text))

class IntellijModuleLoader(val project: Project) : IModuleLoader {

    override fun toUniquePath(relativeToUniquePath: String, path: String): String {
        return try {
            val nioPath = Paths.get(relativeToUniquePath)
            FileUtil.toSystemIndependentName(nioPath.parent.resolve("$path.teth").normalize().toAbsolutePath().toString())
        } catch (e: Throwable) {
            e.printStackTrace()
            ""
        }
    }
    override fun loadModule(uniquePath: String): SourceFileUnit? {
        val file = LocalFileSystem.getInstance().findFileByPath(uniquePath) ?: return null
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
        return parse(psiFile).unit
    }
}
