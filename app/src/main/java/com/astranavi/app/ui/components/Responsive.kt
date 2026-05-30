package com.astranavi.app.ui.components

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ScreenClass {
    CompactPhone,
    Phone,
    Fold,
    Tablet
}

data class ResponsiveMetrics(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val fontScale: Float,
    val screenClass: ScreenClass,
    val isVeryCompactWidth: Boolean,
    val isCompactWidth: Boolean,
    val isMediumWidth: Boolean,
    val isExpandedWidth: Boolean,
    val isTabletWidth: Boolean,
    val isCompactHeight: Boolean,
    val isLargeFont: Boolean,
    @Deprecated("Use useTwoPane instead", ReplaceWith("useTwoPane"))
    val useDashboardTwoPane: Boolean,
    val pagePadding: Dp,
    val cardPadding: Dp,
    val dashboardHorizontalPadding: Dp,
    val dashboardMaxContentWidth: Dp,
    val dashboardSectionGap: Dp,
    val identityBadgeWidth: Dp,
    val energyMapHeight: Dp,
    val energyMapMaxWidth: Dp,
    val orbitBubbleSize: Dp,
    val orbitCoreSize: Dp,
    val bottomBarMinHeight: Dp,
    val bottomBarHorizontalPadding: Dp,
    val bottomBarCenterGap: Dp,
    val bottomFabOuterSize: Dp,
    val bottomFabSize: Dp,
    val bottomNavIconSize: Dp,
    val bottomNavTextSize: TextUnit,
    val gridMinCellWidth: Dp,
    val listTopPadding: Dp,
    val listBottomPadding: Dp,
    val heroTopPadding: Dp,
    val heroBottomPadding: Dp,
    val heroIconSize: Dp,
    val heroPrimaryFontSize: TextUnit,
    val heroPrimaryLineHeight: TextUnit,
    val heroTitleFontSize: TextUnit,
    val heroLetterSpacing: TextUnit,
    val kundliWheelSize: Dp,
    val kundliCardPadding: Dp,
    val kundliGridSpacing: Dp,
    val kundliSectionGap: Dp,
    val kundliPagerHeight: Dp,
    val kundliPlanetIconLarge: Dp,
    val kundliPlanetIconMedium: Dp,
    val kundliPlanetIconSmall: Dp,
    val kundliPlanetIconTiny: Dp,
    val kundliProgressBarHeight: Dp,
    val kundliPagerContentPadding: Dp,
    val kundliBottomPadding: Dp,
    val kundliCardInnerPadding: Dp,
    val kundliPagePadding: Dp,
    val kundliSmallIconSize: Dp,
    val kundliHouseRingSize: Dp,
    val kundliOverlayCorner: Dp,
    val snapshotImageSize: Dp,
    val buttonHeight: Dp,
    val chatBubbleMaxWidth: Dp,
    val chatInputBarHeight: Dp,
    val chatMessagePadding: Dp,
    val profileAvatarSize: Dp,
    val profileFieldWidth: Dp,
    val profileSectionGap: Dp,
    val matchCardHeight: Dp,
    val matchIconSize: Dp,
    val forecastCardMinHeight: Dp,
    val forecastChartHeight: Dp,
    val forecastSectionGap: Dp,
    val forecastMicroStatMinWidth: Dp,
    val consultCardHeight: Dp,
    val consultAvatarSize: Dp,
    val consultSectionGap: Dp,
    val headerVerticalGap: Dp,
    val headerToBadgeGap: Dp,
    val cardInnerGap: Dp,
    val orbitTopOffset: Dp,
    val snapshotScoreFontSize: TextUnit,
    val snapshotTitleFontSize: TextUnit,
    val useTwoPane: Boolean,
    val useTabletTwoPane: Boolean,
    val twoPaneGap: Dp,
    val maxContentWidth: Dp,
    val twoPaneLeftWeight: Float,
    val twoPaneRightWeight: Float
)

// ─── Responsive Font Sizes for AppTypography ──────────────────────────────────

data class ResponsiveFontSizes(
    // Hero & Display
    val heroScore: TextUnit,
    val pageTitle: TextUnit,

    // Sections & Cards
    val sectionHeader: TextUnit,
    val cardTitle: TextUnit,
    val cardBody: TextUnit,
    val cardValue: TextUnit,

    // Labels & Micro
    val microLabel: TextUnit,
    val badgeText: TextUnit,
    val buttonLabel: TextUnit,
    val chartLabel: TextUnit,
    val bottomNavLabel: TextUnit,
    val technicalText: TextUnit
)

@Composable
fun responsiveMetrics(): ResponsiveMetrics {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val width = configuration.screenWidthDp.dp
    val height = configuration.screenHeightDp.dp
    val fontScale = density.fontScale
    val isVeryCompactWidth = width < 340.dp
    val isCompactWidth = width < 380.dp
    val isMediumWidth = width >= 600.dp
    val isExpandedWidth = width >= 840.dp
    val isTabletWidth = width >= 1000.dp
    val isCompactHeight = height < 640.dp
    val isVeryShortHeight = height < 400.dp
    val isLargeFont = fontScale >= 1.25f
    val screenClass = when {
        width >= 1000.dp -> ScreenClass.Tablet
        width >= 600.dp -> ScreenClass.Fold
        width < 380.dp -> ScreenClass.CompactPhone
        else -> ScreenClass.Phone
    }
    val useTwoPane = isMediumWidth && !isLargeFont && !isVeryShortHeight
    val useTabletTwoPane = isExpandedWidth && !isLargeFont && !isVeryShortHeight
    val useDashboardTwoPane = useTwoPane

    return ResponsiveMetrics(
        screenWidth = width,
        screenHeight = height,
        fontScale = fontScale,
        screenClass = screenClass,
        isVeryCompactWidth = isVeryCompactWidth,
        isCompactWidth = isCompactWidth,
        isMediumWidth = isMediumWidth,
        isExpandedWidth = isExpandedWidth,
        isTabletWidth = isTabletWidth,
        isCompactHeight = isCompactHeight,
        isLargeFont = isLargeFont,
        useDashboardTwoPane = useDashboardTwoPane,
        useTwoPane = useTwoPane,
        useTabletTwoPane = useTabletTwoPane,
        twoPaneGap = if (isExpandedWidth) 32.dp else 24.dp,
        maxContentWidth = if (isTabletWidth) 800.dp else 600.dp,
        twoPaneLeftWeight = if (isExpandedWidth) 1f else 1.1f,
        twoPaneRightWeight = if (isExpandedWidth) 1f else 0.9f,
        pagePadding = when {
            isVeryCompactWidth || isLargeFont -> 14.dp
            isCompactWidth -> 16.dp
            isMediumWidth -> 28.dp
            else -> 24.dp
        },
        cardPadding = when {
            isVeryCompactWidth || isLargeFont -> 14.dp
            isCompactWidth -> 16.dp
            else -> 20.dp
        },
        dashboardHorizontalPadding = when {
            isVeryCompactWidth || isLargeFont -> 12.dp
            isCompactWidth -> 16.dp
            isTabletWidth -> 36.dp
            isMediumWidth -> 28.dp
            else -> 20.dp
        },
        dashboardMaxContentWidth = when {
            isTabletWidth -> 680.dp
            isMediumWidth -> 680.dp
            else -> 570.dp
        },
        dashboardSectionGap = if (isCompactHeight || isLargeFont) 8.dp else 14.dp,
        identityBadgeWidth = when {
            isVeryCompactWidth || isLargeFont -> 154.dp
            isCompactWidth -> 160.dp
            isMediumWidth -> 210.dp
            else -> 172.dp
        },
        energyMapHeight = when {
            isCompactHeight || isLargeFont -> 236.dp
            isMediumWidth -> 340.dp
            else -> 308.dp
        },
        energyMapMaxWidth = if (isMediumWidth) 460.dp else 470.dp,
        orbitBubbleSize = when {
            isVeryCompactWidth || isLargeFont -> 56.dp
            isCompactWidth || isCompactHeight -> 58.dp
            isMediumWidth -> 76.dp
            else -> 70.dp
        },
        orbitCoreSize = when {
            isVeryCompactWidth || isLargeFont -> 100.dp
            isCompactWidth || isCompactHeight -> 104.dp
            isMediumWidth -> 144.dp
            else -> 128.dp
        },
        bottomBarMinHeight = if (isCompactHeight || isLargeFont) 66.dp else 72.dp,
        bottomBarHorizontalPadding = when {
            isVeryCompactWidth || isLargeFont -> 2.dp
            isCompactWidth -> 4.dp
            isMediumWidth -> 16.dp
            else -> 8.dp
        },
        bottomBarCenterGap = when {
            isVeryCompactWidth || isLargeFont -> 54.dp
            isCompactWidth -> 58.dp
            isMediumWidth -> 84.dp
            else -> 72.dp
        },
        bottomFabOuterSize = if (isCompactWidth || isCompactHeight || isLargeFont) 58.dp else 64.dp,
        bottomFabSize = if (isCompactWidth || isCompactHeight || isLargeFont) 50.dp else 56.dp,
        bottomNavIconSize = if (isCompactWidth || isLargeFont) 20.dp else 22.dp,
        bottomNavTextSize = if (isVeryCompactWidth || isLargeFont) 8.sp else if (isCompactWidth) 9.sp else 10.sp,
        gridMinCellWidth = if (isVeryCompactWidth || isLargeFont) 148.dp else 164.dp,
        listTopPadding = if (isCompactHeight) 80.dp else 96.dp,
        listBottomPadding = if (isCompactHeight) 96.dp else 112.dp,
        heroTopPadding = if (isCompactHeight) 72.dp else 112.dp,
        heroBottomPadding = if (isCompactHeight) 24.dp else 32.dp,
        heroIconSize = when {
            isVeryCompactWidth || isLargeFont -> 88.dp
            isCompactWidth -> 104.dp
            else -> 120.dp
        },
        heroPrimaryFontSize = when {
            isVeryCompactWidth || isLargeFont -> 42.sp
            isCompactWidth -> 48.sp
            else -> 56.sp
        },
        heroPrimaryLineHeight = when {
            isVeryCompactWidth || isLargeFont -> 48.sp
            isCompactWidth -> 54.sp
            else -> 64.sp
        },
        heroTitleFontSize = when {
            isVeryCompactWidth || isLargeFont -> 22.sp
            isCompactWidth -> 24.sp
            else -> 28.sp
        },
        heroLetterSpacing = when {
            isVeryCompactWidth || isLargeFont -> 2.sp
            isCompactWidth -> 3.sp
            else -> 6.sp
        },
        kundliWheelSize = when {
            isVeryCompactWidth || isLargeFont -> 275.dp
            isCompactWidth -> 300.dp
            isMediumWidth -> 396.dp
            else -> 330.dp
        },
        kundliCardPadding = when {
            isVeryCompactWidth || isLargeFont -> 14.dp
            isCompactWidth -> 16.dp
            isMediumWidth -> 24.dp
            else -> 20.dp
        },
        kundliGridSpacing = if (isCompactWidth || isLargeFont) 12.dp else 16.dp,
        kundliSectionGap = if (isCompactHeight || isLargeFont) 20.dp else 32.dp,
        kundliPagerHeight = when {
            isCompactHeight || isLargeFont -> 420.dp
            isMediumWidth -> 560.dp
            else -> 500.dp
        },
        kundliPlanetIconLarge = if (isVeryCompactWidth || isLargeFont) 60.dp else 72.dp,
        kundliPlanetIconMedium = if (isVeryCompactWidth || isLargeFont) 54.dp else 64.dp,
        kundliPlanetIconSmall = if (isVeryCompactWidth || isLargeFont) 48.dp else 56.dp,
        kundliPlanetIconTiny = if (isVeryCompactWidth || isLargeFont) 40.dp else 48.dp,
        kundliProgressBarHeight = if (isVeryCompactWidth || isLargeFont) 6.dp else 8.dp,
        kundliPagerContentPadding = if (isVeryCompactWidth || isLargeFont) 48.dp else 72.dp,
        kundliBottomPadding = if (isCompactHeight || isLargeFont) 24.dp else 32.dp,
        kundliCardInnerPadding = if (isVeryCompactWidth || isLargeFont) 16.dp else 20.dp,
        kundliPagePadding = when {
            isVeryCompactWidth || isLargeFont -> 14.dp
            isCompactWidth -> 16.dp
            isMediumWidth -> 28.dp
            else -> 24.dp
        },
        kundliSmallIconSize = when {
            isVeryCompactWidth || isLargeFont -> 24.dp
            isCompactWidth -> 28.dp
            isMediumWidth -> 40.dp
            else -> 32.dp
        },
        kundliHouseRingSize = when {
            isVeryCompactWidth || isLargeFont -> 44.dp
            isCompactWidth -> 48.dp
            isMediumWidth -> 72.dp
            else -> 56.dp
        },
        kundliOverlayCorner = when {
            isVeryCompactWidth || isLargeFont -> 24.dp
            isCompactWidth -> 28.dp
            isMediumWidth -> 32.dp
            else -> 28.dp
        },
        snapshotImageSize = when {
            isVeryCompactWidth || isLargeFont -> 44.dp
            isCompactWidth -> 48.dp
            isMediumWidth -> 68.dp
            else -> 56.dp
        },
        buttonHeight = if (isCompactHeight || isLargeFont) 46.dp else 52.dp,
        chatBubbleMaxWidth = when {
            isVeryCompactWidth || isLargeFont -> 240.dp
            isCompactWidth -> 280.dp
            isMediumWidth -> 400.dp
            else -> 320.dp
        },
        chatInputBarHeight = when {
            isVeryCompactWidth || isLargeFont -> 10.dp
            isCompactWidth -> 12.dp
            isMediumWidth -> 20.dp
            else -> 16.dp
        },
        chatMessagePadding = when {
            isVeryCompactWidth || isLargeFont -> 8.dp
            isCompactWidth -> 12.dp
            isMediumWidth -> 20.dp
            else -> 16.dp
        },
        profileAvatarSize = when {
            isVeryCompactWidth || isLargeFont -> 64.dp
            isCompactWidth -> 72.dp
            isMediumWidth -> 96.dp
            else -> 80.dp
        },
        profileFieldWidth = when {
            isVeryCompactWidth || isLargeFont -> 140.dp
            isCompactWidth -> 160.dp
            isMediumWidth -> 280.dp
            else -> 200.dp
        },
        profileSectionGap = when {
            isVeryCompactWidth || isLargeFont -> 12.dp
            isCompactWidth -> 16.dp
            isMediumWidth -> 28.dp
            else -> 24.dp
        },
        matchCardHeight = when {
            isVeryCompactWidth || isLargeFont -> 72.dp
            isCompactWidth -> 76.dp
            isMediumWidth -> 96.dp
            else -> 80.dp
        },
        matchIconSize = when {
            isVeryCompactWidth || isLargeFont -> 48.dp
            isCompactWidth -> 52.dp
            isMediumWidth -> 72.dp
            else -> 56.dp
        },
        forecastCardMinHeight = when {
            isVeryCompactWidth || isLargeFont -> 132.dp
            isCompactWidth -> 140.dp
            isMediumWidth -> 180.dp
            else -> 160.dp
        },
        forecastChartHeight = when {
            isVeryCompactWidth || isLargeFont -> 120.dp
            isCompactWidth -> 124.dp
            isMediumWidth -> 160.dp
            else -> 140.dp
        },
        forecastSectionGap = when {
            isVeryCompactWidth || isLargeFont -> 12.dp
            isCompactWidth -> 14.dp
            isMediumWidth -> 24.dp
            else -> 16.dp
        },
        forecastMicroStatMinWidth = when {
            isVeryCompactWidth || isLargeFont -> 72.dp
            isCompactWidth -> 84.dp
            isMediumWidth -> 120.dp
            else -> 84.dp
        },
        consultCardHeight = when {
            isVeryCompactWidth || isLargeFont -> 84.dp
            isCompactWidth -> 88.dp
            isMediumWidth -> 112.dp
            else -> 96.dp
        },
        consultAvatarSize = when {
            isVeryCompactWidth || isLargeFont -> 48.dp
            isCompactWidth -> 52.dp
            isMediumWidth -> 72.dp
            else -> 56.dp
        },
        consultSectionGap = when {
            isVeryCompactWidth || isLargeFont -> 12.dp
            isCompactWidth -> 14.dp
            isMediumWidth -> 20.dp
            else -> 16.dp
        },
        headerVerticalGap = if (isCompactHeight || isLargeFont) 3.dp else 6.dp,
        headerToBadgeGap = if (isCompactHeight || isLargeFont) 10.dp else 14.dp,
        cardInnerGap = if (isCompactHeight || isLargeFont) 6.dp else 10.dp,
        orbitTopOffset = 0.dp,
        snapshotScoreFontSize = when {
            isVeryCompactWidth || isLargeFont -> 36.sp
            isCompactWidth -> 40.sp
            isMediumWidth -> 48.sp
            else -> 44.sp
        },
        snapshotTitleFontSize = when {
            isVeryCompactWidth || isLargeFont -> 13.sp
            isCompactWidth -> 14.sp
            else -> 15.sp
        }
    )
}

@Composable
fun responsiveGridCells(): GridCells {
    return GridCells.Adaptive(minSize = responsiveMetrics().gridMinCellWidth)
}

@Composable
fun responsiveFontSizes(): ResponsiveFontSizes {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val width = configuration.screenWidthDp.dp
    val fontScale = density.fontScale

    val isVeryCompact = width < 340.dp
    val isCompact = width < 380.dp
    val isLargeFont = fontScale >= 1.25f

    return ResponsiveFontSizes(
        // Hero & Display
        heroScore = when {
            isVeryCompact || isLargeFont -> 36.sp
            isCompact -> 40.sp
            else -> 44.sp
        },
        pageTitle = when {
            isVeryCompact || isLargeFont -> 22.sp
            isCompact -> 24.sp
            else -> 28.sp
        },

        // Sections & Cards
        sectionHeader = when {
            isVeryCompact || isLargeFont -> 11.sp
            else -> 12.sp
        },
        cardTitle = when {
            isVeryCompact || isLargeFont -> 16.sp
            else -> 18.sp
        },
        cardBody = 14.sp, // Fixed, WCAG minimum for body text
        cardValue = when {
            isVeryCompact || isLargeFont -> 20.sp
            isCompact -> 22.sp
            else -> 24.sp
        },

        // Labels & Micro
        microLabel = when {
            isVeryCompact || isLargeFont -> 10.sp
            else -> 11.sp
        },
        badgeText = when {
            isVeryCompact || isLargeFont -> 11.sp
            else -> 12.sp
        },
        buttonLabel = 14.sp, // Fixed
        chartLabel = when {
            isVeryCompact || isLargeFont -> 10.sp
            else -> 11.sp
        },
        bottomNavLabel = when {
            isVeryCompact || isLargeFont -> 9.sp
            isCompact -> 9.sp
            else -> 10.sp
        },
        technicalText = 12.sp // Fixed, WCAG minimum for metadata
    )
}
