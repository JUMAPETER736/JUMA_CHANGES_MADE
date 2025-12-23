package com.uyscuti.social.circuit.utils.audio_compressor

//import com.arthenica.mobileffmpeg.Config
//import com.arthenica.mobileffmpeg.FFmpeg
//import com.arthenica.mobileffmpeg.StatisticsCallback


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

//        Config.enableStatisticsCallback(StatisticsCallback { newStatistics ->
//            val progress: Float =
//                java.lang.String.valueOf(newStatistics.time).toFloat() / audioLength
//            val progressFinal = progress * 100
////            Log.d(TAG, "Video Length: $progressFinal")
////            progressCallback(progressFinal.toInt())
//            progressListener.onProgress(progressFinal.toInt())

//            Log.d(
//                Config.TAG,
//                java.lang.String.format(
//                    "frame: %d, time: %d",
//                    newStatistics.videoFrameNumber,
//                    newStatistics.time
//                )
//            )
//            Log.d(
//                Config.TAG,
//                java.lang.String.format(
//                    "Quality: %f, time: %f",
//                    newStatistics.videoQuality,
//                    newStatistics.videoFps
//                )
//            )
//            progressDialog.setProgress(progressFinal.toInt())
        //})


//        val result = FFmpeg.executeAsync(command) { executionId, returnCode ->
//            when (returnCode) {
//                RETURN_CODE_SUCCESS -> {
//                    Log.i(
//                        Config.TAG,
//                        "Async command execution completed successfully."
//                    )
//                }
//                RETURN_CODE_CANCEL -> {
//                    Log.i(
//                        Config.TAG,
//                        "Async command execution cancelled by user."
//                    )
//                }
//                else -> {
//
//                    Log.i(
//                        Config.TAG,
//                        java.lang.String.format(
//                            "Async command execution failed with rc=%d.",
//                            returnCode
//                        )
//                    )
//                }
//            }
//        }
//        val result = FFmpeg.execute(command)
//        return result.toInt() == 0

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



//class AudioCompressorWithProgress {
//
//    suspend fun compress(inputFilePath: String, outputFilePath: String, progressCallback: (Int) -> Unit): Boolean {
//        return withContext(Dispatchers.IO) {
//            val command = arrayOf(
//                "-i", inputFilePath,
//                "-b:a", "64k", // Set the target bitrate (adjust as needed)
//                "-c:a", "libmp3lame", // Set the codec (libmp3lame for MP3)
//                outputFilePath
//            )
//
//            val processBuilder = ProcessBuilder(*command)
//            processBuilder.redirectErrorStream(true)
//
//            val process = processBuilder.start()
//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//            var line: String?
//
//            // Read the output to capture progress
//            while (reader.readLine().also { line = it } != null) {
//                // Example: FFmpeg typically outputs progress information to stderr
//                // Look for lines containing progress info
//                if (line!!.startsWith("frame=")) {
//                    // Parse progress information and update UI or callback
//                    val progress = parseProgress(line!!)
//                    progressCallback(progress)
//                }
//            }
//
//            val result = process.waitFor()
//            result == 0
//        }
//    }
//
//    private fun parseProgress(line: String): Int {
//        // Example: Parse progress from line like "frame=1234 fps=30 q=0.0 size=12345kB time=12:34:56.78 bitrate=1234.5kbits/s speed=2.5x"
//        // Extract progress from the line
//        // For simplicity, let's assume you extract frame number as progress
//        val progressString = line.substringAfter("frame=").substringBefore(" ")
//        return try {
//            progressString.toInt()
//        } catch (e: NumberFormatException) {
//            0
//        }
//    }
//}

//class AudioCompressorWithProgress {
//
//    suspend fun compress(inputFilePath: String, outputFilePath: String): Boolean {
//        return withContext(Dispatchers.IO) {
//            val command = arrayOf(
//                "-i", inputFilePath,
//                "-b:a", "64k", // Set the target bitrate (adjust as needed)
//                "-c:a", "libmp3lame", // Set the codec (libmp3lame for MP3)
//                outputFilePath
//            )
//
//            val processBuilder = ProcessBuilder(*command)
//            processBuilder.redirectErrorStream(true)
//
//            val process = processBuilder.start()
//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//            var line: String?
//
//            // Read the output to capture progress
//            while (reader.readLine().also { line = it } != null) {
//                // Assuming progress is printed to stderr, parse the output here
//                // Example: you can look for lines containing progress info
//                if (line!!.startsWith("frame=")) {
//                    // Parse progress information and update UI or store it as needed
//                    val progress = parseProgress(line!!)
//                    updateProgress(progress)
//                }
//            }
//
//            val result = process.waitFor()
//            result == 0
//        }
//    }
//
//    private fun parseProgress(line: String): Int {
//        // Example: Parse progress from line like "frame=1234 fps=30 q=0.0 size=12345kB time=12:34:56.78 bitrate=1234.5kbits/s speed=2.5x"
//        // Extract progress from the line
//        // You need to implement this based on FFmpeg output format
//        // For simplicity, let's assume you extract frame number as progress
//        val progressString = line.substringAfter("frame=").substringBefore(" ")
//        return try {
//            progressString.toInt()
//        } catch (e: NumberFormatException) {
//            0
//        }
//    }
//
//    private fun updateProgress(progress: Int) {
//        // Update your UI or store progress as needed
//        println("Progress: $progress")
//    }
//}
