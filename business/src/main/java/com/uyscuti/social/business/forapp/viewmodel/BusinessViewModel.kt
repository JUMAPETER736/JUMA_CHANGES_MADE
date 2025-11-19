package com.uyscuti.social.business.forapp.viewmodel

import androidx.lifecycle.ViewModel

class BusinessViewModel : ViewModel() {
    private var myValue: Boolean = false // Example value to store

    // You can also create functions to update or retrieve the value
    fun setValue(newValue: Boolean) {
        myValue = newValue
    }

    fun getValue(): Boolean {
        return myValue
    }
}
