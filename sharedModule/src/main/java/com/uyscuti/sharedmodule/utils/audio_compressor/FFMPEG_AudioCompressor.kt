package com.uyscuti.sharedmodule.utils.audio_compressor

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log

class FFMPEG_AudioCompressor {

    fun compress(inputFilePath: String, outputFilePath: String): Boolean {
        return try {
            Log.d("AudioCompressor", "Starting compression...")
            Log.d("AudioCompressor", "Input: $inputFilePath")
            Log.d("AudioCompressor", "Output: $outputFilePath")

            compressAudioFile(inputFilePath, outputFilePath)

            Log.d("AudioCompressor", "Compression successful")
            true
        } catch (e: Exception) {
            Log.e("AudioCompressor", "Compression failed: ${e.message}", e)
            false
        }
    }

    private fun compressAudioFile(inputPath: String, outputPath: String) {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputPath)

        // Find audio track
        var audioTrackIndex = -1
        var inputFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                inputFormat = format
                break
            }
        }

        if (audioTrackIndex < 0 || inputFormat == null) {
            throw IllegalArgumentException("No audio track found in input file")
        }

        extractor.selectTrack(audioTrackIndex)

        // Get input audio properties
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        Log.d("AudioCompressor", "Sample rate: $sampleRate, Channels: $channelCount")

        // Create decoder
        val inputMime = inputFormat.getString(MediaFormat.KEY_MIME) ?: ""
        val decoder = MediaCodec.createDecoderByType(inputMime)
        decoder.configure(inputFormat, null, null, 0)
        decoder.start()

        // Create output format with lower bitrate (64kbps for compression)
        val outputFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            sampleRate,
            channelCount
        )
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000) // 64kbps
        outputFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)

        // Create encoder
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        // Create muxer
        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var muxerAudioTrackIndex = -1
        var muxerStarted = false

        val decoderBufferInfo = MediaCodec.BufferInfo()
        val encoderBufferInfo = MediaCodec.BufferInfo()

        var isDecoderInputDone = false
        var isDecoderOutputDone = false
        var isEncoderOutputDone = false

        try {
            // Decoding and encoding loop
            while (!isEncoderOutputDone) {
                // Feed input to decoder
                if (!isDecoderInputDone) {
                    val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                        inputBuffer?.clear()

                        val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isDecoderInputDone = true
                            Log.d("AudioCompressor", "Decoder input done")
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            decoder.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                sampleSize,
                                presentationTimeUs,
                                0
                            )
                            extractor.advance()
                        }
                    }
                }

                // Get decoded output and feed to encoder
                var outputBufferIndex = decoder.dequeueOutputBuffer(decoderBufferInfo, 10000)
                while (outputBufferIndex >= 0) {
                    val decodedBuffer = decoder.getOutputBuffer(outputBufferIndex)

                    if (decoderBufferInfo.size > 0 && decodedBuffer != null) {
                        // Feed decoded data to encoder
                        val encoderInputIndex = encoder.dequeueInputBuffer(10000)
                        if (encoderInputIndex >= 0) {
                            val encoderInputBuffer = encoder.getInputBuffer(encoderInputIndex)

                            if (encoderInputBuffer != null) {
                                encoderInputBuffer.clear()

                                // Check if decoded data fits in encoder buffer
                                val bufferCapacity = encoderInputBuffer.remaining()
                                val dataSize = decoderBufferInfo.size

                                if (dataSize <= bufferCapacity) {
                                    // Copy data from decoded buffer
                                    decodedBuffer.position(decoderBufferInfo.offset)
                                    decodedBuffer.limit(decoderBufferInfo.offset + decoderBufferInfo.size)
                                    encoderInputBuffer.put(decodedBuffer)

                                    val flags = if (decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                        isDecoderOutputDone = true
                                        Log.d("AudioCompressor", "Decoder output done")
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                    } else {
                                        0
                                    }

                                    encoder.queueInputBuffer(
                                        encoderInputIndex,
                                        0,
                                        dataSize,
                                        decoderBufferInfo.presentationTimeUs,
                                        flags
                                    )
                                } else {
                                    Log.e("AudioCompressor", "Buffer too small: capacity=$bufferCapacity, needed=$dataSize")
                                    encoder.queueInputBuffer(encoderInputIndex, 0, 0, 0, 0)
                                }
                            }
                        }
                    } else if (decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isDecoderOutputDone = true
                        Log.d("AudioCompressor", "Decoder output done (EOS flag)")
                    }

                    decoder.releaseOutputBuffer(outputBufferIndex, false)

                    if (isDecoderOutputDone) break

                    outputBufferIndex = decoder.dequeueOutputBuffer(decoderBufferInfo, 0)
                }

                // Get encoded output
                var encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 10000)
                while (encoderOutputIndex >= 0) {
                    when (encoderOutputIndex) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = encoder.outputFormat
                            muxerAudioTrackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            muxerStarted = true
                            Log.d("AudioCompressor", "Muxer started")
                        }
                        else -> {
                            val encodedData = encoder.getOutputBuffer(encoderOutputIndex)

                            if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                encoderBufferInfo.size = 0
                            }

                            if (encoderBufferInfo.size != 0 && muxerStarted && encodedData != null) {
                                encodedData.position(encoderBufferInfo.offset)
                                encodedData.limit(encoderBufferInfo.offset + encoderBufferInfo.size)
                                muxer.writeSampleData(muxerAudioTrackIndex, encodedData, encoderBufferInfo)
                            }

                            encoder.releaseOutputBuffer(encoderOutputIndex, false)

                            if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                isEncoderOutputDone = true
                                Log.d("AudioCompressor", "Encoding complete")
                            }
                        }
                    }

                    if (isEncoderOutputDone) break

                    encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 0)
                }

                // Send EOS to encoder if decoder is done
                if (isDecoderOutputDone && !isEncoderOutputDone) {
                    val encoderInputIndex = encoder.dequeueInputBuffer(0)
                    if (encoderInputIndex >= 0) {
                        encoder.queueInputBuffer(
                            encoderInputIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        Log.d("AudioCompressor", "Sent EOS to encoder")
                    }
                }
            }
        } finally {
            // Cleanup with proper state checks
            try {
                decoder.stop()
                decoder.release()
                Log.d("AudioCompressor", "Decoder released")
            } catch (e: Exception) {
                Log.e("AudioCompressor", "Error stopping decoder: ${e.message}")
            }

            try {
                encoder.stop()
                encoder.release()
                Log.d("AudioCompressor", "Encoder released")
            } catch (e: Exception) {
                Log.e("AudioCompressor", "Error stopping encoder: ${e.message}")
            }

            try {
                extractor.release()
                Log.d("AudioCompressor", "Extractor released")
            } catch (e: Exception) {
                Log.e("AudioCompressor", "Error releasing extractor: ${e.message}")
            }

            try {
                // Only stop muxer if it was started
                if (muxerStarted) {
                    muxer.stop()
                    Log.d("AudioCompressor", "Muxer stopped")
                }
                muxer.release()
                Log.d("AudioCompressor", "Muxer released")
            } catch (e: Exception) {
                Log.e("AudioCompressor", "Error stopping muxer: ${e.message}")
            }
        }
    }
}