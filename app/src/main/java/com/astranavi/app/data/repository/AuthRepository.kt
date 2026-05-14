package com.astranavi.app.data.repository

import com.astranavi.app.data.api.ApiService
import com.astranavi.app.data.cache.ApiCachePolicy
import com.astranavi.app.data.cache.ApiResponseCache
import com.astranavi.app.data.model.LoginRequest
import com.astranavi.app.data.model.LoginResponse
import com.astranavi.app.data.model.ProfileResponse
import com.astranavi.app.data.model.ProfileUpdateRequest
import com.astranavi.app.data.model.User
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val apiCache: ApiResponseCache? = null
) {
    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return apiService.login(LoginRequest(email, password))
    }

    suspend fun getProfile(): Response<ProfileResponse> {
        return apiCache?.getOrFetch(
            logicalKey = apiCache.profileKey(),
            responseClass = ProfileResponse::class.java,
            policy = ApiCachePolicy.ForDuration(ApiResponseCache.PROFILE_TTL_MILLIS),
            shouldCache = { it.user != null }
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
        }
        return response
    }

    suspend fun register(email: String, password: String): Response<com.astranavi.app.data.model.RegisterResponse> {
        return apiService.register(com.astranavi.app.data.model.RegisterRequest(email, password))
    }

    suspend fun deleteUser(): Response<Map<String, String>> {
        val response = apiService.deleteUser()
        if (response.isSuccessful) {
            apiCache?.clearAll()
        }
        return response
    }
}
