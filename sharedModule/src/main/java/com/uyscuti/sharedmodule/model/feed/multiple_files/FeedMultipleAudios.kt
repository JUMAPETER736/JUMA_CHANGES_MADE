package com.uyscuti.sharedmodule.model.feed.multiple_files

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
data class FeedMultipleAudios (
    var duration: String = "",
    var audioPath: String = "",
    var filename: String = ""
): Parcelable, Serializable