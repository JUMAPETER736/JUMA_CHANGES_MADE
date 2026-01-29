package com.uyscuti.sharedmodule.model.feed.multiple_files

import android.os.Parcelable
import com.uyscuti.sharedmodule.model.feed.FeedMultipleImages
import com.uyscuti.sharedmodule.utils.generateRandomId
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
data class MixedFeedUploadDataClass(
    val images: FeedMultipleImages? = null,
    var videos: FeedMultipleVideos? = null,
    var audios: FeedMultipleAudios? = null,
    var documents: FeedMultipleDocumentsDataClass? = null,
    var fileId:String = generateRandomId(),
    var fileTypes:String

): Parcelable, Serializable
