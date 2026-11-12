package com.arathort.localinteraction.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.arathort.localinteraction.presentation.util.Dimensions

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = Dimensions.textSizeBodyLarge,
        lineHeight = Dimensions.lineHeightBodyLarge,
        letterSpacing = Dimensions.letterSpacingBodyLarge
    )
)