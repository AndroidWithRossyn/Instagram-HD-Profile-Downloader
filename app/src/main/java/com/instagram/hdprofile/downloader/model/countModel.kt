package com.instagram.hdprofile.downloader.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class countModel(
    @SerializedName("count")
    val count: String
) : Serializable
