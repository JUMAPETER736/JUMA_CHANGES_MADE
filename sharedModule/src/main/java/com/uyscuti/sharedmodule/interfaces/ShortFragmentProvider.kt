package com.uyscuti.sharedmodule.interfaces

import androidx.fragment.app.Fragment

interface ShortFragmentProvider {
    fun createShortFragment(): Fragment
}