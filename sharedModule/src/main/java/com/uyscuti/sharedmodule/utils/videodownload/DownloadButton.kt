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

    fun setProgress(progress: Int) {
        this.progress = progress.coerceIn(0, 100)
        stopIndeterminateAnimation()
        invalidate()
    }

    fun setCompleted() {
        isDownloading = false
        progress = 0
        setImageResource(R.drawable.baseline_check_circle_24) // Add this drawable
        stopIndeterminateAnimation()
        invalidate()
    }

    fun reset() {
        isDownloading = false
        progress = 0
        setImageResource(R.drawable.download_02_svgrepo_com)
        stopIndeterminateAnimation()
        invalidate()
    }

    private fun startIndeterminateAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation ->
                progress = animation.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    private fun stopIndeterminateAnimation() {
        animator?.cancel()
        animator = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isDownloading || progress > 0) {
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = (Math.min(width, height) / 2f) - 12f

            rect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )

            // Draw background circle
            canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

            // Draw progress arc
            val sweepAngle = (progress / 100f) * 360f
            canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopIndeterminateAnimation()
    }
}