package com.instagram.hdprofile.downloader.utility

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Random
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * This object handles the SharedPreferences operations.
 * It provides methods to save, retrieve, and delete preferences.
 * This class demonstrates the use of SharedPreferences for storing user preferences.
 *
 */
object SharedPref {

    private var TAG = "SharedPref"

    /**
     * The encrypted shared preferences instance.
     */
    private var preferences: SharedPreferences? = null

    /** The name of the SharedPreferences file. */
    private const val SHARED_PREFERENCE_FILE_NAME = "INSTA-PROFILE-PREFERENCE"

    /**
     * Initializes the SharedPreferences with optional encryption.
     *
     * This method first attempts to initialize EncryptedSharedPreferences for securely storing
     * sensitive data. If an error occurs during encryption setup, it defaults to using
     * regular SharedPreferences to maintain functionality.
     *
     * @param context The application context used to initialize the SharedPreferences.
     *                This context is required to access the appropriate resources and files.
     *
     */
    fun init(context: Context) {
        Log.d(TAG, "init: $context")
        try {
            // Create a MasterKey for EncryptedSharedPreferences using AES256_GCM encryption.
            val masterKeyAlias =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

            // Initialize EncryptedSharedPreferences with AES256 encryption for keys and values.
            this.preferences = EncryptedSharedPreferences.create(
                context,
                SHARED_PREFERENCE_FILE_NAME,
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {

            /*   Fallback to regular SharedPreferences in case of encryption failure.
             *    Crashing in Android Samsung Devices
             */
            this.preferences = context.getSharedPreferences(
                SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE
            )
            Log.e(TAG, "Exception: ${e.message}", e)
        }
    }

    /**
     * @return the preferences value is isInitialized on Activity Start
     */
    fun isInitialized(): Boolean {
        return preferences != null
    }

    /**
     * Registers a listener to be notified when a shared preference is changed.
     *
     * @param listener The callback that will run when a shared preference is changed.
     */
    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregisters a previously registered listener.
     *
     * @param listener The callback to be unregistered.
     */
    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }

    var isLoadAgain by BooleanPreference(Constants.IS_LOAD_AGAIN, false)


    var userIdIsSave by StringPreference("USER_ID", "1234567")
    var cookies by StringPreference("COOKIES", null)
    var tempCookies by StringPreference("COOKIESTEMP", getTempCookiesOld())
    var isUserLogin by BooleanPreference("userLogIn", false)

    /** The default user agent string for HTTPs requests. */
    private const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Windows NT 10.0; Win64; x64; rv:109.0;Mobile; LG-M255; rv:113.0; SM-A205U; LM-Q720; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.131 Mobile Safari/537.36 Instagram 282.0.0.22.119"

    var userAgent by StringPreference("userAgent", DEFAULT_USER_AGENT)
    var xIgAppId by StringPreference("xIgAppId", "1217981644879628")
    var xAsbID by StringPreference("xAsbID", "129477")
    var xClaimId by StringPreference(
        "xhmacId", "hmac.AR0uHyFEZl9d6GKZBXiqURKdzyC0Mrva4KlYR0xacolbkt1d"
    )

    /**
     * Manages string preferences.
     * @property name The key for the preference.
     * @property defaultValue The default string value.
     */
    private class StringPreference(private val name: String, private val defaultValue: String?) :
        ReadWriteProperty<SharedPref, String?> {
        override fun getValue(thisRef: SharedPref, property: KProperty<*>) =
            thisRef.preferences?.getString(name, defaultValue) ?: defaultValue

        override fun setValue(thisRef: SharedPref, property: KProperty<*>, value: String?) {
            thisRef.preferences!!.edit { putString(name, value) }
        }
    }


    /**
     * A collection of preference classes for various data types.
     * Each class implements ReadWriteProperty for type-safe shared preference access.
     */

    /**
     * Manages boolean preferences.
     * @property name The key for the preference.
     * @property defaultValue The default boolean value.
     */
    private class BooleanPreference(private val name: String, private val defaultValue: Boolean) :
        ReadWriteProperty<SharedPref, Boolean> {
        override fun getValue(thisRef: SharedPref, property: KProperty<*>): Boolean =
            thisRef.preferences?.getBoolean(name, defaultValue) ?: defaultValue

        override fun setValue(thisRef: SharedPref, property: KProperty<*>, value: Boolean) {
            thisRef.preferences?.edit { putBoolean(name, value) }
        }
    }

    /**
     * Retrieves a randomly selected temporary cookie string from a predefined list.
     *
     * @return A randomly selected cookie string.
     */
    private fun getTempCookiesOld(): String {
        val tempCookiesDefault = listOf(
            "mid=Zm2EMQALAAGINNooHDC5LzShkjpy; datr=MYRtZlwTm2kXcfp9LEhBPNiC; ig_did=447C30E3-0C0E-4CA8-8AFA-4F3BD8FD59F7; ig_nrcb=1; csrftoken=HeatHz3rOFCmrdOK9tILQj",
            "mid=Zj2KpAALAAGaIV6e1V7H2b6tRtv_; datr=dYORZc6qTxG4i3KkXnW9FWCn; ig_did=77AB642B-E23B-4266-B343-6755187DC767; ig_nrcb=1; csrftoken=rJTrrlq4x4xTQApBsYOOkHgN7PCE2gOX",
            "mid=ZavX8QALAAG2SU3GkOgzI1ULhj3O; datr=FEeSZTybyY2VvF3KzdSiTg66; ig_did=CA800CF3-12FF-41DA-8791-00AE1907E628; ig_nrcb=1; csrftoken=zcH9yo1421ikj9DWvUu8FOoyYa92HLUa",
            "mid=ZkQWfgALAAF6bFRi5UYhxAchLU50; datr=evifZdUhQn6sGdNuvuOtNOvU; ig_did=F3C79A5C-4A71-46FF-9E13-26FA2815FEBA; ig_nrcb=1; csrftoken=HXCgaOCLgLpJ2yEHE2LlB6xE2UyATHlv"
//        " csrftoken=; mid=; datr=; ig_did=; ig_nrcb=1; ps_l=1; ps_n=1",

            // add your own cookies
        )

        val random = Random().nextInt(tempCookiesDefault.size)
        return tempCookiesDefault[random]
    }

    /**
     * Retrieves the original cookies based on the availability in SharedPreferences.
     *
     * @return The original cookie string or null if not available.
     */
    fun orgCookies(): String? {
        return when {
            this.cookies != null -> this.cookies
            this.tempCookies != null -> this.tempCookies
            else -> this.getTempCookiesOld()
        }
    }

    /**
     * Generates a modified cookie string based on available cookies or a temporary one.
     *
     * @return A modified cookie string with specific keys (csrftoken, mid, datr, ig_did).
     */
    fun modifiedCookies(): String {
        return try {
            val cookies: String? = when {
                this.cookies != null -> this.cookies
                this.tempCookies != null -> this.tempCookies
                else -> getTempCookiesOld()
            }
            Log.d(TAG, "modifiedCookies: $cookies")
            val temp = cookies!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var mid = "mid=Zj2KpAALAAGaIV6e1V7H2b6tRtv_"
            var ig_did = "ig_did=77AB642B-E23B-4266-B343-6755187DC767"
            var csrftoken = "csrftoken=rJTrrlq4x4xTQApBsYOOkHgN7PCE2gOX"
            var datr = "datr=dYORZc6qTxG4i3KkXnW9FWCn"

            for (cookie in temp) {
                when {
                    cookie.startsWith("mid") -> mid = cookie
                    cookie.startsWith("ig_did") -> ig_did = cookie
                    cookie.startsWith("csrftoken") -> csrftoken = cookie
                    cookie.startsWith("datr") -> datr = cookie
                }
            }
            val MODIFIED_COOKIES = "$csrftoken $mid $datr $ig_did ig_nrcb=1; ps_l=1; ps_n=1;"
            //            String MODIFIED_COOKIES = mid + " " + ig_did + " " + "ig_nrcb=1;" + " " + csrftoken;
            //            Log.d("Cookies", "\n\n return from string Cookies:-\t" + MODIFIED_COOKIES + "\n\n\n");
            MODIFIED_COOKIES.substring(0, MODIFIED_COOKIES.length - 1)
        } catch (e: Exception) {
            getTempCookiesOld()
        }
    }

    /**
     * Retrieves the user agent string wrapped in double quotes.
     *
     * @return The user agent string enclosed in double quotes.
     */
    fun userAgent(): String {
        val finalUserAgent = userAgent!!
        return buildString {
            append("\"")
            append(finalUserAgent)
            append("\"")
        }
    }
}