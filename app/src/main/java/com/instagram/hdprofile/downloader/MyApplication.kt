package com.instagram.hdprofile.downloader

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.multidex.MultiDex
import com.google.android.material.color.DynamicColors
import com.gu.toolargetool.TooLargeTool
import com.instagram.hdprofile.downloader.api.ApiDataCache
import com.instagram.hdprofile.downloader.utility.SharedPref

/**
 * This is the Application class for the IsSave application.
 *
 * It initializes application-wide resources and configurations.
 *
 * ## @insta-downloader
 *
 * Project Created on: Sep 21, 2024
 * @author Ban Rossyn
 * @since v1.0.0
 * @see <a href="mailto:banrossyn@gmail.com">banrossyn@gmail.com</a>
 */
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    companion object {

        private val TAG ="MyApplication"

        /**
         * Instance of the application class [MyApplication] which provides the context of the app.
         * This is used to get the application-level context anywhere within the app.
         */
        lateinit var instance: MyApplication

        /**
         * Returns the application context from the [instance] of [MyApplication].
         *
         * @return the application-level context.
         */
        fun getAppContext(): Context = instance.applicationContext


        var musicStoreCompletion :MutableList<Long> = ArrayList()
    }

    override fun onCreate() {
        super.onCreate()

        instance = this // Assigns the current instance of the application class
//        DynamicColors.applyToActivitiesIfAvailable(this)     // Apply dynamic color theming to all activities

        SharedPref.init(applicationContext) // Initializes Shared Preferences for the first time
        ApiDataCache.initialize(applicationContext) // Initializes Shared Preferences for the first time


        TooLargeTool.startLogging(this)  // Enables logging for TooLargeTool to catch TooLargeException (useful for debugging)

    }


    /**
     * Sets up MultiDex support and initializes application context.
     * Registers activity lifecycle callbacks.
     *
     * @param base The base context to attach.
     */
    override fun attachBaseContext(base: Context?) {
        Log.d(TAG, "attachBaseContext: ")
        MultiDex.install(this)
        super.attachBaseContext(base)
    }


    override fun onTerminate() {
        TooLargeTool.stopLogging(this)
        Log.d("MyAppIsSave", "onTerminate Application")
        super.onTerminate()
    }

    /**
     * Called when the activity is created.
     *
     * This method is part of the Activity lifecycle callback in the application, and it is triggered whenever any activity in the app is created.
     */
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        // Initializes the [SharedPref] if it has not been initialized yet.
        if (!SharedPref.isInitialized()) {
            SharedPref.init(applicationContext)
        }
    }

    override fun onActivityStarted(p0: Activity) {

    }

    override fun onActivityResumed(p0: Activity) {

    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStopped(p0: Activity) {

    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityDestroyed(p0: Activity) {

    }


}