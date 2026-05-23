package com.astranavi.app.data.repository

import com.astranavi.app.data.model.Rashi

fun rashisFor(locale: String): List<Rashi> = when (locale) {
    "hi" -> RashiDataHi.rashis
    "ko" -> RashiDataKo.rashis
    else -> RashiData.rashis
}
