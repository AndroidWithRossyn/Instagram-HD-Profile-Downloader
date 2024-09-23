package com.instagram.hdprofile.downloader.utility

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
/**
 * Utility class for handling runtime permissions in Android.
 *
 * @property activity The activity context.
 * @property mPermissionResult ActivityResultLauncher for permission requests.
 */
class PermissionUtility(
    private val activity: Activity,
    private val mPermissionResult: ActivityResultLauncher<Array<String>>
) {
    /**
     * Requests relevant permissions based on the Android version.
     */
    fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS

            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        mPermissionResult.launch(permissions)
    }

    /**
     * Checks if required permissions are granted.
     * @return true if permissions are granted, false otherwise.
     */
    fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readPhotoStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_IMAGES
            )
            val readVideoStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_VIDEO
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val selectedImageVideo = ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
                (readPhotoStoragePermission == PackageManager.PERMISSION_GRANTED && readVideoStoragePermission == PackageManager.PERMISSION_GRANTED) || (selectedImageVideo == PackageManager.PERMISSION_GRANTED)
            } else {
                readPhotoStoragePermission == PackageManager.PERMISSION_GRANTED && readVideoStoragePermission == PackageManager.PERMISSION_GRANTED
            }
        } else {
            val readExternalStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            readExternalStoragePermission == PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        /**
         * Gets the status of a specific permission.
         * @param activity The activity context.
         * @param androidPermissionName The permission to check.
         * @return "granted", "denied", or "blocked".
         */
        fun getPermissionStatus(activity: Activity, androidPermissionName: String): String {
            return if (ContextCompat.checkSelfPermission(
                    activity, androidPermissionName
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, androidPermissionName
                    )
                ) {
                    "blocked"
                } else {
                    "denied"
                }
            } else {
                "granted"
            }
        }
    }
}
