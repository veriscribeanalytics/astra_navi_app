package com.astranavi.app.util

import androidx.compose.runtime.Composable

// FLAG_SECURE temporarily disabled while diagnosing a Play Protect block on sideloaded
// debug APKs. Re-enable by restoring the DisposableEffect block below once distribution
// moves to Play Console internal testing or a signed release APK.
@Composable
fun SecureScreen() {
    // No-op.
}

/*
import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun SecureScreen() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
*/
