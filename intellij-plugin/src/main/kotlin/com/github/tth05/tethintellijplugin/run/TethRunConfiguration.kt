package com.github.tth05.tethintellijplugin.run

import com.github.tth05.teth.bytecode.compiler.Compiler
import com.github.tth05.teth.bytecodeInterpreter.Interpreter
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.source.InMemorySource
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.*
import com.intellij.execution.process.AnsiEscapeDecoder.ColoredTextAcceptor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.BaseDataReader.SleepingPolicy
import com.intellij.util.io.BaseOutputReader
import com.jetbrains.rd.util.string.printToString
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.concurrent.Future
import javax.swing.JComponent

class TethRunConfiguration(
    project: Project,
    factory: TethConfigurationFactory,
) : LocatableConfigurationBase<RunConfigurationOptions>(project, factory, "Teth Run Config") {
    var filePath: String? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RunProfileState { ex, runner ->
            if (filePath == null) throw ExecutionException("No file configured")

            val file =
                LocalFileSystem.getInstance().findFileByNioFile(Paths.get(filePath!!)) ?: throw ExecutionException(
                    "File not found"
                )

            val psiFile = PsiManager.getInstance(project).findFile(file) ?: throw ExecutionException("File not found")
            val parserResult = Parser.parse(InMemorySource("", psiFile.text))
            if (parserResult.hasProblems()) throw ExecutionException("File has problems")

            val compiler = Compiler()
            compiler.addSourceFileUnit(parserResult.unit)
            compiler.setEntryPoint(parserResult.unit)
            val compilationResult = compiler.compile()
            if (compilationResult.hasProblems()) throw ExecutionException("Compilation failed")

            val interpreter = Interpreter(compilationResult.program)

            val processHandler = MyProcessHandler(interpreter)
            ProcessTerminatedListener.attach(processHandler)

            val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            console.attachToProcess(processHandler)
            return@RunProfileState DefaultExecutionResult(console, processHandler)
        }
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<TethRunConfiguration>() {
            override fun resetEditorFrom(s: TethRunConfiguration) {
            }

            override fun applyEditorTo(s: TethRunConfiguration) {
            }

            override fun createEditor(): JComponent = panel { }
        }
    }
}

private class MyProcessHandler(val interpreter: Interpreter) : ProcessHandler(), ColoredTextAcceptor {
    private val decoder = AnsiEscapeDecoder()

    private val stdOutPipe: OutputStream
    private val errOutPipe: OutputStream
    private val stdInPipe: InputStream
    private val errInPipe: InputStream

    private var hasStarted = false

    init {
        stdOutPipe = PipedOutputStream()
        errOutPipe = PipedOutputStream()
        stdInPipe = PipedInputStream(stdOutPipe, 16384)
        errInPipe = PipedInputStream(errOutPipe, 16384)

        interpreter.setSystemOutStream(stdOutPipe)
        interpreter.setSystemErrStream(errOutPipe)

        addProcessListener(object : ProcessAdapter() {
            var stdReader: BaseOutputReader? = null
            var errReader: BaseOutputReader? = null

            override fun startNotified(event: ProcessEvent) {
                stdReader = MyOutputReader(stdInPipe, stdOutPipe, ProcessOutputTypes.STDOUT, this@MyProcessHandler)
                errReader = MyOutputReader(errInPipe, errOutPipe, ProcessOutputTypes.STDERR, this@MyProcessHandler)
            }

            override fun processTerminated(event: ProcessEvent) {
                while (!hasStarted) Thread.onSpinWait()
                stdReader?.stop()
                errReader?.stop()
                stdReader?.waitFor()
                errReader?.waitFor()
                removeProcessListener(this)
            }
        })
    }

    override fun startNotify() {
        notifyTextAvailable("Running!\n\n", ProcessOutputTypes.SYSTEM)

        super.startNotify()
        hasStarted = true

        // Start the execution
        AppExecutorUtil.getAppExecutorService().submit {
            try {
                interpreter.execute()
            } catch (e: Throwable) {
                errOutPipe.write(e.printToString().toByteArray())
            } finally {
                destroyProcess()
            }
        }
    }

    override fun notifyTextAvailable(text: String, outputType: Key<*>) {
        decoder.escapeText(text, outputType, this)
    }

    override fun coloredTextAvailable(text: String, attributes: Key<*>) {
        super.notifyTextAvailable(text, attributes)
    }

    override fun destroyProcessImpl() {
        if (interpreter.isRunning) interpreter.kill()
        notifyProcessTerminated(0)
    }

    override fun detachProcessImpl() = throw NotImplementedError()

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null
}

private class MyOutputReader(
    inputStream: InputStream,
    val outputStream: OutputStream,
    val outputType: Key<*>,
    val processHandler: ProcessHandler
) :
    BaseOutputReader(inputStream, StandardCharsets.UTF_8, object : Options() {
        override fun sendIncompleteLines(): Boolean = false
        override fun policy(): SleepingPolicy =
            SleepingPolicy {
                if (it) {
                    SleepingPolicy.sleepTimeWhenWasActive
                } else {
                    // Flush when we have to sleep
                    outputStream.flush()
                    SleepingPolicy.sleepTimeWhenIdle
                }
            }
    }) {
    init {
        start("Teth output reader")
    }

    override fun executeOnPooledThread(runnable: Runnable): Future<*> {
        return AppExecutorUtil.getAppExecutorService().submit(runnable)
    }

    override fun onTextAvailable(text: String) {
        processHandler.notifyTextAvailable(text, outputType)
    }
}