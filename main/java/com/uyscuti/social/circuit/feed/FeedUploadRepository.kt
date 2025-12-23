package com.uyscuti.social.circuit.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FeedUploadRepository {
    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus
    var _id = ""
    fun updateUploadStatus(status: Boolean) {
        _uploadStatus.postValue(status)
    }
}
