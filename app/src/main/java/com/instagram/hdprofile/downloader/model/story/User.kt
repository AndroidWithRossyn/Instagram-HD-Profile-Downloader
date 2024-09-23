package com.instagram.hdprofile.downloader.model.story


import com.google.gson.annotations.SerializedName
import com.instagram.hdprofile.downloader.model.countModel


import java.io.Serializable

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("pk") val pk: Long,
    @SerializedName("pk_id") val pk_id: String,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val full_name: String,
    @SerializedName("is_private") val is_private: Boolean,
    @SerializedName("profile_pic_url") val profile_pic_url: String,
    @SerializedName("profile_pic_url_hd") val profile_pic_url_hd: String?,
    @SerializedName("profile_pic_id") val profile_pic_id: String,
    @SerializedName("is_verified") val is_verified: Boolean,
    @SerializedName("followed_by_viewer") val followed_by_viewer: Boolean,
    @SerializedName("media_count") val media_count: Long,
    @SerializedName("follower_count") val follower_count: Long,
    @SerializedName("following_count") val following_count: Long,
    @SerializedName("biography") val biography: String?,
    @SerializedName("total_igtv_videos") val total_igtv_videos: String,
    @SerializedName("hd_profile_pic_url_info") val hd_profile_pic_url_info: HDProfileModel?,
    @SerializedName("mutual_followers_count") val mutual_followers_count: Int,
    @SerializedName("profile_context") val profile_context: String,
    @SerializedName("edge_followed_by") val edge_followed_by: countModel?,
    @SerializedName("edge_follow") val edge_follow: countModel?,
    @SerializedName("edge_owner_to_timeline_media") val edge_owner_to_timeline_media: countModel?
) : Serializable

