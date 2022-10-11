package com.github.tth05.tethintellijplugin.util

import java.io.InputStream
import java.io.OutputStream

class RedirectedOutputStream(val target: ReceivingInputStream) : OutputStream() {
    override fun write(b: Int) {
        target.receive(b)
    }

    override fun write(b: ByteArray) {
        target.receive(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        target.receive(b, off, len)
    }

    override fun flush() {
        target.flush()
    }
}

abstract class ReceivingInputStream() : InputStream() {
    override fun read(): Int = throw NotImplementedError()

    abstract fun receive(data: Int)
    abstract fun receive(data: ByteArray)
    abstract fun receive(data: ByteArray, off: Int, len: Int)

    open fun flush() {}
}