package com.uyscuti.social.circuit.utils.feedutils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.content.Context

object ThumbnailUtil {

    // Convert Drawable to Bitmap
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
                drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    // Create a Thumbnail from Bitmap
    fun createThumbnail(bitmap: Bitmap, thumbnailWidth: Int, thumbnailHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, thumbnailWidth, thumbnailHeight, true)
    }

    // Convert Drawable to Thumbnail
    fun drawableToThumbnail(
        context: Context,
        drawable: Drawable,
        thumbnailWidth: Int,
        thumbnailHeight: Int
    ): Bitmap {
        val bitmap = drawableToBitmap(drawable)
        return createThumbnail(bitmap, thumbnailWidth, thumbnailHeight)
    }
}
