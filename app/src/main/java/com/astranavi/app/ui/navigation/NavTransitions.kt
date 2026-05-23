package com.astranavi.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

// Forward push is 260ms; back is 220ms — back feels lighter than forward.
private const val PUSH_FORWARD_MS = 260
private const val PUSH_BACK_MS = 220
private const val TAB_MS = 200
private const val INSTANT_MS = 0

/** Animation when this destination becomes the visible page (forward push). */
fun TransitionStyle.enterForward(): EnterTransition = when (this) {
    TransitionStyle.NONE -> fadeIn(animationSpec = tween(INSTANT_MS))
    TransitionStyle.TAB -> fadeIn(animationSpec = tween(TAB_MS, easing = LinearOutSlowInEasing)) +
            scaleIn(initialScale = 0.97f, animationSpec = tween(TAB_MS, easing = LinearOutSlowInEasing))
    TransitionStyle.PUSH -> slideInHorizontally(
        animationSpec = tween(PUSH_FORWARD_MS, easing = FastOutSlowInEasing),
        initialOffsetX = { full -> (full * 0.30f).toInt() }
    ) + fadeIn(animationSpec = tween(PUSH_FORWARD_MS, easing = FastOutSlowInEasing))
}

/** Animation when this destination is being replaced by a new page (forward push). */
fun TransitionStyle.exitForward(): ExitTransition = when (this) {
    TransitionStyle.NONE -> fadeOut(animationSpec = tween(INSTANT_MS))
    TransitionStyle.TAB -> fadeOut(animationSpec = tween(TAB_MS, easing = FastOutLinearInEasing)) +
            scaleOut(targetScale = 1.03f, animationSpec = tween(TAB_MS, easing = FastOutLinearInEasing))
    TransitionStyle.PUSH -> fadeOut(animationSpec = tween(PUSH_FORWARD_MS, easing = FastOutLinearInEasing)) +
            scaleOut(targetScale = 0.96f, animationSpec = tween(PUSH_FORWARD_MS, easing = FastOutLinearInEasing))
}

/** Animation when this destination reappears after a back navigation. */
fun TransitionStyle.enterBack(): EnterTransition = when (this) {
    TransitionStyle.NONE -> fadeIn(animationSpec = tween(INSTANT_MS))
    TransitionStyle.TAB -> fadeIn(animationSpec = tween(TAB_MS, easing = LinearOutSlowInEasing)) +
            scaleIn(initialScale = 0.97f, animationSpec = tween(TAB_MS, easing = LinearOutSlowInEasing))
    TransitionStyle.PUSH -> fadeIn(animationSpec = tween(PUSH_BACK_MS, easing = LinearOutSlowInEasing)) +
            scaleIn(initialScale = 0.98f, animationSpec = tween(PUSH_BACK_MS, easing = LinearOutSlowInEasing))
}

/** Animation when this destination is being popped off the back stack. */
fun TransitionStyle.exitBack(): ExitTransition = when (this) {
    TransitionStyle.NONE -> fadeOut(animationSpec = tween(INSTANT_MS))
    TransitionStyle.TAB -> fadeOut(animationSpec = tween(TAB_MS, easing = FastOutLinearInEasing)) +
            scaleOut(targetScale = 1.03f, animationSpec = tween(TAB_MS, easing = FastOutLinearInEasing))
    TransitionStyle.PUSH -> slideOutHorizontally(
        animationSpec = tween(PUSH_BACK_MS, easing = FastOutLinearInEasing),
        targetOffsetX = { full -> (full * 0.30f).toInt() }
    ) + fadeOut(animationSpec = tween(PUSH_BACK_MS, easing = FastOutLinearInEasing))
}
