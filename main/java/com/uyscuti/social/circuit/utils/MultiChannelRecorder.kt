package com.uyscuti.social.circuit.utils
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class MultiChannelRecorder {
    private lateinit var mediaMuxer: MediaMuxer
    private val trackMap = mutableMapOf<File, Int>()
    private val recordingFiles = mutableListOf<File>() // List to track files being recorded

    init {
        val outputFile = createOutputFile()
        mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    private fun createOutputFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val fileName = "record_$timestamp.mp3"
        val outputFile = File(musicDirectory, fileName)
        return outputFile
    }

    fun addAudioTrack(audioFile: File) {
        val audioTrackIndex = mediaMuxer.addTrack(getMediaFormat(audioFile))
        trackMap[audioFile] = audioTrackIndex
        recordingFiles.add(audioFile) // Add file to the list of recording files
    }

    fun writeData(audioFile: File) {
        val trackIndex = trackMap[audioFile] ?: return
        val byteBuffer = ByteBuffer.allocate(1024)
        val mediaCodecBufferInfo = MediaCodec.BufferInfo()
        val fileInputStream = FileInputStream(audioFile)

        mediaMuxer.start()

        var bytesRead = fileInputStream.read(byteBuffer.array())
        while (bytesRead != -1) {
            mediaCodecBufferInfo.set(0, bytesRead, 0, 0)
            mediaMuxer.writeSampleData(trackIndex, byteBuffer, mediaCodecBufferInfo)
            bytesRead = fileInputStream.read(byteBuffer.array())
        }

        fileInputStream.close()
    }

    fun stopRecording() {
        mediaMuxer.stop()
        mediaMuxer.release()
    }

    private fun getMediaFormat(file: File): MediaFormat {
        val mediaExtractor = android.media.MediaExtractor()
        mediaExtractor.setDataSource(file.absolutePath)
        val trackFormat = mediaExtractor.getTrackFormat(0)
        mediaExtractor.release()
        return trackFormat
    }

    fun getRecordingFiles(): List<File> {
        return recordingFiles.toList()
    }
}
