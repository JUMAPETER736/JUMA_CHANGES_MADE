package com.uyscuti.social.circuit.viewmodels

import androidx.lifecycle.ViewModel

class NotificationCountViewModel : ViewModel() {

    private var countValue: Int = 0


    fun resetCountValue() {
        countValue = 0
    }

    fun getCountValue(): Int {
        return countValue
    }
}
