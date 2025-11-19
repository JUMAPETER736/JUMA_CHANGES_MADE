package com.uyscuti.social.circuit.utils.waveformseekbar

import android.view.MotionEvent

interface SeekBarOnProgressChanged {
    fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean)
    fun onRelease(event: MotionEvent?, progress: Float)
}