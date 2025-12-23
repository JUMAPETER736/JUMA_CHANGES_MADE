package com.uyscuti.social.circuit.viewmodels.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeedUploadProgressViewModel : ViewModel() {
    // Backing property to avoid exposing MutableLiveData
    private var progressMaxValue:Int = 0




    // Method to update the integer value
    fun setProgressMaxValue(newValue: Int) {
        progressMaxValue = newValue
    }

    fun getProgressMaxValue():Int {
        return progressMaxValue
    }
}
