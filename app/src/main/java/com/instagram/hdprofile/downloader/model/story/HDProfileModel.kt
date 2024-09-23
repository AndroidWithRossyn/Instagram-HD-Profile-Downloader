package com.instagram.hdprofile.downloader.model.story

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HDProfileModel(
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("url") val url: String
) : Serializable
