package com.astranavi.app.data.repository

import com.astranavi.app.data.model.Yoga

object YogaData {
    val yogas = listOf(
        Yoga(
            id = "raja", nameEn = "Raja Yoga", nameHi = "राज योग", sanskrit = "Raja Yoga",
            represents = "Power, authority, royal status, success in high positions.",
            classification = "Kendra-Trikona Union", keyPlanet = "9th & 10th Lords",
            potency = "High", logic = "Union of Kendra (Action) & Trikona (Luck)",
            traits = listOf("Leadership", "Fame", "Prosperity"),
            deepDive = "Raja Yoga blends effort with divine grace, leading to an elevated social position."
        ),
        Yoga(
            id = "gajakesari", nameEn = "Gajakesari", nameHi = "गजकेसरी योग", sanskrit = "Gajakesari Yoga",
            represents = "Wisdom, reputation, victory over enemies, intelligence.",
            classification = "Jup-Moon Relation", keyPlanet = "Jupiter & Moon",
            potency = "High", logic = "Jupiter in Kendra from Moon",
            traits = listOf("Wisdom", "Eloquence", "Virtue"),
            deepDive = "Represented by the Elephant and the Lion, this yoga brings together wisdom and mental strength."
        )
    )
}
