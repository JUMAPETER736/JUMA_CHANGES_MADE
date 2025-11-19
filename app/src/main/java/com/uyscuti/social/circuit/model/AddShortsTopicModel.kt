package com.uyscuti.social.circuit.model

import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.TextView

data class AddShortsTopicModel(
    var shortsTopicIcon: Int,
    var shortsTopicTitle: String,
    var subTopics: List<String>
    )
