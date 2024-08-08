package dev.koifysh.randomizer.utils

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

object ZipUtils {

    private fun decompress(data: ByteArray): ByteArray {
        val infalter = Inflater()
        infalter.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        outputStream.use {
            while (!infalter.finished()) {
                val count = infalter.inflate(buffer)
                it.write(buffer, 0, count)
            }
        }
        return outputStream.toByteArray()
    }

    fun ByteArray.decompressToString(): String {
        try {
            val decompressedData = decompress(this)
            return String(decompressedData, Charsets.UTF_8)
        } catch (e: Exception) {
            return ""
        }
    }
}