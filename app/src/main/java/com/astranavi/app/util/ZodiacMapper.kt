package com.astranavi.app.util

object ZodiacMapper {

    private val signLookup: Map<String, String> = buildMap {
        put("aries", "aries"); put("mesh", "aries"); put("mesha", "aries")
        put("taurus", "taurus"); put("vrish", "taurus"); put("vrishabha", "taurus")
        put("gemini", "gemini"); put("mithun", "gemini"); put("mithuna", "gemini")
        put("cancer", "cancer"); put("kark", "cancer"); put("karka", "cancer"); put("karkataka", "cancer")
        put("leo", "leo"); put("simha", "leo")
        put("virgo", "virgo"); put("kanya", "virgo"); put("kanyaa", "virgo")
        put("libra", "libra"); put("tula", "libra")
        put("scorpio", "scorpio"); put("vrishchik", "scorpio"); put("vrishchika", "scorpio")
        put("sagittarius", "sagittarius"); put("dhanu", "sagittarius"); put("dhanush", "sagittarius")
        put("capricorn", "capricorn"); put("makar", "capricorn"); put("makara", "capricorn")
        put("aquarius", "aquarius"); put("kumbh", "aquarius"); put("kumbha", "aquarius")
        put("pisces", "pisces"); put("meen", "pisces"); put("meena", "pisces")
    }

    private val idLookup: Map<Int, String> = mapOf(
        1 to "aries", 2 to "taurus", 3 to "gemini", 4 to "cancer",
        5 to "leo", 6 to "virgo", 7 to "libra", 8 to "scorpio",
        9 to "sagittarius", 10 to "capricorn", 11 to "aquarius", 12 to "pisces"
    )

    private val displayNames: Map<String, String> = mapOf(
        "aries" to "Mesh", "taurus" to "Vrish", "gemini" to "Mithun",
        "cancer" to "Kark", "leo" to "Simha", "virgo" to "Kanya",
        "libra" to "Tula", "scorpio" to "Vrishchik", "sagittarius" to "Dhanu",
        "capricorn" to "Makar", "aquarius" to "Kumbh", "pisces" to "Meen"
    )

    private val emojis: Map<String, String> = mapOf(
        "aries" to "♈", "taurus" to "♉", "gemini" to "♊",
        "cancer" to "♋", "leo" to "♌", "virgo" to "♍",
        "libra" to "♎", "scorpio" to "♏", "sagittarius" to "♐",
        "capricorn" to "♑", "aquarius" to "♒", "pisces" to "♓"
    )

    private fun resolveSignKey(sign: String?): String? {
        if (sign == null) return null
        val normalized = sign.lowercase().trim()
        if (normalized.isEmpty()) return null

        val directHit = signLookup[normalized]
        if (directHit != null) return directHit

        val idHit = normalized.toIntOrNull()?.let { idLookup[it] }
        if (idHit != null) return idHit

        for ((key, value) in signLookup) {
            if (normalized.contains(key) && key.length >= normalized.length / 2) {
                if (key == "vrish" && normalized.contains("vrishchik")) continue
                return value
            }
        }

        return normalized
    }

    fun getEnglishName(sign: String?): String? {
        val key = resolveSignKey(sign)
        return if (key != null && signLookup.containsKey(key)) key else key
    }

    fun getDisplayName(sign: String?): String {
        val key = resolveSignKey(sign)
        if (key == null) return "???"
        return displayNames[key] ?: sign!!.replaceFirstChar { it.uppercase() }
    }

    fun getEmoji(sign: String?): String {
        val key = resolveSignKey(sign)
        if (key == null) return "✨"
        return emojis[key] ?: "✨"
    }

    fun getById(id: Int): String? = idLookup[id]
}