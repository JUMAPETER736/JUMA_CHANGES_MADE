package com.uyscuti.social.circuit

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class MyViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}
