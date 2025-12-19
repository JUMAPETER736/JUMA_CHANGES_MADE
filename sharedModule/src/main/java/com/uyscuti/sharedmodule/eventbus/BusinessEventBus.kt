package com.uyscuti.sharedmodule.eventbus

import com.uyscuti.sharedmodule.model.Catalogue

data class BusinessCommentsClicked(
    val position: Int,
    val data: Catalogue
)