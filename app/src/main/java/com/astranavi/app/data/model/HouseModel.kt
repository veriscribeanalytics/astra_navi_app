package com.astranavi.app.data.model

data class House(
    val id: Int,
    val nameEn: String,
    val nameHi: String,
    val sanskrit: String,
    val naturalSign: String,
    val naturalKaraka: String,
    val element: String,
    val quality: String,
    val type: String,
    val classification: String,
    val bodyPart: String,
    val significations: List<String>,
    val coreMeaning: String,
    val planetsInHouse: Map<String, String>
)
