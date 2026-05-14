package com.astranavi.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {
    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_MOON_SIGN = stringPreferencesKey("user_moon_sign")
        private val USER_SUN_SIGN = stringPreferencesKey("user_sun_sign")
        private val USER_LAGNA_SIGN = stringPreferencesKey("user_lagna_sign")
        private val USER_DOB = stringPreferencesKey("user_dob")
        private val USER_TOB = stringPreferencesKey("user_tob")
        private val USER_POB = stringPreferencesKey("user_pob")
        private val USER_BIRTH_PLACE_NAME = stringPreferencesKey("user_birth_place_name")
        private val USER_BIRTH_LATITUDE = doublePreferencesKey("user_birth_latitude")
        private val USER_BIRTH_LONGITUDE = doublePreferencesKey("user_birth_longitude")
        private val USER_BIRTH_TIMEZONE_NAME = stringPreferencesKey("user_birth_timezone_name")
        private val USER_BIRTH_TIMEZONE_OFFSET = doublePreferencesKey("user_birth_timezone_offset")
        private val USER_BIRTH_TIME_FOLD = doublePreferencesKey("user_birth_time_fold")
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val PROFILE_COMPLETE = booleanPreferencesKey("profile_complete")
        private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        private const val API_CACHE_PREFIX = "api_cache_"
        }

        val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID] }
        val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL] }
        val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
        val moonSign: Flow<String?> = context.dataStore.data.map { it[USER_MOON_SIGN] }
        val sunSign: Flow<String?> = context.dataStore.data.map { it[USER_SUN_SIGN] }
        val lagnaSign: Flow<String?> = context.dataStore.data.map { it[USER_LAGNA_SIGN] }
        val userDob: Flow<String?> = context.dataStore.data.map { it[USER_DOB] }
        val userTob: Flow<String?> = context.dataStore.data.map { it[USER_TOB] }
        val userPob: Flow<String?> = context.dataStore.data.map { it[USER_POB] }
    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }
    val profileComplete: Flow<Boolean?> = context.dataStore.data.map { it[PROFILE_COMPLETE] }
    val themePreference: Flow<String> = context.dataStore.data.map { it[THEME_PREFERENCE] ?: "system" }

        suspend fun saveSession(
        id: String, 
        email: String, 
        name: String?, 
        moonSign: String?, 
        sunSign: String? = null, 
        lagnaSign: String? = null, 
        dob: String? = null, 
        tob: String? = null, 
        pob: String? = null,
        accessToken: String? = null,
        refreshToken: String? = null,
        profileComplete: Boolean? = null
        ) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[USER_EMAIL] = email
            name?.let { prefs[USER_NAME] = it }
            moonSign?.let { prefs[USER_MOON_SIGN] = it }
            sunSign?.let { prefs[USER_SUN_SIGN] = it }
            lagnaSign?.let { prefs[USER_LAGNA_SIGN] = it }
            dob?.let { prefs[USER_DOB] = it }
            tob?.let { prefs[USER_TOB] = it }
            pob?.let { prefs[USER_POB] = it }
            accessToken?.let { prefs[ACCESS_TOKEN] = it }
            refreshToken?.let { prefs[REFRESH_TOKEN] = it }
            profileComplete?.let { prefs[PROFILE_COMPLETE] = it }
        }
        }

        suspend fun updateTokens(accessToken: String, refreshToken: String?) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            refreshToken?.let { prefs[REFRESH_TOKEN] = it }
        }
        }

        suspend fun updateSigns(
moonSign: String?, sunSign: String?, lagnaSign: String?) {
        context.dataStore.edit { prefs ->
            moonSign?.let { prefs[USER_MOON_SIGN] = it }
            sunSign?.let { prefs[USER_SUN_SIGN] = it }
            lagnaSign?.let { prefs[USER_LAGNA_SIGN] = it }
        }
    }

    suspend fun updateProfileData(dob: String?, tob: String?, pob: String?,
                                        birthPlaceName: String? = null, birthLatitude: Double? = null,
                                        birthLongitude: Double? = null, birthTimezoneName: String? = null,
                                        birthTimezoneOffsetAtBirth: Double? = null, birthTimeFold: Double? = null) {
        context.dataStore.edit { prefs ->
            dob?.let { prefs[USER_DOB] = it }
            tob?.let { prefs[USER_TOB] = it }
            pob?.let { prefs[USER_POB] = it }
            birthPlaceName?.let { prefs[USER_BIRTH_PLACE_NAME] = it }
            birthLatitude?.let { prefs[USER_BIRTH_LATITUDE] = it }
            birthLongitude?.let { prefs[USER_BIRTH_LONGITUDE] = it }
            birthTimezoneName?.let { prefs[USER_BIRTH_TIMEZONE_NAME] = it }
            birthTimezoneOffsetAtBirth?.let { prefs[USER_BIRTH_TIMEZONE_OFFSET] = it }
            birthTimeFold?.let { prefs[USER_BIRTH_TIME_FOLD] = it }
        }
    }

    suspend fun updateProfileComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PROFILE_COMPLETE] = complete
        }
    }

    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { it[THEME_PREFERENCE] = theme }
    }

    suspend fun getApiCacheEntry(cacheKey: String): String? {
        val preferenceKey = stringPreferencesKey(API_CACHE_PREFIX + cacheKey)
        return context.dataStore.data.map { it[preferenceKey] }.first()
    }

    suspend fun putApiCacheEntry(cacheKey: String, value: String) {
        val preferenceKey = stringPreferencesKey(API_CACHE_PREFIX + cacheKey)
        context.dataStore.edit { prefs ->
            prefs[preferenceKey] = value
        }
    }

    suspend fun removeApiCacheEntry(cacheKey: String) {
        val preferenceKey = stringPreferencesKey(API_CACHE_PREFIX + cacheKey)
        context.dataStore.edit { prefs ->
            prefs.remove(preferenceKey)
        }
    }

    suspend fun removeApiCacheEntriesStartingWith(cacheKeyPrefix: String) {
        val preferencePrefix = API_CACHE_PREFIX + cacheKeyPrefix
        context.dataStore.edit { prefs ->
            val cacheKeys = prefs.asMap().keys.filter { it.name.startsWith(preferencePrefix) }
            cacheKeys.forEach { prefs.remove(it) }
        }
    }

    suspend fun clearApiCache() {
        context.dataStore.edit { prefs ->
            val cacheKeys = prefs.asMap().keys.filter { it.name.startsWith(API_CACHE_PREFIX) }
            cacheKeys.forEach { prefs.remove(it) }
        }
    }

suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            val themeValue = prefs[THEME_PREFERENCE]
            prefs.clear()
            if (themeValue != null) {
                prefs[THEME_PREFERENCE] = themeValue
            }
        }
    }
}
