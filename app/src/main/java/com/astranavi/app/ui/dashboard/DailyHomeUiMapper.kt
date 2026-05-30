package com.astranavi.app.ui.dashboard

import android.content.Context
import com.astranavi.app.R
import androidx.compose.ui.graphics.Color
import com.astranavi.app.data.model.*
import com.astranavi.app.ui.theme.AstroColors
import java.util.Locale
import com.astranavi.app.util.ZodiacMapper

data class HeaderUiModel(
    val greeting: String,
    val name: String,
    val metaDate: String
)

data class ChipUiModel(
    val label: String,
    val value: String,
    val color: Color,
    val iconName: String? = null
)

data class HeroCardUiModel(
    val overallScore: Int,
    val moodValue: String,
    val subtext: String,
    val dominantPlanet: String,
    val activeTransit: String,
    val dominantPlanetColor: Color,
    val dominantPlanetAsset: String?,
    val isPersonalized: Boolean
)

data class LifeAreaCardUiModel(
    val id: String,
    val label: String,
    val score: Int,
    val status: String,
    val shortInsight: String,
    val color: Color,
    val icon: String, // Emoji or Asset Name
    val fullInsight: String = "",
    val personalNotes: List<String> = emptyList()
)

data class LifeAreaDetailUiModel(
    val id: String,
    val label: String,
    val score: Int,
    val status: String,
    val fullInsight: String,
    val personalNotes: List<String>,
    val keyInfluences: List<String>,
    val color: Color
)

data class AlertUiModel(
    val title: String,
    val simpleExplanation: String,
    val technicalReason: String,
    val importance: String,
    val impactArea: String,
    val whatToDo: String,
    val whatToAvoid: String,
    val secondaryAlerts: List<String>
)

data class CosmicHourUiModel(
    val activeLabel: String,
    val activeTime: String,
    val activeAdvice: String,
    val activeColor: Color,
    val allTriggers: List<TimeTrigger>,
    val goodTime: String = "----",
    val goodTimeLabel: String = "",
    val goodTimeAdvice: String = "",
    val goodTimeStartRaw: String? = null,
    val goodTimeEndRaw: String? = null,
    val rahukaal: String = "----",
    val rahukaalAdvice: String = "",
    val rahukaalStartRaw: String? = null,
    val rahukaalEndRaw: String? = null
)

data class PanchangaUiModel(
    val tithi: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val vaara: String,
    val luckyColor: String,
    val luckyNumber: Int,
    val retrogradePlanets: List<String>
)

data class FamilyMemberUiModel(
    val id: String,
    val name: String,
    val relation: String,
    val score: Int,
    val bondingStatus: String,
    val communicationLevel: String,
    val emotionalConnection: String,
    val advice: String,
    val avatarId: Int // Placeholder or drawable ID
)

object DailyHomeUiMapper {

    fun map(
        horoscope: HoroscopeResponse,
        moonSign: String?,
        sunSign: String?,
        lagnaSign: String?,
        userName: String?,
        isDarkTheme: Boolean,
        context: Context,
        timings: DailyHoroscopeTimingsResponse? = null
    ): DailyHomeUiState {
        // 1. Header
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val greetingStr = when (hour) {
            in 5..11 -> context.getString(R.string.dashboard_greeting_morning)
            in 12..16 -> context.getString(R.string.dashboard_greeting_afternoon)
            in 17..20 -> context.getString(R.string.dashboard_greeting_evening)
            else -> context.getString(R.string.dashboard_greeting_night)
        }
        val header = HeaderUiModel(
            greeting = greetingStr,
            name = userName ?: horoscope.user?.name ?: context.getString(R.string.dashboard_seeker),
            metaDate = horoscope.meta?.date_display ?: context.getString(R.string.dashboard_today)
        )

        // 2. Chips
        val chips = mutableListOf<ChipUiModel>()
        moonSign?.let {
            chips.add(ChipUiModel(context.getString(R.string.dashboard_moon), titleCase(it), AstroColors.Moon, ZodiacMapper.getEnglishName(it)))
        }
        sunSign?.let {
            chips.add(ChipUiModel(context.getString(R.string.dashboard_sun), titleCase(it), AstroColors.Sun, ZodiacMapper.getEnglishName(it)))
        }
        chips.add(
            ChipUiModel(
                label = context.getString(R.string.dashboard_chip_today_panchang),
                value = context.getString(R.string.dashboard_chip_panchang_view),
                color = AstroColors.Jupiter
            )
        )

        // 3. Hero
        val overall = horoscope.score.overall
        val dominantPlanet = normalizePlanetName(horoscope.planetary?.dominant_planet) ?: "Moon"
        val dasha = horoscope.planetary?.active_dasha?.replace("Mahadasha", "MD")?.replace("Antardasha", "AD") ?: context.getString(R.string.dashboard_saturn_transit)
        val hero = HeroCardUiModel(
            overallScore = overall,
            moodValue = horoscope.mood?.value ?: getStatusText("general", overall, context),
            subtext = firstSentence(horoscope.tip?.text ?: horoscope.current_state?.advice_now),
            dominantPlanet = titleCase(dominantPlanet),
            activeTransit = dasha,
            dominantPlanetColor = AstroColors.getPlanetaryColor(dominantPlanet),
            dominantPlanetAsset = resolvePlanetDrawableName(dominantPlanet),
            isPersonalized = horoscope.system?.is_personalized == true
        )

        // 4. Life Areas
        val areas = mutableListOf<LifeAreaCardUiModel>()
        val textData = horoscope.areas_text


        // Love
        val loveScore = horoscope.score.areas?.get("love")?.value ?: overall
        val loveInsight = textData?.love?.insight ?: ""
        areas.add(
            LifeAreaCardUiModel(
                id = "love",
                label = context.getString(R.string.dashboard_love),
                score = loveScore,
                status = getStatusText("love", loveScore, context),
                shortInsight = firstSentence(loveInsight),
                color = AstroColors.Venus,
                icon = "❤️",
                fullInsight = resolveAreaLabels(loveInsight),
                personalNotes = textData?.love?.personal_notes?.map { resolveAreaLabels(it) } ?: emptyList()
            )
        )

        // Career
        val careerScore = horoscope.score.areas?.get("career")?.value ?: overall
        val careerInsight = textData?.career?.insight ?: ""
        areas.add(
            LifeAreaCardUiModel(
                id = "career",
                label = context.getString(R.string.dashboard_career),
                score = careerScore,
                status = getStatusText("career", careerScore, context),
                shortInsight = firstSentence(careerInsight),
                color = AstroColors.Saturn,
                icon = "💼",
                fullInsight = resolveAreaLabels(careerInsight),
                personalNotes = textData?.career?.personal_notes?.map { resolveAreaLabels(it) } ?: emptyList()
            )
        )

        // Finance
        val financeScore = horoscope.score.areas?.get("finance")?.value ?: overall
        val financeInsight = textData?.finance?.insight ?: ""
        areas.add(
            LifeAreaCardUiModel(
                id = "finance",
                label = context.getString(R.string.dashboard_finance),
                score = financeScore,
                status = getStatusText("finance", financeScore, context),
                shortInsight = firstSentence(financeInsight),
                color = AstroColors.Jupiter,
                icon = "💵",
                fullInsight = resolveAreaLabels(financeInsight),
                personalNotes = textData?.finance?.personal_notes?.map { resolveAreaLabels(it) } ?: emptyList()
            )
        )

        // Health
        val healthScore = horoscope.score.areas?.get("health")?.value ?: overall
        val healthInsight = textData?.health?.insight ?: ""
        areas.add(
            LifeAreaCardUiModel(
                id = "health",
                label = context.getString(R.string.dashboard_health),
                score = healthScore,
                status = getStatusText("health", healthScore, context),
                shortInsight = firstSentence(healthInsight),
                color = AstroColors.Mercury,
                icon = "🩺",
                fullInsight = resolveAreaLabels(healthInsight),
                personalNotes = textData?.health?.personal_notes?.map { resolveAreaLabels(it) } ?: emptyList()
            )
        )

        // Spiritual
        val spiritualScore = horoscope.score.areas?.get("spiritual")?.value ?: overall
        val spiritualInsight = textData?.spiritual?.insight ?: ""
        areas.add(
            LifeAreaCardUiModel(
                id = "spiritual",
                label = context.getString(R.string.consult_tone_spiritual),
                score = spiritualScore,
                status = getStatusText("spiritual", spiritualScore, context),
                shortInsight = firstSentence(spiritualInsight),
                color = AstroColors.Default,
                icon = "🕉️",
                fullInsight = resolveAreaLabels(spiritualInsight),
                personalNotes = textData?.spiritual?.personal_notes?.map { resolveAreaLabels(it) } ?: emptyList()
            )
        )

        // General
        val generalInsight = horoscope.tip?.text ?: horoscope.current_state?.advice_now ?: ""
        val adviceRaw = horoscope.current_state?.advice_now.orEmpty()
        val generalNotes = adviceRaw.split(".")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { resolveAreaLabels(it) + "." }
        areas.add(
            LifeAreaCardUiModel(
                id = "general",
                label = context.getString(R.string.dashboard_general),
                score = overall,
                status = getStatusText("general", overall, context),
                shortInsight = firstSentence(generalInsight),
                color = AstroColors.Default,
                icon = "✨",
                fullInsight = resolveAreaLabels(generalInsight),
                personalNotes = generalNotes
            )
        )

        // 5. Alert
        val primaryAlert = horoscope.alerts?.primary
        val alert = AlertUiModel(
            title = titleCase(primaryAlert?.type ?: context.getString(R.string.dashboard_saturn_focus)),
            simpleExplanation = resolveAreaLabels(primaryAlert?.simple ?: context.getString(R.string.dashboard_saturn_focus_simple)),
            technicalReason = resolveAreaLabels(primaryAlert?.technical ?: context.getString(R.string.dashboard_saturn_focus_technical)),
            importance = titleCase(primaryAlert?.importance ?: "high"),
            impactArea = titleCase(resolveAreaLabels(primaryAlert?.impact_area ?: "career")),
            whatToDo = context.getString(R.string.dashboard_saturn_focus_do),
            whatToAvoid = context.getString(R.string.dashboard_saturn_focus_avoid),
            secondaryAlerts = horoscope.alerts?.secondary?.map { resolveAreaLabels(it.simple) } ?: emptyList()
        )

        // 6. Cosmic Hour
        val triggers = timings?.time_triggers ?: horoscope.time_triggers ?: emptyList()
        val activeTrigger = findActiveOrUpcomingTrigger(triggers)
        val goodTimeRange = timings?.good_time?.let { formatTimeRange(it.start, it.end) } ?: "----"
        val goodTimeLabel = timings?.good_time?.label ?: ""
        val goodTimeAdvice = timings?.good_time?.advice ?: ""
        val rahukaalRange = timings?.rahukaal?.let { formatTimeRange(it.start, it.end) } ?: "----"
        val rahukaalAdvice = timings?.rahukaal?.advice ?: ""

        val cosmicHour = CosmicHourUiModel(
            activeLabel = activeTrigger?.label?.replace("✨", "")?.trim() ?: context.getString(R.string.dashboard_cosmic_flow),
            activeTime = activeTrigger?.let { formatTimeRange(it.start, it.end) } ?: context.getString(R.string.dashboard_all_day),
            activeAdvice = activeTrigger?.advice ?: context.getString(R.string.dashboard_cosmic_flow_advice),
            activeColor = activeTrigger?.let { triggerStyleFor(it.type).color } ?: AstroColors.Default,
            allTriggers = triggers,
            goodTime = goodTimeRange,
            goodTimeLabel = goodTimeLabel,
            goodTimeAdvice = goodTimeAdvice,
            goodTimeStartRaw = timings?.good_time?.start,
            goodTimeEndRaw = timings?.good_time?.end,
            rahukaal = rahukaalRange,
            rahukaalAdvice = rahukaalAdvice,
            rahukaalStartRaw = timings?.rahukaal?.start,
            rahukaalEndRaw = timings?.rahukaal?.end
        )

        // 7. Panchanga
        val panchanga = PanchangaUiModel(
            tithi = resolveAreaLabels(timings?.panchanga?.tithi ?: horoscope.meta?.panchanga?.tithi ?: context.getString(R.string.dashboard_shukla_panchami)),
            nakshatra = resolveAreaLabels(timings?.panchanga?.nakshatra ?: horoscope.meta?.panchanga?.nakshatra ?: context.getString(R.string.dashboard_rohini)),
            yoga = resolveAreaLabels(timings?.panchanga?.yoga ?: horoscope.meta?.panchanga?.yoga ?: context.getString(R.string.dashboard_siddha)),
            karana = resolveAreaLabels(timings?.panchanga?.karana ?: horoscope.meta?.panchanga?.karana ?: context.getString(R.string.dashboard_kaulava)),
            vaara = resolveAreaLabels(timings?.panchanga?.vaara ?: horoscope.meta?.panchanga?.vaara ?: context.getString(R.string.dashboard_wednesday)),
            luckyColor = titleCase(horoscope.lucky?.color ?: context.getString(R.string.dashboard_pale_yellow)),
            luckyNumber = horoscope.lucky?.number ?: 7,
            retrogradePlanets = horoscope.planetary?.retrograde ?: emptyList()
        )

        return DailyHomeUiState(
            header = header,
            chips = chips,
            hero = hero,
            lifeAreas = areas,
            alert = alert,
            cosmicHour = cosmicHour,
            panchanga = panchanga
        )
    }

    private fun getStatusText(area: String, score: Int, context: Context): String {
        return when {
            score < 35 -> {
                when (area.lowercase()) {
                    "love" -> context.getString(R.string.dashboard_status_love_needs_patience)
                    "career" -> context.getString(R.string.dashboard_status_career_move_slowly)
                    "finance" -> context.getString(R.string.dashboard_status_finance_needs_caution)
                    "health" -> context.getString(R.string.dashboard_status_health_needs_rest)
                    "spiritual" -> context.getString(R.string.dashboard_status_spiritual_seek_stillness)
                    "overall" -> context.getString(R.string.dashboard_status_heavy_friction)
                    else -> context.getString(R.string.dashboard_status_challenging)
                }
            }
            score < 50 -> {
                when (area.lowercase()) {
                    "love" -> context.getString(R.string.dashboard_status_love_needs_patience)
                    "career" -> context.getString(R.string.dashboard_status_career_move_slowly)
                    "finance" -> context.getString(R.string.dashboard_status_finance_needs_caution)
                    "health" -> context.getString(R.string.dashboard_status_health_needs_rest)
                    "spiritual" -> context.getString(R.string.dashboard_status_spiritual_seek_stillness)
                    "overall" -> context.getString(R.string.dashboard_status_challenging)
                    else -> context.getString(R.string.dashboard_status_delicate)
                }
            }
            score < 65 -> context.getString(R.string.dashboard_status_balanced)
            score < 80 -> {
                when (area.lowercase()) {
                    "career" -> context.getString(R.string.dashboard_status_good_progress)
                    "finance" -> context.getString(R.string.dashboard_status_stable)
                    else -> context.getString(R.string.dashboard_status_favorable)
                }
            }
            else -> context.getString(R.string.dashboard_status_excellent)
        }
    }

    private fun firstSentence(text: String?, maxChars: Int = 90): String {
        val resolved = resolveAreaLabels(text.orEmpty()).trim()
        if (resolved.isEmpty()) return ""
        val sentence = resolved.split(".").firstOrNull()?.trim().orEmpty()
        val candidate = if (sentence.isNotEmpty()) "$sentence." else resolved
        return if (candidate.length <= maxChars) {
            candidate
        } else {
            candidate.take(maxChars).trimEnd('.', ',', ' ') + "..."
        }
    }

    private fun resolveAreaLabels(text: String): String {
        var result = text
        // Clean broken placeholder text
        result = result.replace("area_label.vitality", "vitality")
        result = result.replace("area_label.income", "income")
        result = result.replace("area_label.home", "home life")
        result = result.replace("area_label.romance", "romance")
        result = result.replace("area_label.wealth", "wealth")
        result = result.replace("area_label.self", "self-confidence")
        
        result = result.replace(Regex("area_label\\.([A-Za-z_]+)")) { match ->
            match.groupValues[1]
                .replace("_", " ")
                .replaceFirstChar { it.lowercase() }
        }
        return result
    }

    private fun normalizePlanetName(planet: String?): String? {
        return when (planet?.trim()?.lowercase()) {
            "ke", "ketu" -> "ketu"
            "ra", "rahu" -> "rahu"
            else -> planet
        }
    }

    private fun resolvePlanetDrawableName(planet: String?): String? {
        return when (planet?.trim()?.lowercase()) {
            "sun", "surya" -> "sun"
            "moon", "chandra" -> "moon"
            "mars", "mangal" -> "mars"
            "mercury", "budh" -> "mercury"
            "jupiter", "guru" -> "jupiter"
            "venus", "shukra" -> "venus"
            "saturn", "shani" -> "saturn"
            "rahu" -> "rahu"
            "ketu" -> "ketu"
            else -> null
        }
    }

    private fun titleCase(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text.trim().split("\\s+".toRegex()).joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    private fun findActiveOrUpcomingTrigger(triggers: List<TimeTrigger>?): TimeTrigger? {
        if (triggers.isNullOrEmpty()) return null
        val now = java.util.Calendar.getInstance()
        val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        
        fun parseToMinutes(timeStr: String): Int {
            return try {
                val parts = timeStr.split(":")
                parts[0].trim().toInt() * 60 + parts[1].trim().split(" ")[0].toInt()
            } catch (e: Exception) { 0 }
        }

        val active = triggers.find {
            val start = parseToMinutes(it.start)
            val end = parseToMinutes(it.end)
            currentMinutes in start..end
        }
        if (active != null) return active
        
        return triggers.filter { parseToMinutes(it.start) > currentMinutes }
                       .minByOrNull { parseToMinutes(it.start) }
    }

    private fun formatTimeRange(start: String, end: String): String {
        fun format12h(time: String): String {
            return try {
                val parts = time.split(":")
                val hour = parts[0].trim().toInt()
                val minute = parts[1].trim().split(" ")[0].toInt()
                val ampm = if (hour >= 12) "PM" else "AM"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                "$displayHour:${if (minute < 10) "0$minute" else "$minute"} $ampm"
            } catch (e: Exception) { time }
        }
        return "${format12h(start)} - ${format12h(end)}"
    }

    private fun triggerStyleFor(type: String): TriggerStyle {
        return when (type.lowercase()) {
            "social" -> TriggerStyle(AstroColors.Venus, "venus")
            "emotional" -> TriggerStyle(AstroColors.Moon, "moon")
            "energy" -> TriggerStyle(AstroColors.Mars, "mars")
            else -> TriggerStyle(AstroColors.Default, null)
        }
    }

    data class TriggerStyle(val color: Color, val asset: String?)
}

data class DailyHomeUiState(
    val header: HeaderUiModel,
    val chips: List<ChipUiModel>,
    val hero: HeroCardUiModel,
    val lifeAreas: List<LifeAreaCardUiModel>,
    val alert: AlertUiModel,
    val cosmicHour: CosmicHourUiModel,
    val panchanga: PanchangaUiModel
)
