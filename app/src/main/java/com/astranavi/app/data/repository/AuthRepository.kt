package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
import com.astranavi.app.data.cache.ApiCachePolicy
import com.astranavi.app.data.cache.ApiResponseCache
import com.astranavi.app.data.cache.CacheMeta
import com.astranavi.app.data.model.LoginRequest
import com.astranavi.app.data.model.LoginResponse
import com.astranavi.app.data.model.LogoutRequest
import com.astranavi.app.data.model.ProfileResponse
import com.astranavi.app.data.model.ProfileUpdateRequest
import com.astranavi.app.data.model.User
import com.astranavi.app.util.ProfileChangeBus
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val apiCache: ApiResponseCache? = null
) {
    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return apiService.login(LoginRequest(email, password))
    }

    suspend fun validateToken(): Response<Unit> {
        return apiService.authMe()
    }

    suspend fun getProfile(forceRefresh: Boolean = false, metaConsumer: ((CacheMeta) -> Unit)? = null): Response<ProfileResponse> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.profileKey(),
            responseClass = ProfileResponse::class.java,
            policy = ApiCachePolicy.UntilNextLocalMidnight,
            bypassRead = forceRefresh,
            shouldCache = { it.user != null },
            onMeta = metaConsumer
        ) {
            apiService.getProfile()
        } ?: apiService.getProfile()
    }

suspend fun updateProfile(user: User): Response<ProfileResponse> {
        val request = ProfileUpdateRequest(
            name = user.name,
            dob = user.dob,
            tob = user.tob,
            pob = user.pob,
            birthPlaceName = user.birthPlaceName,
            birthLatitude = user.birthLatitude,
            birthLongitude = user.birthLongitude,
            birthTimezoneName = user.birthTimezoneName,
            birthTimezoneOffsetAtBirth = user.birthTimezoneOffsetAtBirth,
            birthTimeFold = user.birthTimeFold,
            phoneNumber = user.phoneNumber,
            gender = user.gender,
            maritalStatus = user.maritalStatus,
            occupation = user.occupation,
            language = user.language
        )
        val response = apiService.updateProfile(request)
        if (response.isSuccessful) {
            apiCache?.invalidateCurrentUserProfileAndAstrology()
            ProfileChangeBus.bump()
        }
        return response
    }

    suspend fun updateLanguage(language: String): Response<ProfileResponse> {
        val response = apiService.updateProfile(ProfileUpdateRequest(language = language))
        if (response.isSuccessful) {
            apiCache?.invalidateCurrentUserProfileAndAstrology()
            ProfileChangeBus.bump()
        }
        return response
    }

    suspend fun register(email: String, password: String): Response<com.astranavi.app.data.model.RegisterResponse> {
        return apiService.register(com.astranavi.app.data.model.RegisterRequest(email, password))
    }

    suspend fun logout(refreshToken: String): Response<okhttp3.ResponseBody> {
        return apiService.logout(LogoutRequest(refreshToken))
    }

    suspend fun deleteUser(): Response<Map<String, String>> {
        val response = apiService.deleteUser()
        if (response.isSuccessful) {
            apiCache?.clearAll()
        }
        return response
    }
}
