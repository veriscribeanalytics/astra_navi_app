package com.astranavi.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object LocaleFormatter {
    private val isoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val iso24Time = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun normalize(locale: Locale): Locale =
        Locale.Builder().setLocale(locale).setExtension('u', "nu-latn").build()

    fun displayDate(isoDateString: String, locale: Locale, pattern: String = "dd MMM yyyy"): String =
        try {
            LocalDate.parse(isoDateString, isoDate)
                .format(DateTimeFormatter.ofPattern(pattern, normalize(locale)))
        } catch (e: Exception) {
            isoDateString
        }

    fun displayDayOfWeek(isoDateString: String, locale: Locale, full: Boolean = false): String =
        try {
            LocalDate.parse(isoDateString, isoDate)
                .format(DateTimeFormatter.ofPattern(if (full) "EEEE" else "EEE", normalize(locale)))
        } catch (e: Exception) {
            ""
        }

    fun displayMonthDay(isoDateString: String, locale: Locale, full: Boolean = false): String =
        try {
            LocalDate.parse(isoDateString, isoDate)
                .format(DateTimeFormatter.ofPattern(if (full) "MMMM d" else "MMM d", normalize(locale)))
        } catch (e: Exception) {
            ""
        }

    fun displayTime(iso24TimeString: String, locale: Locale, pattern: String = "HH:mm"): String =
        try {
            LocalTime.parse(iso24TimeString, iso24Time)
                .format(DateTimeFormatter.ofPattern(pattern, normalize(locale)))
        } catch (e: Exception) {
            iso24TimeString
        }

    fun number(n: Number, locale: Locale): String =
        NumberFormat.getInstance(normalize(locale)).format(n)

    fun currency(amount: Double, currencyCode: String, locale: Locale): String {
        val nf = NumberFormat.getCurrencyInstance(normalize(locale))
        try {
            nf.currency = java.util.Currency.getInstance(currencyCode)
        } catch (_: Exception) {
        }
        return nf.format(amount)
    }
}

@Composable
fun currentAppLocale(): Locale {
    val configuration = LocalConfiguration.current
    return if (!configuration.locales.isEmpty) {
        configuration.locales.get(0) ?: Locale.getDefault()
    } else {
        Locale.getDefault()
    }
}
