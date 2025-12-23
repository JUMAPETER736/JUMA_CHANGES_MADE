package com.uyscuti.social.circuit.User_Interface.fragments

//import com.uyscut.flashdesign.adapter.TAG

//////////////////////////////////////////////////////////////
//private fun playNextVideo() {
//    Log.d("PlayerActivity", "Ready to play next video")
//    player?.seekTo(0)
//    player?.play()
//    // Move to the next video (assuming there is a list of video URIs)
////        mediaItemIndex++
////        if (mediaItemIndex < yourListOfVideoUris.size) {
////            val nextVideoUri = yourListOfVideoUris[mediaItemIndex]
////            val nextMediaItem = MediaItem.fromUri(nextVideoUri)
////            player.setMediaItem(nextMediaItem)
////            player.prepare()
////            player.playWhenReady = playWhenReady
////        }
////        else {
////            // No more videos to play, handle as needed
////        }
//}
//                videoView.useController = false
//
//                // Create a list of MediaItems from the provided URLs
////                val mediaItems: List<MediaItem> = urlList.map { MediaItem.fromUri(it) }
////                val mediaSources = mediaItems.map {
////                    ProgressiveMediaSource.Factory(cacheDataSourceFactory)
////                        .createMediaSource(it)
////                }
//
//                val videoUri = Uri.parse(url)
//                val mediaItem = MediaItem.fromUri(videoUri)
//                val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
//                    .createMediaSource(mediaItem)
//
//                exoPlayer.setMediaSource(mediaSource, true)
//                exoPlayer.addListener(playbackStateListener)
//                exoPlayer.addListener(object : Player.Listener {
//                    @Deprecated("Deprecated in Java")
//                    override fun onPlayerStateChanged(
//                        playWhenReady: Boolean,
//                        playbackState: Int
//                    ) {
//                        if (playbackState == Player.STATE_READY && exoPlayer.duration != C.TIME_UNSET) {
//                            shortsSeekBar.max = exoPlayer.duration.toInt()
//                        }
//                    }
//
//                    override fun onPlayerError(error: PlaybackException) {
//                        super.onPlayerError(error)
//                        Log.d(TAG, "Error occurred because: $error")
//                        Log.d(TAG, "Error occurred because: ${error.message}")
//                        Log.d(TAG, "Error occurred because: ${error.cause}")
//                        error.printStackTrace()
//                        Toast.makeText(
//                            itemView.context,
//                            "Can't play this video",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                    }
//                })
//
//                // Update SeekBar on position change
//                exoPlayer.addListener(object : Player.Listener {
//                    @Deprecated("Deprecated in Java")
//                    override fun onPositionDiscontinuity(reason: Int) {
//                        // Update SeekBar on position discontinuity
//                        updateSeekBar()
//                    }
//                })
//
//                exoPlayer.prepare()
//
//                if (absoluteAdapterPosition == 0) {
//                    exoPlayer.playWhenReady = true
//                    exoPlayer.play()
//                }
//                videoPreparedListener.onVideoPrepared(
//                    ExoPlayerItem(
//                        exoPlayer,
//                        absoluteAdapterPosition
//                    )
//                )
//            }
//
//
//    }

//private fun showProgressBar() {
//    progressBar.visibility = View.VISIBLE
//}
//
//private fun hideProgressBar() {
//    if (!(player!!.isPlaying)) {
////            player!!.play()
//        Log.d(TAG, "Player play video")
//    } else {
////            player!!.stop()
//        Log.d(TAG, "Player don't play video")
//    }
//    progressBar.visibility = View.GONE
//}


//    private fun playbackStateListener() = object : Player.Listener {
//        override fun onPlaybackStateChanged(state: Int) {
//            when (state) {
//                ExoPlayer.STATE_ENDED -> {
//                    // The video playback ended. Move to the next video if available.
//                    playNextVideo()
//                    hideProgressBar()
//                }
//                // Add other cases if needed
//                Player.STATE_BUFFERING -> {
//                    Log.d(TAG, "STATE_BUFFERING")
//                    showProgressBar()
//                }
//
//                Player.STATE_IDLE -> {
//                    Log.d(TAG, "STATE_IDLE")
//                    showProgressBar()
//                }
//
//                Player.STATE_READY -> {
//                    Log.d(TAG, "STATE_READY")
//
//                    hideProgressBar()
//                    startUpdatingSeekBar()
//                }
//
//                else -> {
//                    // Stop updating seek bar in other states
//                    stopUpdatingSeekBar()
//                }
//            }
//        }
//
//        private var updateSeekBarJob: Job? = null
//
//        private fun startUpdatingSeekBar() {
//            updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
//                while (true) {
//                    // Update seek bar based on current playback position
//                    updateSeekBar()
//                    delay(200) // Update seek bar every second (adjust as needed)
//                }
//            }
//        }
//
//        private fun stopUpdatingSeekBar() {
//            updateSeekBarJob?.cancel()
//        }
//
//        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {
////        super.onIsPlayingChanged(isPlaying)
//
//            val playingString: String = if (isVideoPlaying) "Playing" else "Not playing"
//            Log.d(TAG, "player currently is $playingString")
//            isPlaying = true
//        }
//
//        override fun onEvents(player: Player, events: Player.Events) {
////        super.onEvents(player, events)
//            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
//                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
//            ) {
//                Log.d(TAG, "player ui needs to be updated")
////                if(!player.isPlaying) {
////                    player.play()
////                    Log.d(TAG, "player is not playing")
////                }else {
////                    Log.d(TAG, "player is playing")
////                }
//                progressBar.visibility = View.GONE
//            }
//
//            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
//            ) {
//                player.seekTo(5000L)
//            }
//        }
//    }

//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
//////////////////////////view pager listeners/////////////////
//
//                    override  fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
////                        Log.d("Exoplayer", "onPageScrolled")
//
//                        if (position > lastPosition) {
//                            // User is scrolling down
////                            Log.d("Exoplayer", "User scrolling down")
//                            loadMoreVideosIfNeeded(position)
//                        } else {
////                            loadMoreVideosIfNeeded(position)
//
////                            Log.d("Exoplayer", "User scrolling up or no change in position")
//                            val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
//                            val player = exoPlayerItems[newIndex].exoPlayer
//                            if(!player.isPlaying && position == newIndex) {
////                                player.playWhenReady = true
////                                player.seekTo(0)
////                                player.play()
////                                Log.d("Exoplayer", "onPageScrolled Exoplayer Current Video Playing")
//
//                            }else {
////                                Log.d("Exoplayer", "onPageScrolled Exoplayer Current Video not Playing")
//                            }
//                        }
//
//                        lastPosition = position
//                    }

//                    override fun onPageScrollStateChanged(state: Int) {
//                        super.onPageScrollStateChanged(state)
//                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
//                            // Ensure ExoPlayer instances are playing when scrolling is idle
////                            exoPlayerItems.forEach { playerItem ->
////                                playerItem.exoPlayer.playWhenReady = true
////                                playerItem.exoPlayer.play()
////                            }
//                            Log.d("TAG", "onPageScrollStateChanged SCROLL_STATE_IDLE")
//                            val player = exoPlayerItems[viewPager.currentItem].exoPlayer
//                            Log.d("TAG","current item: ${exoPlayerItems[viewPager.currentItem]}")
////                            val newIndex = exoPlayerItems.indexOfFirst { it.position == viewPager.currentItem }
//                            if(!player.isPlaying) {
//
//
//                                requireActivity().runOnUiThread {
//                                    player.play()
//                                }
//
////                                CoroutineScope(Dispatchers.Main).launch {
////                                    delay(500)
////                                    player.play()
////                                }
//
////                                Handler(Looper.getMainLooper()).post {
////                                    player.play()
////                                }
//
////                                mainHandler.post {
////                                    player.play()
////                                }
////                                player.play()
//                            }
//                        }
//                    }


//                when (viewPager.scrollState) {
//                    ViewPager2.SCROLL_STATE_IDLE -> {
//                        // Page is not scrolling
//                        Log.d("TAG", " scrollState SCROLL_STATE_IDLE")
//
//                    }
//                    ViewPager2.SCROLL_STATE_DRAGGING -> {
//                        // User is dragging the page
//                        Log.d("TAG", "SCROLL_STATE_DRAGGING")
//                    }
//                    ViewPager2.SCROLL_STATE_SETTLING -> {
//                        // Page is settling after a scroll
//                        Log.d("TAG", "SCROLL_STATE_SETTLING")
//                    }
//                }

//////////////////end of viewpager listeners//////////

/////////////////view with the observer///////////////


//        shortsViewModel = ViewModelProvider(this)[ShortsViewModel::class.java]

//        shortsViewModel.allShorts.observe(viewLifecycleOwner) { entities ->
//
//            Log.d("TAG", "In fragment: $entities")
//            Log.d("TAG", "In fragment entities size: ${entities.size}")
//
//            shortsAdapter = ShortsAdapter(this@ShotsFragment, entities, object :
//
//                OnVideoPreparedListener {
//                override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
//                    exoPlayerItems.add(exoPlayerItem)
//                }
//            })
//            viewPager.adapter = shortsAdapter
//            // Recreate the activity with the updated theme
////        requireActivity().recreate()
//            // Set orientation to vertical
//            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
//
//            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
//                    if (previousIndex != -1) {
//                        val player = exoPlayerItems[previousIndex].exoPlayer
//                        player.pause()
//                        player.playWhenReady = false
//                        player.seekTo(0)
//                    }
//                    val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
//                    if (newIndex != -1) {
//                        val player = exoPlayerItems[newIndex].exoPlayer
//                        player.playWhenReady = true
//                        player.seekTo(0)
//                        player.play()
//                    }
//                }
//            })
////                viewPager.offscreenPageLimit = 1
//        }
/////////////////////////////////////////////////////

/////////////////shorts adapter////////////////////

//                val concatenatingMediaSource = ConcatenatingMediaSource(*mediaSources.toTypedArray())
//                val mediaSource =
//                    ProgressiveMediaSource.Factory(cacheDataSourceFactory)
//                        .createMediaSource(MediaItem.fromUri(mediaItems.toString()))
//                exoPlayer.setMediaSource(concatenatingMediaSource)
//                exoPlayer.release()

// Set the list of MediaItems to the player
//                exoPlayer.setMediaItems(mediaItems)
//                exoPlayer.playWhenReady = playWhenReady
//                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
///////////////////////////////////////////////////
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
//private fun uploadShorts(videoUri: Uri){
//
//    uris.add(videoUri)
//
//    // 1. compress shorts
//    compressShorts()
//}
//
//@SuppressLint("SetTextI18n")
//private fun compressShorts() {
////        binding.mainContents.visibility = View.VISIBLE
//
//    lifecycleScope.launch {
//        VideoCompressor.start(
//            context = applicationContext,
//            uris,
//            isStreamable = true,
//            sharedStorageConfiguration = SharedStorageConfiguration(
//                saveAt = SaveLocation.movies,
//                subFolderName = "flash-shorts"
//            ),
////                appSpecificStorageConfiguration = AppSpecificStorageConfiguration(
////
////                ),
//            configureWith = Configuration(
//                quality = VideoQuality.MEDIUM,
//                videoNames = uris.map { uri -> uri.pathSegments.last() },
//                isMinBitrateCheckEnabled = true,
//
//                ),
//            listener = object : CompressionListener {
//                override fun onProgress(index: Int, percent: Float) {
//                    //Update UI
////                        if (percent <= 100)
////                            runOnUiThread {
////                                data[index] = VideoDetailsModel(
////                                    "",
////                                    uris[index],
////                                    "",
////                                    percent
////                                )
////                                adapter.notifyDataSetChanged()
////                            }
//                }
//
//                override fun onStart(index: Int) {
////                        data.add(
////                            index,
////                            VideoDetailsModel("", uris[index], "")
////                        )
////                        runOnUiThread {
////                            adapter.notifyDataSetChanged()
////                        }
//
//                    Log.d("Compress", "short compress successful")
//
//                }
//
//                override fun onSuccess(index: Int, size: Long, path: String?) {
////                        data[index] = VideoDetailsModel(
////                            path,
////                            uris[index],
////                            getFileSize(size),
////                            100F
////                        )
////                        runOnUiThread {
////                            adapter.notifyDataSetChanged()
////                        }
//
//                    Log.d("Compress", "short compress successful")
//                    Log.d("Compress", "short file size: ${getFileSize(size)}")
//                    Log.d("Compress", "short path: $path")
//
//                    uploadShortToMongoDB(File(path))
//                }
//
//                override fun onFailure(index: Int, failureMessage: String) {
//                    Log.wtf("failureMessage", failureMessage)
//                }
//
//                override fun onCancelled(index: Int) {
//                    Log.wtf("TAG", "compression has been cancelled")
//                    // make UI changes, cleanup, etc
//                }
//            },
//        )
//    }
//}
//
//private fun uploadShortToMongoDB(file: File) {
//
//    // Convert file content to bytes
//    val fileBytes = file.readBytes()
//
//    val requestFile: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes)
//    val filePart: MultipartBody.Part =
//        MultipartBody.Part.createFormData("images", file.name, requestFile)
//
//    val tags = listOf("Tag1", "Tag2") // Replace with your actual list of tags
//
//    val formData = MultipartBody.Builder().setType(MultipartBody.FORM)
//
//    // Add other form data appends
//    formData.addPart(filePart)
//
//    // Append tags with index-based keys
//    tags.forEachIndexed { index, tag ->
//        formData.addFormDataPart("tags[$index]", tag)
//    }
//
//    val tagParts = tags.mapIndexed { index, tag ->
//        RequestBody.create("text/plain".toMediaTypeOrNull(), tag)
//    }
//    val contentPart: RequestBody = RequestBody.create(
//        "text/plain".toMediaTypeOrNull(),
//        "content here..."
//    )
//    GlobalScope.launch(Dispatchers.IO) {
////            val response = retrofitIns.apiService.uploadShort(filePart, contentPart, tagsRequestBody)
//        val response = retrofitIns.apiService.uploadShort(
//            content = contentPart,
//            images = filePart,
//            tagParts.toTypedArray()
//        )
//        if (response.isSuccessful) {
//            // Existing code for a successful response...
//            Log.i("Shorts","Shorts upload successful")
//            Log.i("Shorts","Shorts ${response.body()!!.data}")
//        }else {
//            Log.i("Shorts","Shorts upload failed ${response.message()}")
//            Log.i("Shorts","Shorts upload failed ${response.code()}")
//            Log.i("Shorts","Shorts upload failed ${response.body()?.message}")
//            Log.i("Shorts", "Shorts error response body: ${response.errorBody()?.string()}")
//        }
//    }
//
//}
//
//private fun backFromShortsUpload() {
//    binding.backButton.setOnClickListener {
//        finish()
//    }
//}
//private  fun cancelShortsUpload() {
//    binding.cancelButton.setOnClickListener {
//        finish()
//    }
//}
//companion object {
//    const val EXTRA_VIDEO_URI = "extra_video_uri"
//}
//
//
//private fun addIdToFilePath(originalPath: String, id: String): String {
//    val file = File(originalPath)
//    val fileName = file.nameWithoutExtension
//    val fileExtension = file.extension
//
//    // Construct the new path with the ID inserted before the extension
////        Log.i(offlineTag, "New file path - ${file.parent}/$fileName$id.$fileExtension")
//    return "${file.parent}/$fileName$id.$fileExtension"
//}
//
//private fun showToast(message: String) {
//    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//}

