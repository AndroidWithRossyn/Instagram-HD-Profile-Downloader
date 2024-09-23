package com.instagram.hdprofile.downloader.utility

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Base ViewModel class for managing UI-related data and state, including loading status and snackbar messages.
 *
 * @param application The application context, used to initialize the ViewModel.
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    /** LiveData representing the loading state of the view. */
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    /**
     * Sets the loading state.
     *
     * @param isLoading True if loading is in progress, false otherwise.
     */
    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    /** LiveData for Snackbar messages. */
    private val _snackBarMessage = MutableLiveData<String?>()
    val snackBarMessage: LiveData<String?> get() = _snackBarMessage

    /**
     * Sets the message to be displayed in the Snackbar.
     *
     * @param string The message to display.
     */
    fun showSnackBar(string: String) {
        _snackBarMessage.value = string
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Clears the loading state and Snackbar message.
     */
    override fun onCleared() {
        _loading.value = false
        _snackBarMessage.value = null
        super.onCleared()
    }

    /**
     * Sealed class representing the result of an operation, either successful or with an error.
     *
     * @param T The type of the successful result.
     */
    sealed class Result<out T> {
        /**
         * Represents a successful result containing data.
         *
         * @param result The result data.
         */
        data class Success<out T>(val result: T) : Result<T>()

        /**
         * Represents an error with an exception.
         *
         * @param exception The exception that occurred.
         */
        data class Error(val exception: Throwable) : Result<Nothing>()
    }
}