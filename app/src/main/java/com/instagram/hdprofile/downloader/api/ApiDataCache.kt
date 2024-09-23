package com.instagram.hdprofile.downloader.api

import android.content.Context
import android.content.SharedPreferences
import android.system.Os.remove
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object ApiDataCache {
    const val TAG = "ApiDataCache"
    const val TIME_CACHE: Long = 1 * 60 * 1000L //  1 Min
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private const val PREF_NAME = "ApiDataCache"

    fun initialize(context: Context) {

        try {
            val masterKeyAlias =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

            this.sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun <T : Any> put(
        key: String, value: T, ttlMillis: Long = TIME_CACHE
    ) {
        val jsonValue = gson.toJson(value)
        val expirationTime = System.currentTimeMillis() + ttlMillis
        val editor = sharedPreferences.edit()
        editor.putString("$key:value", jsonValue)
        editor.putLong("$key:expiration", expirationTime)
        editor.apply()
        Log.d(TAG, "Putting key: $key with TTL: ${cacheTime(expirationTime)}")

    }


    fun <T> get(key: String, clazz: Class<T>, callback: (T?) -> Unit) {
        val jsonValue = sharedPreferences.getString("$key:value", null)
        if (jsonValue == null) {
            // Cache miss
            Log.d(TAG, "Cache miss for key: $key")
            callback(null)
            return
        }
        val expirationTime = sharedPreferences.getLong("$key:expiration", 0)
        val currentTime = System.currentTimeMillis()

        if (currentTime > expirationTime) {
            remove(key)
            Log.d(
                TAG,
                "Cache entry expired for key: $key time in: ${cacheTime(currentTime)} valid for: ${
                    cacheTime(expirationTime)
                }"
            )
            callback(null)
            return
        }

        try {
            // Deserialize and provide the data
            Log.d(TAG, "Cache hit for key: $key")
            val data = gson.fromJson(jsonValue, clazz)
            callback(data)
        } catch (e: Exception) {
            Log.d(TAG, "Type mismatch or deserialization error for key: $key")
            callback(null)
        }

    }

    private fun remove(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove("$key:value")
        editor.remove("$key:expiration")
        editor.apply()
        Log.d(TAG, "Removed cache entry for key: $key")
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Cleared entire cache")
    }

    private fun cacheTime(currentTimeMillis: Long): String {
        val currentDate = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy h:mm:ss a", Locale.getDefault())
        return dateFormat.format(currentDate)
    }
}