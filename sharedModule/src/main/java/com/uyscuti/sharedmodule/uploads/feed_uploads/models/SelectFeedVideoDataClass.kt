package com.uyscuti.sharedmodule.uploads.feed_uploads.models

import android.net.Uri

data class SelectFeedVideoDataClass(
    val videoList : String,
    val videoUri: Uri,
    var isSelected: Boolean = false
)
