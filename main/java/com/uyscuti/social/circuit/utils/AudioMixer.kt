package com.uyscut.flashdesign.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Date

//class AudioMixer(
//    val context: Context,
//    val audioFilePaths: List<String>,
//    val outputFile: String,
//    private val progressListener: ProgressListener
//) {
//
//    private val TAG = "VideoMuxer"
//
//    interface ProgressListener {
//        fun onProgressUpdate(progress: Int)
//        fun onMixingComplete(outputPath: String)
//    }
//
//    fun selectTrack(extractor: MediaExtractor): Int {
//        val numTracks = extractor.trackCount
//        for (i in 0 until numTracks) {
//            val format = extractor.getTrackFormat(i)
//            val mime = format.getString(MediaFormat.KEY_MIME)
//            if (mime?.startsWith("audio/") == true) {
//                extractor.selectTrack(i)
//                return i
//            }
//        }
//        return -1
//    }
//    @RequiresApi(Build.VERSION_CODES.R)
//    fun muxing() {
//        try {
//            val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
////            muxer.setPreAllocation(false)
//            // Add audio tracks to the muxer
//            val audioTrackIndicesOut = mutableListOf<Int>()
//            val presentationTimes = mutableListOf<Long>()
//
//            audioFilePaths.forEach { audioFilePath ->
//                val audioExtractor = MediaExtractor()
//                audioExtractor.setDataSource(audioFilePath)
//                val audioTrackIndex = selectTrack(audioExtractor)
//                val format = audioExtractor.getTrackFormat(audioTrackIndex)
//                val correctedFormat = correctAudioMimeTypes(format)
//                val audioTrackIndexOut = muxer.addTrack(correctedFormat)
//                audioTrackIndicesOut.add(audioTrackIndexOut)
//                presentationTimes.add(0L)
//            }
//
//            muxer.start()
//
//            // Write audio data from each input file to the output file
//            val buffers = mutableListOf<ByteBuffer>()
//            val bufferInfo = MediaCodec.BufferInfo()
//            val offset = 100 // Offset for buffer
//            val sampleSize = 256 * 1024 // Sample size for buffer
//
//            audioFilePaths.forEachIndexed { index, audioFilePath ->
//                val audioExtractor = MediaExtractor()
//                audioExtractor.setDataSource(audioFilePath)
//                val audioTrackIndex = selectTrack(audioExtractor)
//                val audioBuf = ByteBuffer.allocate(sampleSize)
//
//                while (true) {
//                    val sampleSize = audioExtractor.readSampleData(audioBuf, offset)
//                    if (sampleSize < 0) {
//                        break
//                    }
//
//                    bufferInfo.offset = 0
//                    bufferInfo.size = sampleSize
//                    bufferInfo.presentationTimeUs = audioExtractor.sampleTime + presentationTimes[index]
//                    bufferInfo.flags = translateFlags(audioExtractor.sampleFlags)
//                    muxer.writeSampleData(audioTrackIndicesOut[index], audioBuf, bufferInfo)
//                    audioExtractor.advance()
//                }
//
//                audioExtractor.release()
//            }
//
//            muxer.stop()
//            muxer.release()
//            progressListener.onMixingComplete(outputFile)
//
//            // Notify mixing completion or handle further operations as needed
//        } catch (e: IOException) {
//            Log.d(TAG, "Mixer Error 1 ${e.message}")
//            e.printStackTrace()
//        } catch (e: Exception) {
//            Log.d(TAG, "Mixer Error 2 ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//    fun correctAudioMimeTypes(format: MediaFormat): MediaFormat {
//        val mime = format.getString(MediaFormat.KEY_MIME)
//        if (mime != null && mime.startsWith("audio/")) {
//            format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm")
//        }
//        return format
//    }
//
//    fun translateFlags(sampleFlags: Int): Int {
//        var flags = 0
//        if (sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
//            flags = flags or MediaCodec.BUFFER_FLAG_KEY_FRAME
//        }
//        return flags
//    }
////    @RequiresApi(Build.VERSION_CODES.R)
////    fun mixing() {
////        try {
////
////            val extractorList = mutableListOf<MediaExtractor>()
////            val audioTrackIndices = mutableListOf<Int>()
////
////            // Set up extractors for all input files and select audio tracks
////            audioFilePaths.forEach { audioFilePath ->
////                val extractor = MediaExtractor()
////                extractor.setDataSource(audioFilePath)
////                val audioTrackIndex = selectTrack(extractor)
////                extractorList.add(extractor)
////                audioTrackIndices.add(audioTrackIndex)
////            }
////
////            Log.d(TAG, "Video Extractor Track Count ${videoExtractor.trackCount}")
////            Log.d(TAG, "Audio Extractor Track Count ${audioExtractor.trackCount}")
////
////            val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
////
////            videoExtractor.selectTrack(0)
////            val videoFormat = videoExtractor.getTrackFormat(0)
////            val videoTrack = muxer.addTrack(videoFormat)
////
////            audioExtractor.selectTrack(0)
////            val audioFormat = audioExtractor.getTrackFormat(0)
////            val correctedFormat = correctAudioMimeType(audioFormat)
////            val audioTrack = muxer.addTrack(correctedFormat)
////
////            CoroutineScope(Dispatchers.IO).launch {
////                checkMime(audioFormat)
////            }
////
////            Log.d(TAG, "Video Format $videoFormat")
////            Log.d(TAG, "Audio Format $audioFormat")
////            Log.d(TAG, "Corrected Audio Format $correctedFormat")
////
////            val totalVideoFrames = getFrameCount(videoExtractor)
////            val totalAudioFrames = getFrameCount(audioExtractor)
////            val totalFrames = totalVideoFrames + totalAudioFrames
////
////            var sawEOS = false
////            var frameCount = 0
////            val offset = 100
////            val sampleSize = 256 * 1024
////            val videoBuf = ByteBuffer.allocate(sampleSize)
////            val audioBuf = ByteBuffer.allocate(sampleSize)
////            val videoBufferInfo = MediaCodec.BufferInfo()
////            val audioBufferInfo = MediaCodec.BufferInfo()
////
////            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
////            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
////
////            muxer.start()
////
////            while (!sawEOS) {
////                videoBufferInfo.offset = offset
////                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset)
////
////                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
////                    Log.d(TAG, "saw input EOS.")
////                    sawEOS = true
////                    videoBufferInfo.size = 0
////                } else {
////                    videoBufferInfo.presentationTimeUs = videoExtractor.sampleTime
////                    videoBufferInfo.flags = videoExtractor.sampleFlags
////                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo)
////                    videoExtractor.advance()
////
////                    frameCount++
////
////                    val progress = (frameCount * 100 / totalVideoFrames)
////                    progressListener.onProgressUpdate(progress)
//////                    Log.d(TAG, "Frame ($frameCount) Video PresentationTimeUs:${videoBufferInfo.presentationTimeUs} Flags:${videoBufferInfo.flags} Size(KB) ${videoBufferInfo.size / 1024}")
//////                    Log.d(TAG, "Frame ($frameCount) Audio PresentationTimeUs:${audioBufferInfo.presentationTimeUs} Flags:${audioBufferInfo.flags} Size(KB) ${audioBufferInfo.size / 1024}")
////                }
////            }
////
////            Toast.makeText(context, "frame:$frameCount", Toast.LENGTH_SHORT).show()
////
////            var sawEOS2 = false
////            var frameCount2 = 0
////            while (!sawEOS2) {
////                frameCount2++
////
////                audioBufferInfo.offset = offset
////                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset)
////
////                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
////                    Log.d(TAG, "saw input EOS.")
////                    sawEOS2 = true
////                    audioBufferInfo.size = 0
////                } else {
////                    audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
////                    audioBufferInfo.flags = audioExtractor.sampleFlags
////                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo)
////                    audioExtractor.advance()
////
////
////                    val progress = (frameCount * 100 / totalVideoFrames)
////                    progressListener.onProgressUpdate(progress)
////
//////                    Log.d(TAG, "Frame ($frameCount) Video PresentationTimeUs:${videoBufferInfo.presentationTimeUs} Flags:${videoBufferInfo.flags} Size(KB) ${videoBufferInfo.size / 1024}")
//////                    Log.d(TAG, "Frame ($frameCount) Audio PresentationTimeUs:${audioBufferInfo.presentationTimeUs} Flags:${audioBufferInfo.flags} Size(KB) ${audioBufferInfo.size / 1024}")
////                }
////            }
////
////            Toast.makeText(context, "frame:$frameCount2", Toast.LENGTH_SHORT).show()
////
////            muxer.stop()
////            muxer.release()
////
////            progressListener.onMixingComplete(outputFile)
////
////
////        } catch (e: IOException) {
////            Log.d(TAG, "Mixer Error 1 ${e.message}")
////            e.printStackTrace()
////        } catch (e: Exception) {
////            Log.d(TAG, "Mixer Error 2 ${e.message}")
////            e.printStackTrace()
////        }
////    }
//
//    private fun getFrameCount(extractor: MediaExtractor): Int {
//        var frameCount = 0
//        val buffer = ByteBuffer.allocate(256 * 1024) // Adjust buffer size as needed
//
//        while (true) {
//            val sampleSize = extractor.readSampleData(buffer, 0)
//
//            if (sampleSize < 0) {
//                break
//            }
//
//            extractor.advance()
//            frameCount++
//        }
//
//        // Rewind the extractor to the beginning
//        extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
//
//        return frameCount
//    }
//
//    private fun checkMime(format: MediaFormat) {
//        val mime = format.getString(MediaFormat.KEY_MIME)
//
//        // Check if the MIME type needs correction
//        if (mime != null && mime == "audio/mpeg") {
//            // Correct the MIME type to a compatible format (e.g., "audio/mp4a-latm" for AAC)
//            val audioOutPut = getOutputAudioFilePath()
//
////            convertMp3ToM4a(context, audioFilePath, audioOutPut)
//        } else {
//            Log.d(TAG, "Its not an mp3 file")
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.R)
//    private fun correctAudioMimeType(originalFormat: MediaFormat): MediaFormat {
//        val mime = originalFormat.getString(MediaFormat.KEY_MIME)
//
//
//        // Check if the MIME type needs correction
//        if (mime != null && mime == "audio/mpeg") {
//
//
//            val correctedFormat = MediaFormat.createAudioFormat(
//                "audio/mp4a-latm",
//                originalFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
//                originalFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//            )
//
//            // Copy other relevant parameters from the original format if needed
//            correctedFormat.setInteger(
//                MediaFormat.KEY_BIT_RATE,
//                originalFormat.getInteger(MediaFormat.KEY_BIT_RATE)
//            )
//
//            // Explicitly copy parameters that may affect playback
//            val keysToCopy = arrayOf(
//                MediaFormat.KEY_ENCODER_DELAY,
//                MediaFormat.KEY_ENCODER_PADDING,
//                MediaFormat.KEY_DURATION
//            )
//
//            try {
//                val csdBuffer = originalFormat.getByteBuffer("csd-0")
//                val generated = generateAacCsd()
//
//                Log.d(TAG, "CSD Buffer : $csdBuffer")
//                Log.d(TAG, "Generated CSD Buffer : $generated")
//                correctedFormat.setByteBuffer("csd-0", generated)
//            } catch (e: Exception) {
//                Log.d(TAG, "Exception is :${e.message}")
//            }
//
//
//            // Copy all relevant parameters from the original format
//            for (key in originalFormat.keys) {
//                when (key) {
//                    MediaFormat.KEY_AAC_PROFILE -> {
//                        Log.d(TAG, "Setting ACC Profile $key")
//                    }
//
//                    "csd-0" -> {
//                        Log.d(TAG, "Setting CSD-0 $key")
//                    }
//
//                    MediaFormat.KEY_DURATION -> {
//                        Log.d(TAG, "Setting Duration")
//                        correctedFormat.setLong(key, originalFormat.getLong(key))
//                    }
//
//                    MediaFormat.KEY_ENCODER_DELAY -> {
//                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                    }
//
//                    MediaFormat.KEY_SAMPLE_RATE -> {
//                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                    }
//
//                    MediaFormat.KEY_TRACK_ID -> {
//                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                    }
//
//                    MediaFormat.KEY_MIME -> {
//                        Log.d(TAG, "Logging Mime :$key")
//                    }
//
//                    MediaFormat.KEY_CHANNEL_COUNT -> {
//                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                    }
//
//                    MediaFormat.KEY_BIT_RATE -> {
//                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                    }
//
//                    MediaFormat.KEY_ENCODER_PADDING -> {
//                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                    }
//
//                    MediaFormat.KEY_MAX_INPUT_SIZE, MediaFormat.KEY_AAC_PROFILE -> {
//                        // Handle Long values
//                        val keyToCopy = originalFormat.getValueTypeForKey(key)
//
//                        Log.d(TAG, "Key To Copy is $keyToCopy  and $key")
//
//                        try {
//                            correctedFormat.setLong(key, originalFormat.getLong(key))
//                            Log.d(TAG, "Key To Copy is $keyToCopy  and $key is Long")
//                        } catch (e: ClassCastException) {
//                            Log.d(TAG, "class Cast error message $key :${e.message}")
////                            correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                            Log.d(TAG, "Key To Copy is $keyToCopy  and $key is Int")
//                            e.printStackTrace()
//                        } catch (e: Exception) {
//                            Log.d(TAG, "General error message $key :${e.message}")
//                            e.printStackTrace()
//                        }
//                    }
//
//                    else -> {
//                        // Copy other parameters as is
////                        correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                        val keyToCopy = originalFormat.getValueTypeForKey(key)
//
//                        Log.d(TAG, "Key To Copy is $keyToCopy  and $key")
//
//                        if (key == "bits-per-sample") {
//                            correctedFormat.setInteger(key, originalFormat.getInteger(key))
//                            Log.d(TAG, "Setting $key")
//                        }
//                    }
//                }
//            }
////            for (key in keysToCopy) {
////                if (originalFormat.containsKey(key)) {
////                    correctedFormat.setInteger(key, originalFormat.getInteger(key))
////                }
////            }
//
//            // Log the details before and after correction
//            Log.d(TAG, "Original Audio Format: ${originalFormat.toString()}")
//            Log.d(TAG, "Corrected Audio Format: ${correctedFormat.toString()}")
//
//            return correctedFormat
//        }
//
//        // Return the original format if no correction is needed
//        return originalFormat
//    }
//
//    private fun generateAacCsd(): ByteBuffer {
//        // Replace these values with the actual AAC codec parameters
//        val profile = 2 // AAC LC
//        val sampleRate = 44100 // Audio sample rate
//        val channels = 2 // Number of audio channels
//
//        // Calculate the CSD-0 bytes based on AAC parameters
//        val csdData = ByteBuffer.allocate(2)
//        csdData.put((profile shl 3 or (sampleRate shr 1 and 0x7F)).toByte())
//        csdData.put(((sampleRate shl 7 and 0x3FFF) or (channels shl 3 and 0x78)).toByte())
//
//        // Reset position to the beginning for reading
//        csdData.flip()
//
//        return csdData
//    }
//
//
//    private fun convertMpegToAac(inputFilePath: String, outputFilePath: String) {
//        // Set up MediaExtractor for reading the input audio file
//        val extractor = MediaExtractor()
//        extractor.setDataSource(inputFilePath)
//
//        // Find and configure the audio track
//        val audioTrackIndex = findAudioTrack(extractor)
//        extractor.selectTrack(audioTrackIndex)
//
//        // Get the input audio format
//        val inputAudioFormat = extractor.getTrackFormat(audioTrackIndex)
//
//        // Set up MediaCodec for encoding AAC
//        val codec = MediaCodec.createEncoderByType("audio/mp4a-latm")
//        codec.configure(
//            MediaFormat().apply {
//                setInteger(MediaFormat.KEY_BIT_RATE, 128000) // Set your desired bit rate
//                setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100) // Set your desired sample rate
//                setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2) // Set your desired channel count
//                setString(MediaFormat.KEY_MIME, "audio/mp4a-latm")
//            },
//            null,
//            null,
//            MediaCodec.CONFIGURE_FLAG_ENCODE
//        )
//        codec.start()
//
//        // Set up output file
//        val outputFile = File(outputFilePath)
//        val outputStream = FileOutputStream(outputFile)
//
//        try {
//            // Start processing
//            val inputBufferInfo = MediaCodec.BufferInfo()
//            val outputBufferInfo = MediaCodec.BufferInfo()
//
//            val inputBuffer = ByteBuffer.allocateDirect(1024 * 1024) // Adjust buffer size as needed
//            val outputBuffer =
//                ByteBuffer.allocateDirect(1024 * 1024) // Adjust buffer size as needed
//
//            // Encode input audio and write to the output file
//            while (true) {
//                val inputIndex = codec.dequeueInputBuffer(-1)
//                if (inputIndex >= 0) {
//                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
//                    if (sampleSize < 0) {
//                        codec.queueInputBuffer(
//                            inputIndex,
//                            0,
//                            0,
//                            0,
//                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
//                        )
//                        break
//                    }
//                    val presentationTimeUs = extractor.sampleTime
//                    codec.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0)
//                    extractor.advance()
//                }
//
//                val outputIndex = codec.dequeueOutputBuffer(outputBufferInfo, -1)
//                if (outputIndex >= 0) {
//                    outputBuffer.clear()
//                    val size = codec.getOutputBuffer(outputIndex)!!.remaining()
//                    codec.getOutputBuffer(outputIndex)!!.get(outputBuffer.array(), 0, size)
//                    outputStream.write(outputBuffer.array(), 0, size)
//                    codec.releaseOutputBuffer(outputIndex, false)
//                }
//
//                if ((outputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    break
//                }
//            }
//        } finally {
//            // Release resources
//            extractor.release()
//            codec.stop()
//            codec.release()
//            outputStream.close()
//        }
//    }
//
//    private fun findAudioTrack(extractor: MediaExtractor): Int {
//        for (i in 0 until extractor.trackCount) {
//            val format = extractor.getTrackFormat(i)
//            val mime = format.getString(MediaFormat.KEY_MIME)
//            if (mime?.startsWith("audio/") == true) {
//                return i
//            }
//        }
//        throw RuntimeException("No audio track found")
//    }
//
//    private fun getOutputAudioFilePath(): String {
//        val downloadsDir =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val fileName = "output_${Date().time}.m4a"
//        val outputFile = File(downloadsDir, fileName)
//        return outputFile.absolutePath
//    }
//
//    private fun convertMp3ToM4a(context: Context, inputFilePath: String, outputFile: String) {
//
//        try {
//            val outputFilePath = outputFile
//
//            // Set up MediaExtractor for MP3 input
//            val mp3Extractor = MediaExtractor()
//            mp3Extractor.setDataSource(inputFilePath)
//            var muxerStarted = false
//
//            // Find and configure the audio track from MP3
//            var audioTrackIndex = -1
//            for (i in 0 until mp3Extractor.trackCount) {
//                val format = mp3Extractor.getTrackFormat(i)
//                val mime = format.getString(MediaFormat.KEY_MIME)
//                if (mime?.startsWith("audio/") == true) {
//                    audioTrackIndex = i
//                    break
//                }
//            }
//
//            if (audioTrackIndex == -1) {
//                // No audio track found
//                return
//            }
//
//            mp3Extractor.selectTrack(audioTrackIndex)
//            val mp3Format = mp3Extractor.getTrackFormat(audioTrackIndex)
//
//            // Set up MediaCodec for AAC output
//            val aacFormat = MediaFormat.createAudioFormat(
//                MediaFormat.MIMETYPE_AUDIO_AAC,
//                mp3Format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
//                mp3Format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//            )
//            aacFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000) // Adjust bit rate as needed
//
//            val aacCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
//            aacCodec.configure(aacFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//            aacCodec.start()
//
//            // Set up MediaMuxer for M4A output
//            val muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
//
//            // Add audio track to the muxer
//            var aacTrackIndex = muxer.addTrack(aacFormat)
////            muxer.start()
//
//            // Process input data
//            val bufferInfo = MediaCodec.BufferInfo()
//            val buffer = ByteBuffer.allocateDirect(1024 * 1024) // Adjust buffer size as needed
//
//            while (true) {
//                val inputBufferIndex = aacCodec.dequeueInputBuffer(-1)
//                if (inputBufferIndex >= 0) {
//                    val size = mp3Extractor.readSampleData(buffer, 0)
//                    if (size < 0) {
//                        // End of input
//                        aacCodec.queueInputBuffer(
//                            inputBufferIndex,
//                            0,
//                            0,
//                            0,
//                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
//                        )
//                        break
//                    } else {
//                        val presentationTimeUs = mp3Extractor.sampleTime
//                        aacCodec.queueInputBuffer(inputBufferIndex, 0, size, presentationTimeUs, 0)
//                        mp3Extractor.advance()
//                    }
//                }
//
//                val outputBufferIndex = aacCodec.dequeueOutputBuffer(bufferInfo, 0)
//                if (outputBufferIndex >= 0) {
//                    buffer.position(0)
//                    buffer.limit(bufferInfo.size)
//                    muxer.writeSampleData(aacTrackIndex, buffer, bufferInfo)
//                    aacCodec.releaseOutputBuffer(outputBufferIndex, false)
//                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    // Update the track format when the format changes
//                    val newFormat = aacCodec.outputFormat
////                    aacTrackIndex = muxer.addTrack(newFormat)
////                    muxer.start()
//
//                    // Add a new track to the muxer with the updated format
//                    aacTrackIndex = muxer.addTrack(newFormat)
//
//                    // Start the muxer if it hasn't been started yet
//                    if (!muxerStarted) {
//                        muxer.start()
//                        muxerStarted = true
//                    }
//                    Log.d(TAG, "Format Changed $newFormat")
//                }
//            }
//            Log.d(TAG, "Converted Successfully")
//            CoroutineScope(Dispatchers.Main).launch {
//                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
//            }
//            // Release resources
//            mp3Extractor.release()
//            aacCodec.stop()
//            aacCodec.release()
//            muxer.stop()
//            muxer.release()
//
//        } catch (e: IOException) {
//            Log.d(TAG, "IO Exception is :${e.message}")
//            e.printStackTrace()
//        } catch (e: Exception) {
//            Log.d(TAG, "Exception is ${e.message}")
//            e.printStackTrace()
//        }
//
//    }
//
//
//    @SuppressLint("WrongConstant")
//    fun trimAudio(inputFilePath: String, outputFilePath: String, startTimeUs: Long, endTimeUs: Long) {
//        val mediaExtractor = MediaExtractor()
//        mediaExtractor.setDataSource(inputFilePath)
//
//        val audioTrackIndex = getAudioTrackIndex(mediaExtractor)
//
//        val mediaFormat = mediaExtractor.getTrackFormat(audioTrackIndex)
//        val mediaMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
//
//        val trackIndex = mediaMuxer.addTrack(mediaFormat)
//        mediaMuxer.start()
//
//        val buffer = ByteBuffer.allocate(1024 * 1024)
//        val info = MediaCodec.BufferInfo()
//
//        mediaExtractor.selectTrack(audioTrackIndex)
//
//        while (true) {
//            val sampleSize = mediaExtractor.readSampleData(buffer, 0)
//            if (sampleSize < 0) {
//                break
//            }
//
//            val presentationTimeUs = mediaExtractor.sampleTime
//            if (presentationTimeUs > endTimeUs) {
//                break
//            }
//
//            if (presentationTimeUs >= startTimeUs) {
//                info.offset = 0
//                info.size = sampleSize
//                info.presentationTimeUs = presentationTimeUs
//                info.flags = mediaExtractor.sampleFlags
//                mediaMuxer.writeSampleData(trackIndex, buffer, info)
//            }
//
//            mediaExtractor.advance()
//        }
//
//        mediaMuxer.stop()
//        mediaMuxer.release()
//        mediaExtractor.release()
//    }
//
//    private fun getAudioTrackIndex(mediaExtractor: MediaExtractor): Int {
//        val trackCount = mediaExtractor.trackCount
//        for (i in 0 until trackCount) {
//            val format = mediaExtractor.getTrackFormat(i)
//            val mime = format.getString(MediaFormat.KEY_MIME)
//            if (mime?.startsWith("audio/") == true) {
//                return i
//            }
//        }
//        return -1
//    }
//
//}
