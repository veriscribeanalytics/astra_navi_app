package com.astranavi.app.data.model

data class Astrologer(
    val id: Int,
    val name: String,
    val exp: Int,
    val special: List<String>,
    val rating: Double,
    val reviews: Int,
    val price: Int,
    val online: Boolean,
    val img: String
)
