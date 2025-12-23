package com.uyscuti.social.circuit.presentation

import androidx.lifecycle.ViewModel

class LikeUnLikeViewModel : ViewModel() {
    // Properties to store String and Boolean values
    private var myString: String = ""
    private var myBoolean: Boolean = false

    var isLiked: Boolean = false
    var totalLikes: Int = 0
    // Getter methods for accessing values
    fun getMyString(): String {
        return myString
    }

    fun getMyBoolean(): Boolean {
        return myBoolean
    }

    // Setter methods for updating values
    fun setMyString(newString: String) {
        myString = newString
    }

    fun setMyBoolean(newBoolean: Boolean) {
        myBoolean = newBoolean
    }
}
