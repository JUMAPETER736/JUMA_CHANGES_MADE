package com.uyscuti.social.circuit.views

// RoundedImageView.kt
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap

class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val cornerRadius = 18f // Change this to the desired radius
    private val rectF = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val bitmap = drawable.toBitmap() // Convert drawable to bitmap
        val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = bitmapShader
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
    }
}
