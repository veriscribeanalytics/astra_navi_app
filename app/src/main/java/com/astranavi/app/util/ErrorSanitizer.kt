package com.astranavi.app.util

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Translates technical exceptions into user-friendly messages.
 * Never exposes raw exception text, hostnames, or API URLs to the UI.
 */
object ErrorSanitizer {

    fun sanitize(exception: Exception): String {
        return when (exception) {
            is UnknownHostException -> "Connection failed. Please check your internet connection."
            is SocketTimeoutException -> "Server is taking too long to respond. Please try again."
            is IOException -> "Network error. Please check your connection and try again."
            else -> "Something went wrong. Please try again."
        }
    }
}
