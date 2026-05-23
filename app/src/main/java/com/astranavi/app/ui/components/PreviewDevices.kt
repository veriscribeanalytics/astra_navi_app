package com.astranavi.app.ui.components

import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-device and multi-font scale Compose preview annotations for rapid UI validation.
 */

@Preview(
    name = "1. Compact Phone",
    group = "Form Factor",
    device = "spec:width=360dp,height=640dp,dpi=480",
    showSystemUi = true
)
annotation class PreviewCompact

@Preview(
    name = "2. Medium / Foldable",
    group = "Form Factor",
    device = "spec:width=600dp,height=900dp,dpi=420",
    showSystemUi = true
)
annotation class PreviewMedium

@Preview(
    name = "3. Expanded / Tablet",
    group = "Form Factor",
    device = "spec:width=840dp,height=1200dp,dpi=320",
    showSystemUi = true
)
annotation class PreviewExpanded

@Preview(
    name = "4. Large Font (1.5x)",
    group = "Accessibility",
    device = "spec:width=360dp,height=640dp,dpi=480",
    fontScale = 1.5f,
    showSystemUi = true
)
annotation class PreviewLargeFont

@Preview(
    name = "5. Foldable Open",
    group = "Form Factor",
    device = "spec:width=673dp,height=841dp,dpi=420",
    showSystemUi = true
)
annotation class PreviewFoldOpen

/**
 * Combined annotation grouping all preview states for rapid visual validation across the UI suite.
 */
@PreviewCompact
@PreviewMedium
@PreviewExpanded
@PreviewLargeFont
@PreviewFoldOpen
annotation class PreviewMultiDevice
