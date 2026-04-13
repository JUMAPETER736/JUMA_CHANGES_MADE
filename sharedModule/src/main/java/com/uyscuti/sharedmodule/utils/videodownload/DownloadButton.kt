package com.uyscuti.sharedmodule.utils.videodownload

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.uyscuti.sharedmodule.R

class DownloadButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    private var progress = 0
    private var isDownloading = false

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = ContextCompat.getColor(context, R.color.bluejeans)
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = ContextCompat.getColor(context, R.color.white)
        alpha = 50
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()
    private var animator: ValueAnimator? = null

    fun setDownloading(downloading: Boolean) {
        isDownloading = downloading
        if (downloading) {
            setImageResource(R.drawable.download_02_svgrepo_com)
            startIndeterminateAnimation()
        } else {
            stopIndeterminateAnimation()
            setImageResource(R.drawable.download_02_svgrepo_com)
        }
        invalidate()
    }

    fun setPaused(progress: Int) {
        isDownloading = false
        this.progress = progress
        setImageResource(R.drawable.baseline_pause_white_24) // Add a pause icon
        stopIndeterminateAnimation()
        invalidate()
    }

