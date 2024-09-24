package com.instagram.hdprofile.downloader.utility


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.instagram.hdprofile.downloader.MyApplication
import com.instagram.hdprofile.downloader.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object providing common methods for displaying UI messages & functions.
 */
object Helper {

    /**
     * Displays a Snackbar with a short duration on the provided view.
     *
     * @param view The view to anchor the Snackbar to.
     * @param string The message to display in the Snackbar.
     */
    fun showSnackBarShort(view: View, string: String) {
        Snackbar.make(
            view, string, Snackbar.LENGTH_SHORT
        ).setTextColor(
            MyApplication.getAppContext().resources.getColor(
                R.color.md_theme_onPrimary, MyApplication.getAppContext().theme
            )
        ).setBackgroundTint(
            MyApplication.getAppContext().resources.getColor(
                R.color.md_theme_primary, MyApplication.getAppContext().theme
            )
        ).show()
    }

    /**
     * Displays a Toast message with a short duration.
     *
     * @param string The message to display in the Toast.
     */
    fun Context.showToastShort(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }



    /**
     * This function searches for a key-value pair in the format `key=value` within the input string,
     * where the value is terminated by a semicolon (`;`) or the end of the string. It returns the value
     * corresponding to the provided key, or `null` if the key is not found.
     *
     * @param input The input string containing the key-value pairs (e.g., "key1=value1;key2=value2;").
     * @param key The key whose associated value needs to be extracted.
     * @return The value associated with the provided key, or `null` if the key is not found.
     */
    fun extractValue(input: String, key: String): String? {
        val regex = Regex("$key=([^;]*)")
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1)
    }

    fun extractCsrftoken(cookies: String): String {
        val returnValue = extractValue(cookies, "csrftoken")
        return returnValue!!
    }
    /**
     * Copies text to clipboard and optionally shows a confirmation message.
     *
     * @param context The context to access system services.
     * @param view The view to show the Snackbar on, or null to skip showing a message.
     * @param text The text to be copied.
     */
    fun URLCopy(context: Context, view: View?, text: String?) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("COPY", text)
        manager.setPrimaryClip(clip)
        if (view != null) {
            showSnackBarShort(view, context.resources.getString(R.string.copied))
        }
    }

    /**
     * Copies text to clipboard in debug builds only.
     *
     * @param text The text to be copied.
     */
    fun URLCopy(text: String?) {

        val manager = MyApplication.getAppContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("COPY", text)
        manager.setPrimaryClip(clip)

    }






    /**
     * Formats a count into a more readable string (e.g., 1000 -> 1K+).
     * @param countString Count as a string.
     * @return Formatted count string.
     */
    fun formatCount(countString: String): String {
        val count = countString.toLongOrNull() ?: return countString
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> "${count / 1000}K+"
            count < 1000000 -> "${count / 1000}K+"
            count < 10000000 -> "${count / 1000000}M+"
            count < 1000000000 -> "${count / 1000000}M+"
            count < 10000000000 -> "${count / 1000000000}B+"
            else -> {
                val formattedCount = count / 1000000000
                "$formattedCount B+"
            }
        }
    }



    /**
     * Hides the soft keyboard.
     * @param view The view currently in focus.
     */
    fun hideSoftKeyboard(view: View) {
        val imm = view.context.getSystemService(
            InputMethodManager::class.java
        )
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun formatJson(json: String): String {
        return try {
            if (json.trim().startsWith("{")) {
                val jsonObject = JSONObject(json)
                jsonObject.toString(4)
            } else if (json.trim().startsWith("[")) {
                val jsonArray = JSONArray(json)
                jsonArray.toString(4)
            } else {
                json
            }
        } catch (e: Exception) {
            json // Return the original string if it's not valid JSON
        }
    }


}