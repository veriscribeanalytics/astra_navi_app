package com.astranavi.app.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

// --- AUTH & USER ---
@Serializable
data class UserPreferences(
    val horoscope: Boolean? = null,
    val notifications: Boolean? = null
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String? = null,
    val image: String? = null,
    val astrologyData: AnalyzeFullResponse? = null,
    val moonSign: String? = null,
    val sunSign: String? = null,
    val lagnaSign: String? = null,
    val dob: String? = null,
    val tob: String? = null,
    val pob: String? = null,
    val birthPlaceName: String? = null,
    val birthLatitude: Double? = null,
    val birthLongitude: Double? = null,
    val birthTimezoneName: String? = null,
    val birthTimezoneOffsetAtBirth: Double? = null,
    val birthTimeFold: Double? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val maritalStatus: String? = null,
    val occupation: String? = null,
    val tier: String? = null,
    val language: String? = null,
    @Contextual val chartContext: JsonElement? = null,
    @Contextual val preferences: JsonElement? = null
)

@Serializable
data class ProfileUpdateRequest(
    val name: String? = null,
    val dob: String? = null,
    val tob: String? = null,
    val pob: String? = null,
    val birthPlaceName: String? = null,
    val birthLatitude: Double? = null,
    val birthLongitude: Double? = null,
    val birthTimezoneName: String? = null,
    val birthTimezoneOffsetAtBirth: Double? = null,
    val birthTimeFold: Double? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val maritalStatus: String? = null,
    val occupation: String? = null,
    val language: String? = null,
    val preferences: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val profileComplete: Boolean? = null,
    val user: User
)

@Serializable
data class LoginResponse(
    val user: User,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val profileComplete: Boolean? = null
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)

@Serializable
data class ProfileResponse(
    val user: User? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val requiresReanalysis: Boolean? = null
)

// --- DASHBOARD / HOROSCOPE ---
@Serializable
data class HoroscopeUser(val name: String, val sign: String)
@Serializable
data class HoroscopeMeta(val date: String, val date_display: String, val generated_at: String)
@Serializable
data class ScoreData(val overall: Int, val areas: Map<String, AreaScoreValue>?)
@Serializable
data class AreaScoreValue(val value: Int)
@Serializable
data class TipData(val text: String, val type: String)
@Serializable
data class MoodData(val value: String, val type: String)
@Serializable
data class LuckyData(val color: String, val number: Int)
@Serializable
data class PlanetaryData(val dominant_planet: String?, val active_dasha: String?)
@Serializable
data class AreasTextData(
    val career: AreaInsight?,
    val love: AreaInsight?,
    val health: AreaInsight?,
    val finance: AreaInsight?
)
@Serializable
data class AreaInsight(val insight: String, val tone: String)
@Serializable
data class AlertsData(val primary: AlertItem?, val secondary: List<AlertItem>?, val display_limit: Int? = null)
@Serializable
data class AlertItem(
    val type: String,
    val importance: String,
    val technical: String?,
    val simple: String,
    val impact_area: String?
)
@Serializable
data class TimeTrigger(
    val start: String,
    val end: String,
    val type: String,
    val label: String,
    val advice: String
)
@Serializable
data class CurrentStateData(
    val energy: String,
    val derived_from: List<String>?,
    val advice_now: String
)
@Serializable
data class AstroExplanationsData(val enabled: Boolean, val items: List<AstroExplanationItem>?)
@Serializable
data class AstroExplanationItem(val technical: String, val simple: String, val importance: String)
@Serializable
data class StreakData(val current: Int, val reward: String?)
@Serializable
data class EngagementData(val streak: StreakData?)
@Serializable
data class SystemData(val is_personalized: Boolean = false, val language: String = "English")

@Serializable
data class HoroscopeResponse(
    val user: HoroscopeUser?,
    val meta: HoroscopeMeta?,
    val score: ScoreData,
    val tip: TipData?,
    val mood: MoodData?,
    val lucky: LuckyData?,
    val planetary: PlanetaryData?,
    val areas_text: AreasTextData?,
    val alerts: AlertsData?,
    val time_triggers: List<TimeTrigger>? = null,
    val current_state: CurrentStateData?,
    val astro_explanations: AstroExplanationsData?,
    val ui_state: String?,
    val engagement: EngagementData?,
    val system: SystemData? = null,
    @SerializedName("paywall") val paywall: PaywallCardData? = null,
    val teaser: Boolean? = null,
    val message: String? = null
)

@Serializable
data class PersonalizedAlert(
    val type: String? = null,
    val importance: String? = null,
    val technical: String? = null,
    val simple: String,
    val impact_area: String? = null
)

@Serializable
data class ForecastTransit(
    val sign: String? = null,
    val house_from_moon: Int? = null,
    val house_from_lagna: Int? = null,
    val retrograde: Boolean? = null,
    val degrees: Double? = null
)

@Serializable
data class ForecastMood(
    val value: String? = null,
    val type: String? = null
)

@Serializable
data class ForecastDay(
    val date: String,
    val is_today: Boolean,
    val score: Int,
    val personal_adjustment: Int? = null,
    val text: String,
    val dominant_planet: String,
    val dominant_planet_meaning: String? = null,
    val personalized_alerts: List<PersonalizedAlert>? = null,
    val transits: Map<String, ForecastTransit>? = null,
    val mood: ForecastMood? = null,
    val lucky_color: String? = null,
    val lucky_number: Int? = null
)

@Serializable
data class ForecastRange(
    val from: String? = null,
    val to: String? = null
)

@Serializable
data class ForecastSummary(
    val best_day: String,
    val worst_day: String,
    val average_score: Double,
    val trend: String
)

@Serializable
data class ForecastResponse(
    val area: String,
    val name: String? = null,
    val moon_sign: String? = null,
    val lagna_sign: String? = null,
    val active_dasha: String? = null,
    val today_scores: Map<String, Int>? = null,
    val range: ForecastRange? = null,
    val today: String? = null,
    val days: List<ForecastDay>,
    val summary: ForecastSummary,
    val lang: String? = null
)

// --- KUNDLI / ANALYSIS ---
@Serializable
data class AnalyzeFullRequest(
    val chart_context: String? = null,
    val force_refresh: Boolean = false
)

@Serializable
data class ChartIdentity(
    val name: String? = null,
    val birth_details: String? = null,
    val ayanamsa: String? = null,
    val chart_style: String? = null
)

@Serializable
data class ChartSummaryData(
    val headline: String? = null,
    val overview: String? = null,
    val strengths: List<String>? = null,
    val challenges: List<String>? = null,
    val overall_tone: String? = null
)

@Serializable
data class KeyTheme(
    val theme: String? = null,
    val title: String? = null,
    val interpretation: String? = null
)

@Serializable
data class AshtakavargaHouseScore(
    val area: String,
    val house: Int,
    val score: Int
)

@Serializable
data class AshtakavargaData(
    val house_scores: List<Int>? = null,
    val weakest_houses: List<AshtakavargaHouseScore>? = null,
    val strongest_houses: List<AshtakavargaHouseScore>? = null
)

@Serializable
data class PlanetStrengthRank(
    val rank: Int,
    val planet: String,
    val dignity: String,
    val summary: String,
    val shadbala: Double
)

@Serializable
data class TransitPlanet(
    val planet: String,
    val current_sign: String,
    val current_house_in_natal: Int,
    val transit_interpretation: String? = null
)

@Serializable
data class TransitData(
    val date: String? = null,
    val planets: List<TransitPlanet>? = null
)

@Serializable
data class AscendantData(
    val sign: String? = null,
    val degree: Double = 0.0,
    val nakshatra: String? = null,
    val interpretation: String? = null
)

@Serializable
data class Occupant(val planet: String, val dignity: String, val retrograde: Boolean)
@Serializable
data class HouseData(
    val house: Int,
    val name: String,
    val sanskrit_name: String? = null,
    val areas: List<String>,
    val sign: String,
    val lord: String,
    val lord_house: Int? = null,
    val lord_dignity: String? = null,
    val lord_interpretation: String? = null,
    val occupants: List<Occupant>,
    val ashtakavarga_score: Int? = null,
    val strength_assessment: String? = null,
    val aspects_received: List<String>? = null
)

@Serializable
data class PlanetData(
    val planet: String,
    val sign: String,
    val house: Int,
    val degree: Double,
    val dignity: String,
    val dignity_interpretation: String? = null,
    val retrograde: Boolean,
    val combust: Boolean = false,
    val shadbala: Double = 0.0,
    val shadbala_percent: Int = 0,
    val lord_of: List<Int>? = null,
    val nakshatra: String? = null,
    val nakshatra_pada: Int? = null,
    val nakshatra_lord: String? = null,
    val nakshatra_interpretation: String? = null,
    val aspects_given: List<Int>? = null,
    val conjunctions: List<String>? = null,
    val conjunction_interpretations: List<String>? = null,
    val house_placement_interpretation: String? = null
)

@Serializable
data class DashaPeriod(
    val planet: String,
    val start: String,
    val end: String,
    val interpretation: String? = null
)

@Serializable
data class CurrentDasha(
    val mahadasha: DashaPeriod? = null,
    val antardasha: DashaPeriod? = null,
    val pratyantardasha: DashaPeriod? = null
)

@Serializable
data class DashaTimeline(
    val mahadashas: Map<String, MahadashaEntry>? = null
)

@Serializable
data class MahadashaEntry(
    val start: String,
    val end: String,
    val antardashas: Map<String, AntardashaEntry>? = null
)

@Serializable
data class AntardashaEntry(
    val start: String,
    val end: String,
    val pratyantardashas: Map<String, PratyantardashaEntry>? = null
)

@Serializable
data class PratyantardashaEntry(
    val start: String,
    val end: String
)

@Serializable
data class DashaData(
    @SerializedName("active") val active: List<DashaRow>? = null,
    @SerializedName("explanation") val explanation: List<String>? = null,
    val current: CurrentDasha? = null,
    val timeline: DashaTimeline? = null,
    val upcoming_transitions: DashaTimeline? = null
)
@Serializable
data class DashaRow(
    val planet: String, 
    val start: String, 
    val end: String, 
    val type: String? = null
)

@Serializable
data class AnalyzeFullResponse(
    @SerializedName("identity") val identity: ChartIdentity? = null,
    @SerializedName("chart_summary") val chart_summary: ChartSummaryData? = null,
    @SerializedName("ascendant") val ascendant: AscendantData? = null,
    @SerializedName("houses") val houses: List<HouseData>? = null,
    @SerializedName("planets") val planets: List<PlanetData>? = null,
    @SerializedName("dasha") val dasha: DashaData? = null,
    @SerializedName("key_themes") val key_themes: List<KeyTheme>? = null,
    @SerializedName("ashtakavarga") val ashtakavarga: AshtakavargaData? = null,
    @SerializedName("planet_strength_ranking") val planet_strength_ranking: List<PlanetStrengthRank>? = null,
    @SerializedName("transits") val transits: TransitData? = null,
    val lockedSections: Map<String, LockedContent>? = null,
    @SerializedName("lastSyncedAt") val lastSyncedAt: String? = null
)

@Serializable
data class AnalyzeFullWrapper(
    val success: Boolean,
    val message: String?,
    val astrologyData: AnalyzeFullResponse?,
    @SerializedName("paywall") val paywall: PaywallCardData? = null
)

// --- MATCH MAKING ---
@Serializable
data class PersonDetail(
    val name: String,
    val dob: String,
    val tob: String,
    val place: String,
    val gender: String = "male"
)

@Serializable
data class MatchRequest(
    val person1: PersonDetail,
    val person2: PersonDetail,
    val language: String = "english"
)

@Serializable
data class KootDetail(
    val name: String,
    val human_label: String? = null,
    val icon: String? = null,
    val obtained: Double? = null,
    val max: Int? = null,
    val detail: String? = null,
    // Support older/alternative format
    val score: Double? = null,
    val total: Int? = null,
    val description: String? = null
)

@Serializable
data class Ashtakoot(
    val total_score: Double,
    val max_score: Int? = null,
    val percentage: Int? = null,
    val verdict: String? = null,
    val koots: List<KootDetail>? = null
)

@Serializable
data class MangalDoshaPerson(
    val has_dosha: Boolean,
    val severity: String? = null,
    val from_houses: List<Int>? = null,
    val cancellation: String? = null,
    val raw_dosha: Boolean? = null
)

@Serializable
data class MangalDoshaDetail(
    val person1: MangalDoshaPerson? = null,
    val person2: MangalDoshaPerson? = null,
    val compatible: Boolean? = null,
    val note: String? = null,
    // Support older/alternative format
    val is_mangal_dosha: Boolean? = null,
    val score: Int? = null,
    val status: String? = null,
    val description: String? = null,
    val cancellation_reason: String? = null
)

@Serializable
data class MatchResponse(
    val success: Boolean = true,
    val groom_details: PersonDetail? = null,
    val bride_details: PersonDetail? = null,
    val ashtakoot: Ashtakoot? = null,
    val mangal_dosha: MangalDoshaDetail? = null,
    val ai_narrative: String? = null,
    val summary: String? = null,
    val recommendation: String? = null,
    val lang: String? = null,
    // Fallback fields for backward compatibility
    val score: Double? = null,
    val total: Int? = null,
    @SerializedName("koot_details") val koot_details: List<KootDetail>? = null
)

@Serializable
data class MatchRecord(
    val id: String,
    val person1_name: String? = null,
    val person2_name: String? = null,
    val person1Name: String? = null,
    val person2Name: String? = null,
    val groom_name: String? = null,
    val bride_name: String? = null,
    val score: Double? = null,
    val total_score: Double? = null,
    val created_at: String? = null,
    val summary: String? = null,
    val aiNarrative: String? = null,
    val resultData: MatchResponse? = null
)

@Serializable
data class MatchHistoryResponse(
    val success: Boolean = true,
    @SerializedName("history") val history: List<MatchRecord>? = null,
    @SerializedName("results") val results: List<MatchRecord>? = null
)

// --- CHAT ---
@Serializable
data class ChatMessage(
    val id: String,
    val role: String,
    @SerializedName("content") val content: String,
    val timestamp: String? = null,
    val type: String? = null,
    val rating: Int? = null,
    val feedbackTags: List<String>? = null
)

@Serializable
data class ChatSummary(
    val id: String,
    val title: String,
    val updatedAt: String? = null,
    val createdAt: String? = null,
    val averageRating: Double? = null
)

@Serializable
data class ChatHistoryWrapper(
    val chats: List<ChatSummary>? = null,
    val nextCursor: String? = null
)

@Serializable
data class ChatDetailResponse(
    val chat: ChatDetail? = null
)

@Serializable
data class ChatDetail(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatCreateRequest(
    val title: String = "New conversation",
    val language: String = "english"
)

@Serializable
data class ChatRequest(
    val text: String,
    val language: String = "english"
)

@Serializable
data class ChatResponse(val response: String)

@Serializable
data class RatingRequest(
    val rating: Int,
    val feedback_tags: List<String>? = null,
    val feedback_comment: String? = null
)

// --- CONSULTATION ---
@Serializable
data class ConsultRequest(
    val birth_date: String,
    val birth_time: String,
    val birth_place: String,
    val name: String? = null,
    val gender: String? = null,
    val maritalStatus: String? = null,
    val occupation: String? = null,
    val language: String = "English",
    val primary_category: String,
    val secondary_category: String,
    val final_question: String,
    val response_tone: String = "warm",
    val optional_note: String? = null
)

@Serializable
data class ConsultRecord(
    val id: String? = null,
    @SerializedName("category") val primary_category: String? = null,
    @SerializedName("subcategory") val secondary_category: String? = null,
    @SerializedName("question") val final_question: String? = null,
    @SerializedName("response") val insight: String? = null,
    val tone: String? = null,
    @SerializedName("createdAt") val created_at: String? = null,
    val created_at_alt: String? = null
)

@Serializable
data class ConsultHistoryResponse(
    val success: Boolean = true,
    @SerializedName("history") val results: List<ConsultRecord>? = null,
    @SerializedName("results") val results_alt: List<ConsultRecord>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)

@Serializable
data class ConsultResponse(val insight: String)

@Serializable
data class SubCategory(
    val key: String,
    val label: String,
    val questions: List<String>
)

@Serializable
data class Category(
    val key: String,
    val label: String,
    val icon: String,
    val subs: List<SubCategory>
)

@Serializable
data class ConsultTree(
    val life_stage: String,
    val primary: List<Category>,
    val hidden: List<Category>
)

@Serializable
data class AgeGroup(
    val key: String,
    val label: String,
    val life_stage: String
)

@Serializable
data class LocationSearchResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val timezone: String
)

@Serializable
data class LocationSearchResponse(
    val results: List<LocationSearchResult>
)

@Serializable
data class ConsultTreeWrapper(
    val age: Int,
    val age_group: AgeGroup? = null,
    val tree: ConsultTree
)
