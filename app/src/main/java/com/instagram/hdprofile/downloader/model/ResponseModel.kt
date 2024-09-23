package com.instagram.hdprofile.downloader.model



import com.google.gson.annotations.SerializedName

data class ResponseModel(
    @SerializedName("graphql") val graphql: Graphql?,
    @SerializedName("data") val data: UserData?,
)
