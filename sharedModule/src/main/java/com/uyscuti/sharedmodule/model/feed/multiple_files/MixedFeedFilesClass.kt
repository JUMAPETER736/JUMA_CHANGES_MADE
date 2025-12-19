package com.uyscuti.sharedmodule.model.feed.multiple_files


data class MixedFeedFilesClass(
    var images: ArrayList<String> = ArrayList(),
    var videos: ArrayList<FeedMultipleVideos> = ArrayList(),
    var filePaths: MutableList<String> = mutableListOf(),
    var fileType:String = "image",
    var total:Int = 0
)
