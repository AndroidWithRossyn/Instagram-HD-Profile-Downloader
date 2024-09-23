package com.instagram.hdprofile.downloader.utility

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instagram.hdprofile.downloader.MyApplication
import com.instagram.hdprofile.downloader.activitys.MainActivity
import com.instagram.hdprofile.downloader.api.ApiHelper
import com.instagram.hdprofile.downloader.customview.CustomDialog

import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility class that provides methods for checking external storage state and Downloading photos & videos
 */
object Utils {
    private var TAG_D = "checkDownlaod"
    private val TAG = "checkFileName"

    /**
     * Constant for the root destination folder where media will be saved.
     */
    const val DESTINATION_FOLDER = "/IsSave/"

    /**
     * Constant for the folder where videos will be stored.
     * folder name width is 6
     */
    const val VIDEOS_FOLDER = "Videos/"

    /**
     * Constant for the folder where images will be stored.
     * folder name width is 6
     */
    const val PHOTOS_FOLDER = "Images/"

    /**
     * Constant for the folder where Audios will be stored.
     * folder name width is 6
     */
    const val AUDIO_FOLDER = "Audios/"

    /**
     * Checks if external storage is available for write operations.
     *
     * @return True if the external storage is writable, false otherwise.
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Checks if external storage is read-only.
     *
     * @return True if the external storage is mounted as read-only, false otherwise.
     */
    fun isExternalStorageReadOnly(): Boolean {
        return Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()
    }

    /**
     * Extracts the filename from the given URL and replaces the file extension if necessary.
     *
     * @param url The URL from which the filename will be extracted.
     * @return The filename with an appropriate extension.
     */
    fun getFilenameFromURL(url: String): String {
        return try {
            if (url.contains(".webp")) {
                File(URI(url).path).name.replace(".webp", "") + ".jpg"
            } else if (url.contains(".mp4")) {
                File(URI(url).path).name
            } else {
                File(URI(url).path).name
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            if (url.contains(".jpg") || url.contains(".webp")) {
                System.currentTimeMillis().toString() + ".jpg"
            } else if (url.contains(".mp4")) {
                System.currentTimeMillis().toString() + ".mp4"
            } else if (url.contains(".m4a")) {
                System.currentTimeMillis().toString() + ".m4a"
            } else System.currentTimeMillis().toString()
        }
    }


    /**
     * Generates a new file name if the file already exists in the album.
     *
     * @param str The original file name.
     * @return A new file name with a unique suffix if the file exists.
     */
    private fun checkForAlreadyExistedFile(
        str: String
    ): String { // check if already download with same name
//      Log.d(TAG_D, "checkForAlreadyExistedFile  str " +str);
        var newstr: String = str

        newstr = getNewUrl(newstr)
        Log.d(
            TAG,
            "checkForAlreadyExistedFile: file name return:" + newstr.substring(Constants.FOLDER_LENGTH)
        )
        return newstr.substring(Constants.FOLDER_LENGTH)
    }

    /**
     * Generates a unique file name by appending a counter suffix if the file already exists.
     *
     * @param str       The original file name.
     * @param albumData The album data containing existing media files.
     * @return A unique file name.
     */
    private fun getNewUrl(
        str: String
    ): String { // get a new url when file available in album
        val temp = str.substring(0, str.length - 4)
        val mediaType = str.substring(str.length - 4)
        var count = 1
        val rand = "_${count++}"
        Log.d(TAG, "getNewUrl: uri: $temp , $rand , $mediaType")
        return temp + rand + mediaType
    }


    /**
     * Makes the decision on whether to download the Audio file and initiates the download process.
     *
     * @param context               The context of the activity.
     * @param str                   The URL of the file to download.
     * @param username              The username to append to the file name.
     * @param historyURL            A history URL to associate with the download.
     */
    fun downloadProfile(
        context: Context, str: String, username: String?, historyURL: String?
    ) {
        var temp = getFilenameFromURL(str)
        // check for duplicate file present or not
        temp = checkForAlreadyExistedFile("$DESTINATION_FOLDER$PHOTOS_FOLDER$username-$temp")

        startDownload(
            str, context, temp, "photo", 0, username, historyURL
        )
    }


    //    step 3

    /**
     * Starts the download process for a file.
     *
     * @param downloadPath      The URL to download from.
     * @param context           The context of the activity.
     * @param tempFileName      The temporary file name.
     * @param downloadType      The type of the file (photo or video).
     * @param currentCount      The current number of downloads.
     * @param username          The username for file naming.
     * @param historyURL        A URL associated with the download history.
     */
    private fun startDownload(
        downloadPath: String?,
        context: Context,
        tempFileName: String,
        downloadType: String,
        currentCount: Int,
        username: String?,
        historyURL: String?
    ) {
        if (currentCount == 0) {
            if (context is AppCompatActivity) {
                val activity = context as Activity
                activity.runOnUiThread {
                    if (context is MainActivity) {
                        CustomDialog.hideProgressDialog()
                    }
                    Toast.makeText(
                        context, "Download Started", Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
        val newDisUrl = historyURL?.replace(
            ApiHelper.BASE_URL, ""
        )

        Log.d(TAG, "startDownload downloadPath: $downloadPath")

        val uri = Uri.parse(downloadPath) // Path from where you want to download file.
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI) // Tell on which network you want to download file.
        request.setAllowedOverRoaming(true)
        request.setTitle("$username Profile")
        request.setDescription("$historyURL")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setVisibleInDownloadsUi(true) // necessary otherwise all downloads will be deleted in v11
        val subFolder = when (downloadType) {
            "audio" -> {
                AUDIO_FOLDER
            }

            "photo" -> {
                PHOTOS_FOLDER
            }

            else -> {
                VIDEOS_FOLDER
            }
        }
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS, DESTINATION_FOLDER + subFolder + tempFileName
        )
        Log.d(TAG, "startDownload file name : $tempFileName")
        val ID =
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request) // This will start downloading
        MyApplication.musicStoreCompletion.add(ID)


        /*
        * `MediaScannerConnection.scanFile()` is used to scan newly added media files (images, videos, etc.)
        *  so they immediately appear in media apps like the gallery or music player.
        *  Without it, new files may not show up until the system does an automatic scan later.
        * This method forces the system to detect and display the files right away.
        * */
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(File(Environment.DIRECTORY_DOWNLOADS + DESTINATION_FOLDER + subFolder + tempFileName).absolutePath),
                null
            ) { _: String?, _: Uri? -> }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}