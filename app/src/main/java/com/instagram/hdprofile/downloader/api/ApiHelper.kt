package com.instagram.hdprofile.downloader.api

import android.os.Build
import android.util.Log
import com.instagram.hdprofile.downloader.model.ResponseModel
import com.instagram.hdprofile.downloader.model.UserInfoForSingleStoryDownload
import com.instagram.hdprofile.downloader.utility.Helper
import com.instagram.hdprofile.downloader.utility.SharedPref

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers

import java.net.URI
import java.net.URISyntaxException

object ApiHelper {

    private val TAG = "ApiHelper"

    const val BASE_URL = "https://www.instagram.com/"

    const val LOGIN_URL = "https://www.instagram.com/accounts/login/"

    const val BASE_URL_MOB = "https://i.instagram.com/"

    const val EXPLORE_URL = "https://www.instagram.com/explore/"

    const val SCRIPT_PARAMETERS = "?__a=1&__d=dis"

    const val REELS_TRAY = "https://i.instagram.com/api/v1/feed/reels_tray/"

    const val FEED_REEL_MEDIA = "https://i.instagram.com/api/v1/feed/reels_media/"
    const val GRAPHQL_QUERY = "https://i.instagram.com/graphql/query/"

    const val POST_API = "https://i.instagram.com/api/v1/media/%s/info/"

    fun profileByUserId(userId: String): String {
        return "https://i.instagram.com/api/v1/users/$userId/info/"
    }


    fun profileByUsername(username: String): String {
        return "https://www.instagram.com/api/v1/users/web_profile_info/?username=$username"
    }


    fun searchQuery(text: String): String {
        return "https://www.instagram.com/web/search/topsearch/?context=blended&query=$text&include_reel=false"
    }


    fun getUrlWithoutParameters(url: String?): String {
        return try {
            val uri = URI(url)
            val authority = uri.getAuthority()
            if ("instagram.com" == authority || "www.instagram.com" == authority) {
                URI(
                    uri.scheme,
                    authority,
                    uri.getPath(),
                    null,  // Ignore the query part of the input url
                    uri.getFragment()
                ).toString()
            } else ""
        } catch (e: URISyntaxException) {
            ""
        }
    }


    val headerMapDefault = HashMap<String, String>().apply {
        put("User-Agent", SharedPref.userAgent())
        put("X-IG-App-ID", SharedPref.xIgAppId!!)
        put("Accept", "*/*")
        put("sec-ch-ua-platform", "Android")
        put("sec-ch-ua-model", Build.MODEL)
        put("sec-ch-ua-platform-version", Build.VERSION.RELEASE)
        put("Accept-Language", "en-US,en;q=0.9")
        put("X-ASBD-ID", SharedPref.xAsbID!!)
        put("X-IG-WWW-Claim", SharedPref.xClaimId!!)
        put("Origin", "https://www.instagram.com")
        put("DNT", "1")
        put("Connection", "keep-alive")
        put("Sec-Fetch-Dest", "empty")
        put("Sec-Fetch-Mode", "cors")
        put("Sec-Fetch-Site", "same-origin")
        put("Sec-GPC", "1")
        put("TE", "trailers")
    }




    fun callResult(
        observer: DisposableObserver<ResponseModel>, url: String, cookies: String
    ) {
        ApiDataCache.get(
            url, ResponseModel::class.java
        ) { result ->
            if (result != null) {
                Log.d(ApiDataCache.TAG, "Retrieved list: $result")
                observer.onNext(result)
            } else {
                Log.d(ApiDataCache.TAG, "api called")

                RestClient.getService().callResult(
                        url, cookies,Helper.extractCsrftoken(cookies), headerMapDefault
                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<ResponseModel> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(o: ResponseModel) {
                            observer.onNext(o)
                            ApiDataCache.put(
                                url, o
                            )
                        }

                        override fun onError(e: Throwable) {
                            observer.onError(e)
                        }

                        override fun onComplete() {
                            observer.onComplete()
                        }
                    })
            }
        }
    }

    fun getStoryUserIdForDownload(
        observer: DisposableObserver<UserInfoForSingleStoryDownload>, url: String, cookies: String
    ) {
        ApiDataCache.get(
            url, UserInfoForSingleStoryDownload::class.java
        ) { result ->
            if (result != null) {
                Log.d(ApiDataCache.TAG, "Retrieved list: $result")
                observer.onNext(result)
            } else {
                Log.d(ApiDataCache.TAG, "api called")

                RestClient.getService().getUserIdForStoryDownload(
                    url, cookies,Helper.extractCsrftoken(cookies), headerMapDefault
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<UserInfoForSingleStoryDownload> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(o: UserInfoForSingleStoryDownload) {
                            observer.onNext(o)
                            ApiDataCache.put(url, o)
                        }

                        override fun onError(e: Throwable) {
                            observer.onError(e)
                        }

                        override fun onComplete() {
                            observer.onComplete()
                        }
                    })
            }
        }
    }




}