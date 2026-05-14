package com.astranavi.app.data.model

data class NakshatraPada(
    val number: Int,
    val navamsha: String,
    val essence: String,
    val description: String
)

data class Nakshatra(
    val id: Int,
    val nameEn: String,
    val nameHi: String,
    val span: String,
    val ruler: String,
    val deity: String,
    val symbol: String,
    val gana: String,
    val guna: String,
    val caste: String,
    val gender: String,
    val nadi: String,
    val purushartha: String,
    val animal: String,
    val bird: String,
    val tree: String,
    val bodyPart: String,
    val direction: String,
    val coreNature: String,
    val keywords: List<String>,
    val positiveTraits: List<String>,
    val challengingTraits: List<String>,
    val careers: List<String>,
    val padas: List<NakshatraPada>,
    val summary: String,
    val reflection: String
)
