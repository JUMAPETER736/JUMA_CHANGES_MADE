package com.uyscuti.sharedmodule.utils.audio_compressor




private const val TAG  = "AudioCompressorWithProgress"

class AudioCompressorWithProgress {

    interface ProgressListener {
        fun onProgress(progress: Int)
    }

    fun compress(inputFilePath: String, outputFilePath: String,  audioLength: Long, progressListener: ProgressListener): Boolean {
        val command = arrayOf(
            "-i", inputFilePath,
            "-b:a", "64k",
            "-c:a", "libmp3lame",
            "-progress", "pipe:1", // Enable progress reporting
            outputFilePath
        )


        return false
    }
    private fun String.toSeconds(): Double {
        val parts = this.split(":")
        val hours = parts[0].toDoubleOrNull() ?: 0.0
        val minutes = parts[1].toDoubleOrNull() ?: 0.0
        val seconds = parts[2].toDoubleOrNull() ?: 0.0
        return hours * 3600 + minutes * 60 + seconds
    }

}

