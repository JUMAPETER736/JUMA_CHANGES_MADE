package com.uyscuti.social.compressor.config

import com.uyscuti.social.compressor.VideoQuality

data class Configuration(
    var quality: VideoQuality = VideoQuality.MEDIUM,
    var isMinBitrateCheckEnabled: Boolean = true,
    var videoBitrateInMbps: Int? = null,
    var disableAudio: Boolean = false,
    val keepOriginalResolution: Boolean = false,
    var videoHeight: Double? = null,
    var videoWidth: Double? = null,
    var videoNames: List<String>,
//    val customVideoNames: List<String>? = null
)

data class AppSpecificStorageConfiguration(
    var subFolderName: String? = null,
)

data class SharedStorageConfiguration(
    var saveAt: SaveLocation? = null,
    var subFolderName: String? = null,
)

enum class SaveLocation {
    pictures,
    downloads,
    movies,
}