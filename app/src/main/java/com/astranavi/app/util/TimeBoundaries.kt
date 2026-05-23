package com.astranavi.app.util

import java.util.Calendar

internal fun nextLocalHourMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.HOUR_OF_DAY, 1)
    }.timeInMillis
}

internal fun nextLocalMidnightMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
