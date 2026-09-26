package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CompactConfig(
    val isCompact: Boolean,
    val screenPadding: Dp,
    val cardPadding: Dp,
    val sectionSpacing: Dp,
    val itemSpacing: Dp,
    val primaryButtonHeight: Dp,
    val secondaryButtonHeight: Dp,
    val chipHeight: Dp,
    val iconSize: Dp,
    val titleTextSize: TextUnit,
    val bodyTextSize: TextUnit
)

val LocalCompactConfig = compositionLocalOf {
    CompactConfig(
        isCompact = false,
        screenPadding = 16.dp,
        cardPadding = 20.dp,
        sectionSpacing = 18.dp,
        itemSpacing = 12.dp,
        primaryButtonHeight = 50.dp,
        secondaryButtonHeight = 44.dp,
        chipHeight = 36.dp,
        iconSize = 24.dp,
        titleTextSize = 22.sp,
        bodyTextSize = 14.sp
    )
}

@Composable
fun rememberCompactConfig(
    forceCompact: Boolean,
    autoDetectSmallScreen: Boolean
): CompactConfig {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 380 || configuration.screenHeightDp < 700
    val active = forceCompact || (autoDetectSmallScreen && isSmallScreen)

    return if (active) {
        CompactConfig(
            isCompact = true,
            screenPadding = 10.dp,
            cardPadding = 12.dp,
            sectionSpacing = 10.dp,
            itemSpacing = 6.dp,
            primaryButtonHeight = 40.dp,
            secondaryButtonHeight = 36.dp,
            chipHeight = 28.dp,
            iconSize = 18.dp,
            titleTextSize = 17.sp,
            bodyTextSize = 12.sp
        )
    } else {
        CompactConfig(
            isCompact = false,
            screenPadding = 16.dp,
            cardPadding = 20.dp,
            sectionSpacing = 18.dp,
            itemSpacing = 12.dp,
            primaryButtonHeight = 50.dp,
            secondaryButtonHeight = 44.dp,
            chipHeight = 36.dp,
            iconSize = 24.dp,
            titleTextSize = 22.sp,
            bodyTextSize = 14.sp
        )
    }
}
