package com.helpmom.mother.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val FallbackFont = FontFamily.SansSerif

val HelpMomTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = FallbackFont, fontWeight = FontWeight.Medium),
    displayMedium = Typography().displayMedium.copy(fontFamily = FallbackFont, fontWeight = FontWeight.Medium),
    displaySmall = Typography().displaySmall.copy(fontFamily = FallbackFont, fontWeight = FontWeight.Medium),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = FallbackFont, fontWeight = FontWeight.SemiBold),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = FallbackFont, fontWeight = FontWeight.SemiBold),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = FallbackFont, fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontFamily = FallbackFont, fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontFamily = FallbackFont, fontWeight = FontWeight.Medium),
    titleSmall = Typography().titleSmall.copy(fontFamily = FallbackFont, fontWeight = FontWeight.Medium),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = FallbackFont, fontSize = 16.sp),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = FallbackFont),
    bodySmall = Typography().bodySmall.copy(fontFamily = FallbackFont),
    labelLarge = Typography().labelLarge.copy(fontFamily = FallbackFont, fontWeight = FontWeight.Medium),
    labelMedium = Typography().labelMedium.copy(fontFamily = FallbackFont),
    labelSmall = Typography().labelSmall.copy(fontFamily = FallbackFont)
)
