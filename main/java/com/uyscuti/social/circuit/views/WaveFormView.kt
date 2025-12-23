package com.uyscuti.social.circuit.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveFormView(context: Context?, attrs: AttributeSet?): View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()
    private var radius = 6f
    private var d = 6f
    private var w = 2f
    private var sw = 0f
    private var sh = 90f
    private var maxSpikes = 0
    init{
        paint.color = Color.parseColor("#3284fc")

//        paint.color = Color.rgb(0,0,70)

        sw = resources.displayMetrics.widthPixels.toFloat()
        maxSpikes = (sw/(w+d)).toInt()
    }

    fun clear():ArrayList<Float>{
        val amps = amplitudes.clone() as ArrayList<Float>
        amplitudes.clear()
        spikes.clear()
        invalidate()

        return amps
    }

    fun addAmplitude(amp: Float) {

        val norm  = (amp.toInt() / 17).coerceAtMost(90).toFloat()
        amplitudes.add(norm)

//        Log.d("Spikes","add amplitude")

        spikes.clear()
        val amps = amplitudes.takeLast(maxSpikes)
        for(i in amps.indices) {
            val left = sw - i*(w+d)
            val top = sh/2 - amps[i]/2
            val right: Float = left + w
            val bottom:Float = top + amps[i]

            spikes.add(RectF(left, top, right, bottom))
        }
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
//        canvas.drawRoundRect(RectF(20f, 40f, 20f+30f, 30f+60f), 6f, 6f, paint)
        spikes.forEach {
            canvas.drawRoundRect(it, radius, radius, paint)
        }
    }
}