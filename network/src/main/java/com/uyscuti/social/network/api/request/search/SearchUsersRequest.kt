package com.uyscuti.social.network.api.request.search

import com.google.gson.annotations.SerializedName

data class SearchUsersRequest(
    @SerializedName("query") val query: String
)

