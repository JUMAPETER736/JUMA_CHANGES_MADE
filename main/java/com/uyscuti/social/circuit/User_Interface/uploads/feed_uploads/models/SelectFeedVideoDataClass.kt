package com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.models

import android.net.Uri

data class SelectFeedVideoDataClass(
    val videoList : String,
    val videoUri: Uri,
    var isSelected: Boolean = false
)
