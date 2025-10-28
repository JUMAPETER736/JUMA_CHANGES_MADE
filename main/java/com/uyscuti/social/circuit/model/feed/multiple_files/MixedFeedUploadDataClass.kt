package com.uyscuti.social.circuit.model.feed.multiple_files

import android.os.Parcelable
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleAudios
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
data class MixedFeedUploadDataClass(
    val images: FeedMultipleImages? = null,
    var videos: FeedMultipleVideos? = null,
    var audios: FeedMultipleAudios? = null,
    var documents: FeedMultipleDocumentsDataClass? = null,
    var fileId:String = generateRandomId(),
    var fileTypes: String = "",


): Parcelable, Serializable
