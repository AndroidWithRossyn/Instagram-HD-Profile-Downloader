package com.instagram.hdprofile.downloader.utility

import android.os.Environment

/**
 * Object that holds constant values used throughout the application.
 */
object Constants {

    /** Key for checking if WebView should be loaded again. */
    const val IS_LOAD_AGAIN = "isLoadAgain"

    /** The default user agent string for HTTPs requests. */
    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Windows NT 10.0; Win64; x64; rv:109.0;Mobile; LG-M255; rv:113.0; SM-A205U; LM-Q720; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.131 Mobile Safari/537.36 Instagram 282.0.0.22.119"



    /** The length of the combined folder paths for destination and photos. */
    const val FOLDER_LENGTH = Utils.DESTINATION_FOLDER.length + Utils.PHOTOS_FOLDER.length

    /** The length of the path to the public Downloads directory. */
    val DIRECTORY_DOWNLOADS_LENGTH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath.length // don't change this: storage/emulated/0/Download/

    /** The absolute path to the public Downloads directory for storing photos. */
    val DIR_PHOTOS: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    /** The absolute path to the public Downloads directory for storing videos. */
    val DIR_VIDEOS: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath


}