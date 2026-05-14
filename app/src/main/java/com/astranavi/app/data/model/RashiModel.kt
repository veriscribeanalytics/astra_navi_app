package com.astranavi.app.data.model

data class Rashi(
    val id: Int,
    val nameEn: String,
    val nameHi: String,
    val span: String,
    val rulingPlanet: String,
    val element: String,
    val quality: String,
    val gender: String,
    val guna: String,
    val caste: String,
    val direction: String,
    val bodyPart: String,
    val color: String,
    val gemstone: String,
    val day: String,
    val season: String,
    val decanRulers: String,
    val coreNature: String,
    val keywords: List<String>,
    val positiveTraits: List<String>,
    val challengingTraits: List<String>,
    val asLagna: String,
    val planetsInRashi: Map<String, String>,
    val careers: List<String>
)
