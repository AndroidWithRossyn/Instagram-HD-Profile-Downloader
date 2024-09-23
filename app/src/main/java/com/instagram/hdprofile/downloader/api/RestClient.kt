package com.instagram.hdprofile.downloader.api

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.instagram.hdprofile.downloader.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * A singleton object for configuring and providing a Retrofit instance with OkHttpClient for network operations.
 */
object RestClient {

    /**
     * Configures an `HttpLoggingInterceptor` to log HTTP request and response data.
     *
     * The logging level is set based on the build type:
     * - In debug builds (`BuildConfig.DEBUG` is `true`), the logging level is set to `BODY`,
     *   which logs the entire request and response, including headers and payloads.
     * - In release builds (`BuildConfig.DEBUG` is `false`), the logging level is set to `NONE`,
     *   which disables logging to prevent sensitive information from being exposed.
     */
    private val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * Configured OkHttpClient with timeouts and logging interceptor.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(httpLoggingInterceptor)
        .build()

    /**
     * Configured Retrofit instance for API requests.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiHelper.BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setStrictness(Strictness.LENIENT)
                        .setPrettyPrinting()
                        .create()
                )
            )
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides an instance of ApiServices for making API calls.
     *
     * @return An instance of ApiServices.
     */
    fun getService(): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }
}
