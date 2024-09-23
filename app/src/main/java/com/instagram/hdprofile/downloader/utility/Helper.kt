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
     * Checks if a network connection is available.
     *
     * @return True if a network connection is available, false otherwise.
     * @throws SecurityException If network permissions are missing.
     *
     * Note: Requires ACCESS_NETWORK_STATE and INTERNET permissions.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = MyApplication.getAppContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION") return networkInfo.isConnected
        }
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
     * Formats a timestamp into "dd/MM/yy h:mm a" format.
     * @param t Timestamp in seconds.
     * @return Formatted date string.
     */
    fun postTime(t: Long): String {
        val date = Date(t * 1000)
        val dateFormat = SimpleDateFormat("dd/MM/yy h:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * Formats a timestamp into "dd/MM/yy" format.
     * @param t Timestamp in seconds.
     * @return Formatted date string.
     */
    fun archiveTime(t: Long): String {
        val date = Date(t * 1000)
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * Returns current time formatted as "dd/MM/yyyy h:mm:ss a".
     * @return Current time as formatted string.
     */
    fun downloadTime(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val currentDate = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy h:mm:ss a", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    /**
     * Formats a millisecond timestamp into "dd/MM/yyyy h:mm:ss a".
     * @param currentTimeMillis Timestamp in milliseconds.
     * @return Formatted date string.
     */
    fun cacheTime(currentTimeMillis: Long): String {
        val currentDate = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy h:mm:ss a", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    /**
     * Formats a millisecond timestamp into "dd(HH:mm)" format.
     * @param currentTimeMillis Timestamp in milliseconds.
     * @return Formatted date string.
     */
    fun chartTime(currentTimeMillis: Long): String {
        val currentDate = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("dd(HH:mm)", Locale.getDefault())
        return dateFormat.format(currentDate)
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
     * Converts a number to its word representation.
     * @param number The number to convert.
     * @return The word representation of the number.
     */
    fun convertNumberToWords(number: Long): String {
        if (number == 0L) return "zero"

        val below20 = arrayOf(
            "",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "ten",
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen"
        )

        val tens = arrayOf(
            "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
        )

        val thousands = arrayOf("", "thousand", "million", "billion")

        fun convertChunk(num: Int): String {
            var number = num
            var result = ""

            if (number >= 100) {
                result += below20[number / 100] + " hundred "
                number %= 100
            }

            if (number >= 20) {
                result += tens[number / 10] + " "
                number %= 10
            }

            if (number > 0) {
                result += below20[number] + " "
            }

            return result.trim()
        }

        var result = ""
        var chunkCount = 0
        var n = number

        while (n > 0) {
            val chunk = (n % 1000).toInt()
            if (chunk != 0) {
                result = convertChunk(chunk) + " " + thousands[chunkCount] + " " + result
            }
            n /= 1000
            chunkCount++
        }

        return result.trim()
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

    /**
     * Returns the first five letters of a string.
     * @param input The input string.
     * @return The first five letters or the entire string if shorter.
     */
    fun getFirstFiveLetters(input: String?): String? {
        return if (input == null || input.length < 5) {
            input
        } else {
            input.substring(0, 5)
        }
    }


}