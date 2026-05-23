package com.astranavi.app.util

import android.content.ClipData
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry

suspend fun Clipboard.setSensitiveText(label: String, text: String) {
    val clipData = ClipData.newPlainText(label, text).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
    }
    setClipEntry(clipData.toClipEntry())
}
