package com.astranavi.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.astranavi.app.data.repository.*
import com.astranavi.app.util.SessionManager
import com.astranavi.app.ui.astrologers.AstrologersViewModel
import com.astranavi.app.ui.chat.AvatarSelectionViewModel
import com.astranavi.app.ui.chat.ChatViewModel
import com.astranavi.app.ui.consult.ConsultHistoryViewModel
import com.astranavi.app.ui.consult.ConsultViewModel
import com.astranavi.app.ui.dashboard.DashboardViewModel
import com.astranavi.app.ui.entitlement.EntitlementViewModel
import com.astranavi.app.ui.entitlement.PlansViewModel
import com.astranavi.app.ui.kundli.KundliViewModel
import com.astranavi.app.ui.login.LoginViewModel
import com.astranavi.app.ui.login.RegistrationViewModel
import com.astranavi.app.ui.match.MatchHistoryViewModel
import com.astranavi.app.ui.match.MatchViewModel
import com.astranavi.app.ui.forecast.ForecastViewModel
import com.astranavi.app.ui.profile.ProfileViewModel
import com.astranavi.app.ui.rashis.RashiViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val dashboardRepository: DashboardRepository,
    private val astrologyRepository: AstrologyRepository,
    private val entitlementRepository: EntitlementRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ForecastViewModel::class.java) -> {
                ForecastViewModel(dashboardRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(AstrologersViewModel::class.java) -> {
                AstrologersViewModel(astrologyRepository) as T
            }
            modelClass.isAssignableFrom(AvatarSelectionViewModel::class.java) -> {
                AvatarSelectionViewModel(astrologyRepository) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(astrologyRepository, sessionManager, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(ConsultViewModel::class.java) -> {
                ConsultViewModel(astrologyRepository, sessionManager, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(ConsultHistoryViewModel::class.java) -> {
                ConsultHistoryViewModel(astrologyRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(dashboardRepository, authRepository, astrologyRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(KundliViewModel::class.java) -> {
                KundliViewModel(astrologyRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                RegistrationViewModel(authRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(MatchHistoryViewModel::class.java) -> {
                MatchHistoryViewModel(astrologyRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(MatchViewModel::class.java) -> {
                MatchViewModel(astrologyRepository, sessionManager, entitlementRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(authRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(RashiViewModel::class.java) -> {
                RashiViewModel(astrologyRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(EntitlementViewModel::class.java) -> {
                EntitlementViewModel(entitlementRepository) as T
            }
            modelClass.isAssignableFrom(PlansViewModel::class.java) -> {
                PlansViewModel(entitlementRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}