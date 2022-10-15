package com.github.tth05.tethintellijplugin.run

import com.github.tth05.teth.bytecode.compiler.Compiler
import com.github.tth05.teth.bytecodeInterpreter.Interpreter
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.source.InMemorySource
import com.github.tth05.tethintellijplugin.syntax.IntellijModuleLoader
import com.github.tth05.tethintellijplugin.syntax.parse
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationWarning
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.util.text.nullize
import org.jdom.Element

class TethRunConfiguration(
    project: Project,
    factory: TethConfigurationFactory,
) : LocatableConfigurationBase<RunConfigurationOptions>(project, factory, "Teth Run Config") {
    var filePath: String? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RunProfileState { _, _ ->
            if (filePath == null) throw ExecutionException("File path is not set")

            val file =
                LocalFileSystem.getInstance().findFileByPath(filePath!!) ?: throw ExecutionException(
                    "File does not exist"
                )

            val psiFile =
                PsiManager.getInstance(project).findFile(file) ?: throw ExecutionException("PsiFile not found")
            val parserResult = parse(psiFile)
            if (parserResult.hasProblems()) throw ExecutionException("File has problems")

            val compiler = Compiler()
            compiler.setEntryPoint(parserResult.unit)
            compiler.setModuleLoader(IntellijModuleLoader(project))
            val compilationResult = compiler.compile()
            if (compilationResult.hasProblems()) throw ExecutionException("Compilation failed")

            val processHandler = InterpreterProcessHandler(Interpreter(compilationResult.program))
            ProcessTerminatedListener.attach(processHandler)

            val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            console.attachToProcess(processHandler)
            return@RunProfileState DefaultExecutionResult(console, processHandler)
        }
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute("filePath", filePath.orEmpty())
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        filePath = element.getAttributeValue("filePath").nullize()
    }

    override fun checkConfiguration() {
        if (filePath == null)
            throw RuntimeConfigurationError("File path is not set")
        val file = LocalFileSystem.getInstance().findFileByPath(filePath!!)
        if (file == null || !file.isValid)
            throw RuntimeConfigurationWarning("File does not exist")
        if (!file.name.endsWith(".teth"))
            throw RuntimeConfigurationWarning("File is not a teth file")
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return TethConfigurationEditor(project)
    }
}