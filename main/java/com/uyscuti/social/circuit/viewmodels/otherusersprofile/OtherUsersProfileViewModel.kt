package com.uyscuti.social.circuit.viewmodels.otherusersprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OtherUsersProfileViewModel: ViewModel() {
    // MutableLiveData to hold the counter value
    private val openShortsPlayerFragment = MutableLiveData<Boolean>()

    // Expose an immutable LiveData externally
    val getOpenShortsPlayerFragment: LiveData<Boolean>
        get() = openShortsPlayerFragment

    init {
        // Initialize the counter value when ViewModel is created
        openShortsPlayerFragment.value = false
    }


    fun setOpenShortsPlayerFragment(value: Boolean) {
        openShortsPlayerFragment.value = value
    }


}