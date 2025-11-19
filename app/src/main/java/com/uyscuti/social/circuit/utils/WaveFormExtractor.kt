package com.uyscuti.social.circuit.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.uyscuti.social.circuit.utils.waveformseekbar.utils.uriToFile
import linc.com.amplituda.AmplitudaProcessingOutput
import linc.com.amplituda.exceptions.AmplitudaException
import linc.com.amplituda.Amplituda
import linc.com.amplituda.Cache




object WaveFormExtractor {

    @JvmStatic
    fun getSampleFrom(context: Context, pathOrUrl: String, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput(Amplituda(context).processAudio(pathOrUrl, Cache.withParams(Cache.REUSE)), onSuccess)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, resource: Int, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput(Amplituda(context).processAudio(resource), onSuccess)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, uri: Uri, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput(Amplituda(context).processAudio(context.uriToFile(uri)), onSuccess)
    }

    private fun handleAmplitudaOutput(
        amplitudaOutput: AmplitudaProcessingOutput<*>,
        onSuccess: (IntArray) -> Unit
    ) {
        val result = amplitudaOutput.get { exception: AmplitudaException ->

            Log.e("Amplitude", "Amplitude exception: ${exception.message}")
            exception.printStackTrace()
        }
//        onSuccess(result.amplitudesAsList().toTypedArray().toIntArray())

        result?.let {
            onSuccess(result.amplitudesAsList().toTypedArray().toIntArray())
            Log.d("WaveFormExtractor", "Waveform extraction successful")
        }
    }
}