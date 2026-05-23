package com.astranavi.app.util

import com.astranavi.app.R
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

object ApiErrorParser {

    fun parse(response: Response<*>): UiText {
        val code = response.code()
        val body = try { response.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }

        val parsed = extractMessage(body)

        return when {
            parsed != null -> UiText.DynamicString(parsed)
            code == 401 -> UiText.StringResource(R.string.error_invalid_credentials)
            code == 403 -> UiText.StringResource(R.string.error_forbidden)
            code == 404 -> UiText.StringResource(R.string.error_account_not_found)
            code == 409 -> UiText.StringResource(R.string.error_account_exists)
            code == 422 -> UiText.StringResource(R.string.error_validation_failed)
            code == 429 -> UiText.StringResource(R.string.error_rate_limited)
            code in 500..599 -> UiText.StringResource(R.string.error_server_unavailable)
            else -> UiText.StringResource(R.string.error_login_failed, code.toString())
        }
    }

    private fun extractMessage(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val json = JSONObject(body)
            when {
                json.has("detail") -> readDetail(json.get("detail"))
                json.has("error") -> json.optString("error").takeUnless { it.isBlank() }
                json.has("message") -> json.optString("message").takeUnless { it.isBlank() }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readDetail(detail: Any?): String? {
        return when (detail) {
            is String -> detail.takeUnless { it.isBlank() }
            is JSONArray -> {
                if (detail.length() == 0) return null
                val first = detail.optJSONObject(0) ?: return null
                first.optString("msg").takeUnless { it.isBlank() }
            }
            is JSONObject -> detail.optString("msg").takeUnless { it.isBlank() }
            else -> null
        }
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return false
        return android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
    }
}
