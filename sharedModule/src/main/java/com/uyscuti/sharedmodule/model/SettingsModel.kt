package com.uyscuti.sharedmodule.model

import android.graphics.Bitmap
import android.widget.ImageView

data class SettingsModel(
    val imageBitmap: Bitmap?,
    val title: String,
    var subTitle: String? = null,
    )
