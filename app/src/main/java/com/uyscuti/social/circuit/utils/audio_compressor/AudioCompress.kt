package com.uyscuti.social.circuit.utils.audio_compressor
import android.media.*
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

fun compressAudio(inputFilePath: String, outputFilePath: String): Boolean {
    val inputStream = FileInputStream(inputFilePath)
    val outputStream = FileOutputStream(outputFilePath)

    return try {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(inputStream.fd)

        val audioTrackIndex = selectAudioTrack(mediaExtractor)

        mediaExtractor.selectTrack(audioTrackIndex)

        val format = mediaExtractor.getTrackFormat(audioTrackIndex)

//        val codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME)!!)

        Log.d("Codec", "mime type: ${format.getString(MediaFormat.KEY_MIME)}")
        val codec = try {
            MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        } catch (e: Exception) {
            // Fallback to a commonly supported MIME type
            MediaCodec.createEncoderByType("audio/mp4a-latm")
        }

        Log.d("Codec", "codec: $codec")
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        val inputBuffers = codec.inputBuffers
        val outputBuffers = codec.outputBuffers

        val bufferInfo = MediaCodec.BufferInfo()

        var isEOS = false
        while (!isEOS) {
            val inputBufferIndex = codec.dequeueInputBuffer(1000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = inputBuffers[inputBufferIndex]
                val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    isEOS = true
                } else {
                    codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.sampleTime, 0)
                    mediaExtractor.advance()
                }
            }

            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
            if (outputBufferIndex >= 0) {
                val outputBuffer = outputBuffers[outputBufferIndex]
                val outputData = ByteArray(bufferInfo.size)
                outputBuffer.get(outputData)
                outputStream.write(outputData)
//
//                val outputBuffer = outputBuffers[outputBufferIndex]
//                outputStream.write(outputBuffer.array(), bufferInfo.offset, bufferInfo.size)
                codec.releaseOutputBuffer(outputBufferIndex, false)
            }
        }

        codec.stop()
        codec.release()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    } finally {
        inputStream.close()
        outputStream.close()
    }
}

private fun selectAudioTrack(extractor: MediaExtractor): Int {
    val numTracks = extractor.trackCount
    for (i in 0 until numTracks) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime?.startsWith("audio/") == true) {
            return i
        }
    }
    return -1
}


//
////class AudioCompress {
////}
//import android.media.*
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//
//fun compressAudio(inputFilePath: String, outputFilePath: String) {
//    val inputStream = FileInputStream(inputFilePath)
//    val outputStream = FileOutputStream(outputFilePath)
//
//    try {
//        val mediaExtractor = MediaExtractor()
//        mediaExtractor.setDataSource(inputStream.fd)
//
//        val audioTrackIndex = selectAudioTrack(mediaExtractor)
//
//        mediaExtractor.selectTrack(audioTrackIndex)
//
//        val format = mediaExtractor.getTrackFormat(audioTrackIndex)
//
//        val codec = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME)!!)
//        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//        codec.start()
//
//        val inputBuffers = codec.inputBuffers
//        val outputBuffers = codec.outputBuffers
//
//        val bufferInfo = MediaCodec.BufferInfo()
//
//        var isEOS = false
//        while (!isEOS) {
//            val inputBufferIndex = codec.dequeueInputBuffer(1000)
//            if (inputBufferIndex >= 0) {
//                val inputBuffer = inputBuffers[inputBufferIndex]
//                val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
//                if (sampleSize < 0) {
//                    codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
//                    isEOS = true
//                } else {
//                    codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.sampleTime, 0)
//                    mediaExtractor.advance()
//                }
//            }
//
//            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
//            if (outputBufferIndex >= 0) {
//                val outputBuffer = outputBuffers[outputBufferIndex]
//                outputStream.write(outputBuffer.array(), bufferInfo.offset, bufferInfo.size)
//                codec.releaseOutputBuffer(outputBufferIndex, false)
//            }
//        }
//
//        codec.stop()
//        codec.release()
//    } catch (e: IOException) {
//        e.printStackTrace()
//    } finally {
//        inputStream.close()
//        outputStream.close()
//    }
//}
//
//private fun selectAudioTrack(extractor: MediaExtractor): Int {
//    val numTracks = extractor.trackCount
//    for (i in 0 until numTracks) {
//        val format = extractor.getTrackFormat(i)
//        val mime = format.getString(MediaFormat.KEY_MIME)
//        if (mime?.startsWith("audio/") == true) {
//            return i
//        }
//    }
//    return -1
//}
//
