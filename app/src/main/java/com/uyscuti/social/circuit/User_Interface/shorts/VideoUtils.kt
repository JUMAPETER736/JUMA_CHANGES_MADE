package com.uyscuti.social.circuit.User_Interface.shorts

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

object VideoUtils {

    fun getFirstFrame(context: Context, videoUri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, videoUri)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }
}