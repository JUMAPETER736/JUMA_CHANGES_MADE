package com.uyscuti.social.circuit.utils.audio_compressor


class FFMPEG_AudioCompressor {

    fun compress(inputFilePath: String, outputFilePath: String): Boolean {
        val command = arrayOf(
            "-i", inputFilePath,
            "-b:a", "64k", // Set the target bitrate (adjust as needed)
            "-c:a", "libmp3lame", // Set the codec (libmp3lame for MP3)
            outputFilePath
        )

        //val result = FFmpeg.execute(command)

        //return result ==0
        return false
    }
}
