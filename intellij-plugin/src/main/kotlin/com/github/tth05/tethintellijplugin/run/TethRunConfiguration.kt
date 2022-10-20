package com.github.tth05.tethintellijplugin.run

import com.github.tth05.teth.bytecode.compiler.Compiler
import com.github.tth05.teth.bytecodeInterpreter.Interpreter
import com.github.tth05.tethintellijplugin.syntax.IntellijModuleLoader
import com.github.tth05.tethintellijplugin.syntax.findPsiFileByPath
import com.github.tth05.tethintellijplugin.syntax.parse
import com.intellij.execution.CantRunException
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.text.nullize
import org.jdom.Element

class TethRunConfiguration(
    project: Project,
    factory: TethConfigurationFactory,
) : LocatableConfigurationBase<RunConfigurationOptions>(project, factory, "Teth Run Config") {
    var filePath: String? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RunProfileState { _, _ ->
            if (filePath == null) throw CantRunException("File path is not set")

            val parserResult =
                parse(project.findPsiFileByPath(filePath!!) ?: throw CantRunException("File not found"))
            if (parserResult.hasProblems()) throw CantRunException("File has problems")

            val compiler = Compiler()
            compiler.setEntryPoint(parserResult.unit)
            compiler.setModuleLoader(IntellijModuleLoader(project))
            val compilationResult = compiler.compile()
            if (compilationResult.hasProblems()) throw CantRunException("Compilation failed")

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