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

    fun sanitizeHttpCode(code: Int): String {
        return when (code) {
            401, 403 -> "Your session needs a refresh. Please sign in again."
            402 -> "This feature requires an upgrade."
            404 -> "We couldn't find this guidance right now."
            408, 429 -> "The server is busy. Please try again in a moment."
            in 500..599 -> "Our service is temporarily unavailable. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }
}
