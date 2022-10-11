package com.github.tth05.tethintellijplugin.run

import com.github.tth05.teth.bytecode.compiler.Compiler
import com.github.tth05.teth.bytecodeInterpreter.Interpreter
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.source.InMemorySource
import com.github.tth05.tethintellijplugin.util.ReceivingInputStream
import com.github.tth05.tethintellijplugin.util.RedirectedOutputStream
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
import com.intellij.execution.process.AnsiEscapeDecoder.ColoredTextAcceptor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.toByteArray
import com.intellij.util.text.nullize
import com.jetbrains.rd.util.string.printToString
import org.jdom.Element
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import javax.swing.JComponent

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
            val parserResult = Parser.parse(InMemorySource("", psiFile.text))
            if (parserResult.hasProblems()) throw ExecutionException("File has problems")

            val compiler = Compiler()
            compiler.addSourceFileUnit(parserResult.unit)
            compiler.setEntryPoint(parserResult.unit)
            val compilationResult = compiler.compile()
            if (compilationResult.hasProblems()) throw ExecutionException("Compilation failed")

            val processHandler = MyProcessHandler(Interpreter(compilationResult.program))
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

private class MyProcessHandler(val interpreter: Interpreter) : ProcessHandler(), ColoredTextAcceptor {
    private val decoder = AnsiEscapeDecoder()
    private var stopped = false

    init {
        val stdPipe = RedirectedOutputStream(ProcessHandlerForwardingInputStream(this, ProcessOutputType.STDOUT))
        val errPipe = RedirectedOutputStream(ProcessHandlerForwardingInputStream(this, ProcessOutputType.STDERR))

        interpreter.setSystemOutStream(stdPipe)
        interpreter.setSystemErrStream(errPipe)

        addProcessListener(object : ProcessAdapter() {
            override fun startNotified(event: ProcessEvent) {
                // If the output doesn't flush itself by overflowing, we do it here periodically
                AppExecutorUtil.getAppExecutorService().submit {
                    while (!stopped) {
                        stdPipe.flush()
                        errPipe.flush()
                        Thread.sleep(20)
                    }
                }
            }

            override fun processTerminated(event: ProcessEvent) {
                stdPipe.flush()
                errPipe.flush()
                removeProcessListener(this)
            }
        })
    }

    override fun startNotify() {
        notifyTextAvailable("Running!\n\n", ProcessOutputTypes.SYSTEM)

        super.startNotify()

        // Start the execution
        AppExecutorUtil.getAppExecutorService().submit {
            try {
                interpreter.execute()
            } catch (e: Throwable) {
                notifyTextAvailable(e.printToString(), ProcessOutputType.STDERR)
            } finally {
                stopped = true
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
        if (interpreter.isRunning) {
            interpreter.kill()
            notifyProcessTerminated(-1)
        } else {
            notifyProcessTerminated(0)
        }
    }

    override fun detachProcessImpl() = throw NotImplementedError()

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null
}

class ProcessHandlerForwardingInputStream(private val processHandler: ProcessHandler, private val outputType: Key<*>) :
    ReceivingInputStream() {

    private val buffer = ByteBuffer.allocate(32768)

    override fun receive(data: Int) {
        receive(byteArrayOf(data.toByte()))
    }

    override fun receive(data: ByteArray) {
        receive(data, 0, data.size)
    }

    override fun receive(data: ByteArray, off: Int, len: Int) {
        synchronized(buffer) {
            if (len > buffer.capacity()) {
                sendText(data)
                return
            }

            if (len > buffer.remaining())
                flush()
            buffer.put(data, off, len)
        }
    }

    override fun flush() {
        synchronized(buffer) {
            if (buffer.position() <= 0) return

            buffer.flip()
            val data = buffer.toByteArray()
            sendText(data)
            buffer.clear()
        }
    }

    private fun sendText(data: ByteArray) {
        processHandler.notifyTextAvailable(String(data, StandardCharsets.UTF_8), outputType)
    }
}