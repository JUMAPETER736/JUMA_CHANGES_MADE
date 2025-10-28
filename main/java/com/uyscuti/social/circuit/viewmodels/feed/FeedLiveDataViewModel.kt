package com.uyscuti.social.circuit.viewmodels.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeedLiveDataViewModel: ViewModel() {
    // MutableLiveData to hold the counter value
    private val commentsCounter = MutableLiveData<Int>()

    // Expose an immutable LiveData externally
    val counter: LiveData<Int>
        get() = commentsCounter

    init {
        // Initialize the counter value when ViewModel is created
        commentsCounter.value = 0
    }

    // Function to increment the counter value
    fun incrementCounter() {
        val updatedValue = (commentsCounter.value ?: 0) + 1
        commentsCounter.value = updatedValue
    }

    private val _booleanValue = MutableLiveData<Boolean>()

    // Expose an immutable LiveData externally
    val booleanValue: LiveData<Boolean>
        get() = _booleanValue

    init {
        // Initialize the boolean value when ViewModel is created
        _booleanValue.value = false
    }

    // Function to toggle the boolean value
    fun toggleBoolean() {
        _booleanValue.value = _booleanValue.value?.not()

    }
    // Function to set the boolean value
    fun setBoolean(value: Boolean) {
        _booleanValue.value = value
    }


}