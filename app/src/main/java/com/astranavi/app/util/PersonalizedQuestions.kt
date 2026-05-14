package com.astranavi.app.util

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

object PersonalizedQuestions {

    enum class AgeBracket(val label: String) {
        BRACKET_18_25("18-25"),
        BRACKET_26_35("26-35"),
        BRACKET_36_50("36-50"),
        BRACKET_51_PLUS("51+"),
        DEFAULT("default")
    }

    fun calculateAge(dob: String?): Int? {
        if (dob.isNullOrBlank()) return null
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(dob, formatter)
            val today = LocalDate.now()
            Period.between(birthDate, today).years
        } catch (e: Exception) {
            null
        }
    }

    fun getAgeBracket(age: Int?): AgeBracket {
        return when {
            age == null -> AgeBracket.DEFAULT
            age in 18..25 -> AgeBracket.BRACKET_18_25
            age in 26..35 -> AgeBracket.BRACKET_26_35
            age in 36..50 -> AgeBracket.BRACKET_36_50
            age >= 51 -> AgeBracket.BRACKET_51_PLUS
            else -> AgeBracket.DEFAULT
        }
    }

    fun getPersonalizedQuestions(bracket: AgeBracket): List<String> {
        return when (bracket) {
            AgeBracket.BRACKET_18_25 -> listOf(
                "What career path aligns best with my birth chart?",
                "When is the right time for me to pursue higher education?",
                "What does my chart say about finding my life partner?"
            )
            AgeBracket.BRACKET_26_35 -> listOf(
                "When will I see a breakthrough in my career or financial growth?",
                "What does my birth chart reveal about my future life partner?",
                "Is this the right time to start a business or change careers?"
            )
            AgeBracket.BRACKET_36_50 -> listOf(
                "How can I maximize wealth and stability in my current Mahadasha?",
                "What does my chart say about my children's future?",
                "When is the best time for major investments or property purchase?"
            )
            AgeBracket.BRACKET_51_PLUS -> listOf(
                "What spiritual practices align with my birth chart?",
                "How can I ensure health and longevity based on my chart?",
                "What legacy and wisdom should I focus on in this phase?"
            )
            AgeBracket.DEFAULT -> listOf(
                "When will I see a breakthrough in my career or financial growth?",
                "What does my birth chart reveal about my future life partner?",
                "Which planetary Mahadasha am I currently in and what are its effects?"
            )
        }
    }
}
