package com.uyscuti.sharedmodule.utils.videodownload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadStateManager @Inject constructor() {

    data class DownloadState(
        val postId: String,
        val workId: UUID,
        val progress: Int,
        val status: Status
    )

    enum class Status {
        IDLE,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    private val _downloadStates = MutableLiveData<Map<String, DownloadState>>(emptyMap())
    val downloadStates: LiveData<Map<String, DownloadState>> = _downloadStates

    private val currentStates = mutableMapOf<String, DownloadState>()

    fun startDownload(postId: String, workId: UUID) {
        currentStates[postId] = DownloadState(postId, workId, 0, Status.DOWNLOADING)
        _downloadStates.postValue(currentStates.toMap())
    }

    fun updateProgress(postId: String, progress: Int) {
        currentStates[postId]?.let { state ->
            currentStates[postId] = state.copy(progress = progress)
            _downloadStates.postValue(currentStates.toMap())
        }
    }

    fun setPaused(postId: String) {
        currentStates[postId]?.let { state ->
            currentStates[postId] = state.copy(status = Status.PAUSED)
            _downloadStates.postValue(currentStates.toMap())
        }
    }

    fun setCompleted(postId: String) {
        currentStates[postId]?.let { state ->
            currentStates[postId] = state.copy(status = Status.COMPLETED, progress = 100)
            _downloadStates.postValue(currentStates.toMap())
        }
    }

    fun setFailed(postId: String) {
        currentStates[postId]?.let { state ->
            currentStates[postId] = state.copy(status = Status.FAILED)
            _downloadStates.postValue(currentStates.toMap())
        }
    }

    fun setCancelled(postId: String) {
        currentStates[postId]?.let { state ->
            currentStates[postId] = state.copy(status = Status.CANCELLED)
            _downloadStates.postValue(currentStates.toMap())
        }
    }

    fun removeDownload(postId: String) {
        currentStates.remove(postId)
        _downloadStates.postValue(currentStates.toMap())
    }

    fun getDownloadState(postId: String): DownloadState? {
        return currentStates[postId]
    }

    fun isDownloading(postId: String): Boolean {
        return currentStates[postId]?.status == Status.DOWNLOADING
    }

    fun isPaused(postId: String): Boolean {
        return currentStates[postId]?.status == Status.PAUSED
    }
}