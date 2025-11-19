package com.uyscuti.social.circuit.model.feed.multiple_files

import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos

data class MixedFeedFilesClass(
    var images: ArrayList<String> = ArrayList(),
    var videos: ArrayList<FeedMultipleVideos> = ArrayList(),
    var filePaths: MutableList<String> = mutableListOf(),
    var fileType:String = "image",
    var total:Int = 0
)
