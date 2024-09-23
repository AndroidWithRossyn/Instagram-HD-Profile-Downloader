package com.instagram.hdprofile.downloader.api


import com.instagram.hdprofile.downloader.model.ResponseModel
import com.instagram.hdprofile.downloader.model.UserInfoForSingleStoryDownload
import io.reactivex.rxjava3.core.Observable

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Url

interface ApiServices {

    @GET
    fun callResult(
        @Url url: String,
        @Header("Cookie") cookie: String,
        @Header("x-csrftoken") xCsrftoken: String,
        @HeaderMap headers: Map<String, String>
    ): Observable<ResponseModel>

    @GET
    fun getUserIdForStoryDownload(
        @Url url: String,
        @Header("Cookie") cookies: String,
        @Header("x-csrftoken") xCsrftoken: String,
        @HeaderMap headers: Map<String, String>
    ): Observable<UserInfoForSingleStoryDownload>

}
