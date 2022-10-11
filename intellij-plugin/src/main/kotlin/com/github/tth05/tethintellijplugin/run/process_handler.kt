package com.github.tth05.tethintellijplugin.run

import com.github.tth05.teth.bytecodeInterpreter.Interpreter
import com.github.tth05.tethintellijplugin.util.ReceivingInputStream
import com.github.tth05.tethintellijplugin.util.RedirectedOutputStream
import com.intellij.execution.process.*
import com.intellij.openapi.util.Key
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.toByteArray
import com.jetbrains.rd.util.string.printToString
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class InterpreterProcessHandler(private val interpreter: Interpreter) : ProcessHandler(), AnsiEscapeDecoder.ColoredTextAcceptor {
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