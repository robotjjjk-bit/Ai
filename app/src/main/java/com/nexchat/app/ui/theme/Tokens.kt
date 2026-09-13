package com.nexchat.app.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

object ColorTokens {
    val CanvasLight = Color(0xFFFBFBFA)
    val CanvasDark = Color(0xFF141210)
    val PrimaryLight = Color(0xFF1F1B16)
    val PrimaryDark = Color(0xFFEDE8DF)
    val PastelBlue = Color(0xFFE1F3FE)
    val PastelGreen = Color(0xFFEDF3EC)
    val PastelYellow = Color(0xFFFBF3DB)
    val PastelRed = Color(0xFFFDEBEC)
}

val NexType = Typography(
    displayLarge = TextStyle(fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.02).sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.6.sp, fontFamily = FontFamily.Default),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.4.sp),
    labelSmall = TextStyle(fontSize = 11.sp, letterSpacing = 0.05.sp, fontFamily = FontFamily.Monospace),
)
val NexShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
)
