package com.uyscuti.sharedmodule.eventbus

import android.os.Bundle
import androidx.fragment.app.Fragment

data class FragmentEvents(
    val fragmentType: String,
    val args: Bundle? = null,
    var fragment: Fragment? = null
)


