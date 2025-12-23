package com.uyscuti.social.circuit.User_Interface.media

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType,
    private val callback: (Long, Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val source = file.source()
        var totalBytesRead: Long = 0
        var bytesRead: Long

        while (source.read(sink.buffer, SEGMENT_SIZE.toLong()).also { bytesRead = it } != -1L) {
            totalBytesRead += bytesRead
            sink.flush()
            callback(totalBytesRead, file.length())
        }
    }

    companion object {
        private const val SEGMENT_SIZE = 2048
    }
}
