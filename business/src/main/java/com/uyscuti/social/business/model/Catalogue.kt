package com.uyscuti.social.business.model

import java.io.Serializable

data class Catalogue(
    var id: String,
    var name: String,
    var description: String,
    var price: String,
    var images: List<String>,
    var isSelected: Boolean = false
): Serializable {
}