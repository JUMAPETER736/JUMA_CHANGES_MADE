package com.uyscuti.sharedmodule.model

import android.graphics.Bitmap

data class SettingsModel(
    val imageBitmap: Bitmap?,
    val title: String,
    var subTitle: String? = null,
    // Add these new fields for user data
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null
)